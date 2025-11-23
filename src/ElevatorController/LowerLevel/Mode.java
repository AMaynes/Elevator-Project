package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import CommandCenter.CommandPanel;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.State;
import Message.Message;
import Message.TopicCodes;

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
    private static final int TOPIC_START = TopicCodes.SYSTEM_START.code();
    private static final int TOPIC_STOP = TopicCodes.SYSTEM_STOP.code();
    //TODO: do we need to handle a SYSTEM_RESET message?
    private static final int TOPIC_CENTRALIZED = TopicCodes.CLEAR_FIRE.code();
    private static final int TOPIC_MODE = TopicCodes.MODE.code();

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
        softwareBus.subscribe(TOPIC_STOP, elevatorID);
        softwareBus.subscribe(TOPIC_CENTRALIZED, elevatorID);
        softwareBus.subscribe(TOPIC_MODE, elevatorID);
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
        //Todo: Set current mode from software bus
        Message message = softwareBus.get(TOPIC_MODE,elevatorID);
        while (message != null){
            int state = message.getBody();
            switch (state){
                case BODY_CENTRALIZED_MODE -> currentMode = State.CONTROL;
                case BODY_FIRE_MODE -> currentMode = State.FIRE;
                case BODY_NORMAL_MODE -> currentMode = State.NORMAL;
            }
            message = softwareBus.get(TOPIC_MODE,elevatorID);
        }
    }

    /**
     * Call get() on softwareBus w/ appropriate topic/subtopic,
     * @return
     */
    public FloorNDirection nextService(){
        //TODO needs Command Center stuff
        return null;}

}
