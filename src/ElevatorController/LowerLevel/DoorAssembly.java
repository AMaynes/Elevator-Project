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
    private boolean obstructed;
    private boolean fullyClosed;
    private boolean fullyOpen;
    private boolean overCapacity;
    private int elevatorID;
    private SoftwareBus softwareBus;

    // Constants for topic codes
    private static final int TOPIC_DOOR_CONTROL = SoftwareBusCodes.doorControl;
    private static final int TOPIC_DOOR_SENSOR = SoftwareBusCodes.doorSensor;
    private static final int TOPIC_CABIN_LOAD = SoftwareBusCodes.cabinLoad;
    private static final int TOPIC_DOOR_STATUS = SoftwareBusCodes.doorStatus;
    private static final int TOPIC_ELEV_ONOFF = SoftwareBusCodes.elevatorOnOff;

    //Constants for body codes
    private static final int OPEN_CODE = SoftwareBusCodes.doorOpen;
    private static final int CLOSE_CODE = SoftwareBusCodes.doorClose;
    private static final int OBSTRUCTED_CODE = SoftwareBusCodes.obstructed;
    private static final int NOT_OBSTRUCTED_CODE = SoftwareBusCodes.clear;
    private static final int OVER_CAPACITY_CODE = SoftwareBusCodes.overloaded;
    private static final int NOT_OVER_CAPACITY_CODE = SoftwareBusCodes.normal;

    /**
     * Instantiate a DoorAssembly object, and run its thread
     * @param elevatorID For software bus messages
     * @param softwareBus The means of communication
     */
    public DoorAssembly(int elevatorID, SoftwareBus  softwareBus) {
        this.obstructed = false;
        this.fullyClosed = false;
        this.fullyOpen = true;
        this.overCapacity = false;
        this.softwareBus = softwareBus;
        this.elevatorID = elevatorID;

        softwareBus.subscribe(TOPIC_DOOR_SENSOR, elevatorID);
        softwareBus.subscribe(TOPIC_CABIN_LOAD, elevatorID);
        softwareBus.subscribe(TOPIC_DOOR_STATUS, elevatorID);
        // Listen for elevator on/off commands so doors open when elevator turns on
        softwareBus.subscribe(TOPIC_ELEV_ONOFF, elevatorID);

        // Publish initial door status so MUX/GUI reflect actual starting state
        if (this.fullyOpen) {
            softwareBus.publish(new Message(TOPIC_DOOR_STATUS, elevatorID, OPEN_CODE));
        } else if (this.fullyClosed) {
            softwareBus.publish(new Message(TOPIC_DOOR_STATUS, elevatorID, CLOSE_CODE));
        }

        // Start a small poller to react to elevator on/off messages
        Thread onOffPoller = new Thread(() -> {
            while (true) {
                Message msg = softwareBus.get(TOPIC_ELEV_ONOFF, elevatorID);
                if (msg != null) {
                    int body = msg.getBody();
                    if (body == SoftwareBusCodes.on) {
                        // Ensure doors are open when elevator is turned on
                        open();
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        onOffPoller.setDaemon(true);
        onOffPoller.start();
    }

    /**
     * Send message to softwareBus to open the doors (which sends the message
     * to the MUX)
     */
    public void open(){
        softwareBus.publish(new Message(TOPIC_DOOR_CONTROL, elevatorID, OPEN_CODE));
    }

    /**
     * Send message to softwareBus to close the doors (which sends the message
     * to the MUX)
     */
    public void close(){
        softwareBus.publish(new Message(TOPIC_DOOR_CONTROL, elevatorID, CLOSE_CODE));

    }

    /**
     * @return true if obstruction sensor triggered, false otherwise
     */
    public boolean obstructed(){
        Message message = MessageHelper.pullAllMessages(softwareBus, elevatorID, TOPIC_DOOR_SENSOR);
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
        Message message =  MessageHelper.pullAllMessages(softwareBus, elevatorID, TOPIC_DOOR_STATUS);
        if (message != null ) {
            int body = message.getBody();
            if (body == OPEN_CODE) fullyClosed = false;
            else if (body == CLOSE_CODE) fullyClosed = true;
            else System.out.println("Unexpected body in SoftwareBusCodes.doorStatus Message in DoorAssembly: body = " + body);
        }
        return fullyClosed;
    }

    /**
     * @return true if fully open sensor triggered, false otherwise
     */
    public boolean fullyOpen(){
        Message message =  MessageHelper.pullAllMessages(softwareBus, elevatorID, TOPIC_DOOR_STATUS);
        if (message != null ) {
            if (message.getBody() == OPEN_CODE) fullyOpen = true;
            if (message.getBody() == CLOSE_CODE) fullyOpen = false;
        }
        return fullyOpen;
    }

    /**
     * @return true if an over capacity message was received, false if an under
     *         capacity message was received, true initially
     */
    public boolean overCapacity(){
        Message message = MessageHelper.pullAllMessages(softwareBus, elevatorID, TOPIC_CABIN_LOAD);
        if (message != null ) {
            if (message.getBody() == OVER_CAPACITY_CODE) overCapacity = true;
            if (message.getBody() == NOT_OVER_CAPACITY_CODE) overCapacity = false;
        }
        return overCapacity;
    }

}
