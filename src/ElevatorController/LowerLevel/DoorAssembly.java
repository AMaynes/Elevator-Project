package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
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

    //Constants for body codes
    private final int openCode = 1;
    private final int closeCode = 2;
    private final int obstructedCode = 0;
    private final int notObstructedCode = 1;
    private final int fullyClosedCode = 2;
    private final int notFullyClosedCode = 3;
    private final int fullyOpenCode = 4;
    private final int notFullyOpenCode = 5;
    private final int overCapacityCode = 0;
    private final int notOverCapacityCode = 1;

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
        softwareBus.subscribe(Topic.DOOR_SENSOR, elevatorID);
        softwareBus.subscribe(Topic.CABIN_LOAD, elevatorID);
    }

    /**
     * Send message to softwareBus to open the doors (which sends the message
     * to the MUX)
     */
    public void open(){
        // correct body for current mux
        softwareBus.publish(new Message(Topic.DOOR_CONTROL, elevatorID, openCode));
    }

    /**
     * Send message to softwareBus to close the doors (which sends the message
     * to the MUX)
     */
    public void close(){
        // correcct body for current mux 11/23/2025
        softwareBus.publish(new Message(Topic.DOOR_CONTROL, elevatorID, closeCode));
    }

    /**
     * @return true if obstruction sensor triggered, false otherwise
     */
    public boolean obstructed(){
        //Todo: ok so I assume this will return a message that says 0 for not obstructed and 1 if obstructed (DOOR_STATUS or DOOR_SENSOR????????)
        Message message = MessageHelper.pullAllMessages(softwareBus, elevatorID, Topic.DOOR_SENSOR);
        if (message != null ) {
            if (message.getBody() == obstructedCode) obstructed = true;
            if (message.getBody() == notObstructedCode) obstructed = false;
        }
        return obstructed;
    }

    /**
     * @return true if fully closed sensor triggered, false otherwise
     */
    public boolean fullyClosed(){
        // Todo: assuming 3 for fully closed and 4 for not fully closed (DOOR_STATUS or DOOR_SENSOR????????)
        Message message =  MessageHelper.pullAllMessages(softwareBus, elevatorID, Topic.DOOR_SENSOR);
        if (message != null ) {
            if (message.getBody() == fullyClosedCode) fullyClosed = true;
            if (message.getBody() == notFullyClosedCode) fullyClosed = false;
        }
        return fullyClosed;
    }

    /**
     * @return true if fully open sensor triggered, false otherwise
     */
    public boolean fullyOpen(){
        // Todo: assuming 5 for fully closed and 6 for not fully closed (DOOR_STATUS or DOOR_SENSOR????????)
        Message message =  MessageHelper.pullAllMessages(softwareBus, elevatorID, Topic.DOOR_SENSOR);
        if (message != null ) {
            if (message.getBody() == fullyOpenCode) fullyOpen = true;
            if (message.getBody() == notFullyOpenCode) fullyOpen = false;
        }
        return fullyOpen;
    }

    /**
     * @return true if an over capacity message was received, false if an under
     *         capacity message was received, true initially
     */
    public boolean overCapacity(){
        //Todo: assuming 0 for over capacity and 1 for not over capacity
        Message message = MessageHelper.pullAllMessages(softwareBus, elevatorID, Topic.CABIN_LOAD);
        if (message != null ) {
            if (message.getBody() == overCapacityCode) overCapacity = true;
            if (message.getBody() == notOverCapacityCode) overCapacity = false;
        }
        return overCapacity;
    }

}
