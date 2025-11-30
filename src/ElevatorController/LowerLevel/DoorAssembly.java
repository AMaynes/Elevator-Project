package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import Message.*;

/**
 * The door assembly is a virtualization of the physical interfaces which
 * comprise the doors: fully open sensors, fully closed sensors, door
 * obstruction sensors, the scale, and the door motor. The door assembly posts
 * and receives messages from its physical counterparts via the software bus;
 * posting to the motor; receiving from the fully closed sensors, fully open
 * sensors, the scale, and the door obstruction sensors.
 */
public class DoorAssembly {
    private boolean opened;  // TODO: do we need to store this in Door Assembly?
    private boolean closed;
    private boolean obstructed;
    private boolean fullyClosed;
    private boolean fullyOpen;
    private boolean overCapacity;
    private int elevatorID;
    private SoftwareBus softwareBus;

    // Constants for topic codes
    private static final int TOPIC_DOOR_CONTROL = SoftwareBusCodes.doorControl;
    private static final int DOOR_SENSOR = SoftwareBusCodes.doorSensor;
    private static final int CABIN_LOAD = SoftwareBusCodes.cabinLoad;

    //Constants for body codes
    private static final int OPEN_CODE = 1;
    private static final int CLOSE_CODE = 2;
    private static final int OBSTRUCTED_CODE = 0;
    private static final int NOT_OBSTRUCTED_CODE = 1;
    private static final int FULLY_CLOSED_CODE = 2;
    private static final int NOT_FULLY_CLOSED_CODE = 3;
    private static final int FULLY_OPEN_CODE = 4;
    private static final int NOT_FULLY_OPEN_CODE = 5;
    private static final int OVER_CAPACITY_CODE = 0;
    private static final int NOT_OVER_CAPACITY_CODE = 1;

    /**
     * Instantiate a DoorAssembly object, and run its thread
     * @param elevatorID For software bus messages
     * @param softwareBus The means of communication
     */
    public DoorAssembly(int elevatorID, SoftwareBus  softwareBus) {

        this.opened = true;
        this.closed = false;
        this.obstructed = false;
        this.fullyClosed = false;
        this.fullyOpen = true;
        this.overCapacity = false;
        this.softwareBus = softwareBus;
        this.elevatorID = this.elevatorID;

        //Todo: (DOOR_STATUS or DOOR_SENSOR????????)
        softwareBus.subscribe(DOOR_SENSOR, elevatorID);
        softwareBus.subscribe(CABIN_LOAD, elevatorID);
    }

    /**
     * Send message to softwareBus to open the doors (which sends the message
     * to the MUX)
     */
    public void open(){
        // correct body for current mux
        softwareBus.publish(new Message(TOPIC_DOOR_CONTROL, elevatorID, OPEN_CODE));
    }

    /**
     * Send message to softwareBus to close the doors (which sends the message
     * to the MUX)
     */
    public void close(){
        // correcct body for current mux 11/23/2025
        softwareBus.publish(new Message(TOPIC_DOOR_CONTROL, elevatorID, CLOSE_CODE));
    }

    /**
     * @return true if obstruction sensor triggered, false otherwise
     */
    public boolean obstructed(){
        //Todo: ok so I assume this will return a message that says 0 for not obstructed and 1 if obstructed (DOOR_STATUS or DOOR_SENSOR????????)
        Message message = MessageHelper.pullAllMessages(softwareBus, elevatorID, DOOR_SENSOR);
        if (message != null ) {
            if (message.getBody() == OBSTRUCTED_CODE) obstructed = true;
            if (message.getBody() == NOT_OBSTRUCTED_CODE) obstructed = false;
        }
        return obstructed;
    }

    /**
     * @return true if fully closed sensor triggered, false otherwise
     */
    public boolean fullyClosed(){
        // Todo: assuming 3 for fully closed and 4 for not fully closed (DOOR_STATUS or DOOR_SENSOR????????)
        Message message =  MessageHelper.pullAllMessages(softwareBus, elevatorID, DOOR_SENSOR);
        if (message != null ) {
            if (message.getBody() == FULLY_CLOSED_CODE) fullyClosed = true;
            if (message.getBody() == NOT_FULLY_CLOSED_CODE) fullyClosed = false;
        }
        return fullyClosed;
    }

    /**
     * @return true if fully open sensor triggered, false otherwise
     */
    public boolean fullyOpen(){
        // Todo: assuming 5 for fully closed and 6 for not fully closed (DOOR_STATUS or DOOR_SENSOR????????)
        Message message =  MessageHelper.pullAllMessages(softwareBus, elevatorID, DOOR_SENSOR);
        if (message != null ) {
            if (message.getBody() == FULLY_OPEN_CODE) fullyOpen = true;
            if (message.getBody() == NOT_FULLY_OPEN_CODE) fullyOpen = false;
        }
        return fullyOpen;
    }

    /**
     * @return true if an over capacity message was received, false if an under
     *         capacity message was received, true initially
     */
    public boolean overCapacity(){
        //Todo: assuming 0 for over capacity and 1 for not over capacity
        Message message = MessageHelper.pullAllMessages(softwareBus, elevatorID, CABIN_LOAD);
        if (message != null ) {
            if (message.getBody() == OVER_CAPACITY_CODE) overCapacity = true;
            if (message.getBody() == NOT_OVER_CAPACITY_CODE) overCapacity = false;
        }
        return overCapacity;
    }

}
