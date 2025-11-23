package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.State;
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
    private static final int START_TOPIC = TopicCodes.SYSTEM_START.code();
    private static final int STOP_TOPIC = TopicCodes.SYSTEM_STOP.code();
    //TODO: do we need to handle a SYSTEM_RESET message?
    private static final int CENTRALIZED_TOPIC = TopicCodes.CLEAR_FIRE.code();

    //TODO: make this dependent on CommandCenter/CommandPanel.java
    private static final int B_MODE_TF  = 1110; //TODO: may not need to read the body

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

        // Subscribe
        softwareBus.subscribe(START_TOPIC, elevatorID);
        softwareBus.subscribe(STOP_TOPIC, elevatorID);
        softwareBus.subscribe(CENTRALIZED_TOPIC, elevatorID);
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
    }

    /**
     * Call get() on softwareBus w/ appropriate topic/subtopic,
     * @return
     */
    public FloorNDirection nextService(){return null;}

}
