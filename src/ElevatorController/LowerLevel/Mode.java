package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import CommandCenter.CommandPanel;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.State;
import Message.Message;

/**
 * The mode serves as a means for the Elevator Controller to be put into and track its current mode.
 * The mode is indirectly being updated by the Control Room, a separate entity outside of the Elevator Controller system.
 * Additionally, the mode is responsible for taking in demands from the Control Room when the elevator is being remotely controlled.
 * The mode object receives messages via the software bus but does not post messages to the software bus.
 *
 * The modes:
 *         1 – NORMAL
 *         2 – FIRE_SAFETY
 *         3 - CONTROLLED
 */
public class Mode {
    private final int elevatorID;
    private SoftwareBus softwareBus;
    private State currentMode;
    private FloorNDirection currDestination;

    // Topic Constants
    private static final int TOPIC_START = SoftwareBusCodes.elevatorOnOff;
    //TODO: do we need to handle a SYSTEM_RESET message?
    private static final int TOPIC_MODE = SoftwareBusCodes.setMode;
    private static final int TOPIC_DESTINATION =
            SoftwareBusCodes.setDestination;
    private static final int TOPIC_FIRE_MODE = SoftwareBusCodes.fireMode;
    private static final int TOPIC_FIRE_ALARM =
            SoftwareBusCodes.fireAlarmActive;
    private static final int TOPIC_SET_FIRE = SoftwareBusCodes.fireAlarm;



    //TODO: handle these in software bus getter
    // Body for mode changes
    private static final int BODY_CENTRALIZED_MODE  = CommandPanel.B_MODE_CEN;
    private static final int BODY_NORMAL_MODE = CommandPanel.B_MODE_IND;
    private static final int BODY_FIRE_MODE = CommandPanel.B_MODE_TF;

    /**
     * Instantiate a Mode object
     * @param elevatorID which elevator this Mode object is associated with
     *                   (for software bus messages)
     * @param softwareBus the means of communication
     */
    public Mode(int elevatorID, SoftwareBus softwareBus) {
        //TODO call subscribe on softwareBus w/ relevant topic/subtopic
        this.softwareBus = softwareBus;
        this.elevatorID = elevatorID;

        this.currDestination = null;

        // Initially in Normal mode
        this.currentMode = State.NORMAL;

        // Subscribe to relevant topics, subtopic is elevatorID
        softwareBus.subscribe(TOPIC_START, elevatorID);
        softwareBus.subscribe(TOPIC_MODE, elevatorID);
        softwareBus.subscribe(TOPIC_DESTINATION, elevatorID);
        softwareBus.subscribe(TOPIC_FIRE_ALARM, elevatorID);
    }

    /**
     * Call get() on softwareBus w/ appropriate topic/subtopic, until NULL is returned (only care about most recent mode
     * set), store last valid mode in currentMode, return currentMode
     * @return the currentMode this elevator is in
     */
    public State getMode(){
        setCurrentMode();
        return currentMode;
    }

    /**
     * Pulls all related messages from softwareBUs until null and
     * sets current mode equal to the last relevant message
     */
    private void setCurrentMode(){
        // From the Command Center
        Message message = softwareBus.get(TOPIC_MODE,elevatorID);
        // From the MUX
        Message fireAlarm = softwareBus.get(TOPIC_FIRE_ALARM, elevatorID);

        // Update current mode based on software bus messages sent by Command
        // Center
        while (message != null){
            int state = message.getBody();
            switch (state){
                case BODY_CENTRALIZED_MODE -> currentMode = State.CONTROL;
                case BODY_NORMAL_MODE -> currentMode = State.NORMAL;
            }
            message = softwareBus.get(TOPIC_MODE,elevatorID);
        }
        // In fire mode if the fire alarm is pulled (sent by MUX)
        while(fireAlarm != null){
            if(fireAlarm.getBody() == 1){
                //Notify the command center
                softwareBus.publish(new Message(TOPIC_FIRE_MODE, elevatorID,
                        SoftwareBusCodes.emptyBody));
                currentMode = State.FIRE;
            }
            fireAlarm = softwareBus.get(TOPIC_FIRE_ALARM, elevatorID);
        }

        // Notify the MUX that the fire is active
        if (currentMode == State.FIRE){
            softwareBus.publish(new Message(TOPIC_SET_FIRE, elevatorID,
                    SoftwareBusCodes.allElevators));
        }
    }

    /**
     * Call get() on softwareBus w/ appropriate topic/subtopic,
     * @return
     */
    public FloorNDirection nextService(){
        //TODO needs Command Center stuff
        int floor = -1;
        Message message = softwareBus.get(TOPIC_DESTINATION, elevatorID);
        //Checks to see if there is a new message for the destination
        while(message != null){
            floor = message.getBody();
            message = softwareBus.get(TOPIC_DESTINATION , elevatorID);
        }
        //When there is no next service given
        if (floor == -1) {
            return null;
        }

        return new FloorNDirection(floor, null);}

}
