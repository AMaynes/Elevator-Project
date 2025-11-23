package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import ElevatorController.Util.FloorNDirection;
import Message.Message;
import Message.Topic;
import Team7MotionControl.Hardware.Elevator;

/**
 * The notifier object is used to communicate all necessary visual and audio
 * information. The notifier sends messages to the speakers, button lights, and
 * floor display (up/down arrows and LEDs for displaying the floor number). The
 * notifier object does not receive any messages from the Software Bus.
 */
public class Notifier {
    private int elevatorID;
    private SoftwareBus softwareBus;

    // Topic for car postion
    private static final int CAR_POSITION = Topic.CAR_POSITION;

    private static final int DISPLAY_DIRECTION=Topic.DISPLAY_DIRECTION;

    public  Notifier(int elevatorID, SoftwareBus softwareBus){
        //TODO: does notifier need to subscribe? or can it just publish messages?
        //TODO call subscribe on softwareBus w/ relevant topic/subtopic
        this.elevatorID = elevatorID;
        this.softwareBus = softwareBus;
    }

    /**
     * Notify Control Center and MUX of elevator status (arrived => play arrival
     * chime)
     * @param floorNDirection This elevator's current floor and direction
     */
    public void arrivedAtFloor(FloorNDirection floorNDirection){}

    /**
     * Notify Control Center and MUX of this elevator's status
     * @param floorNDirection This elevator's floor and direction
     */
    public void elevatorStatus(FloorNDirection floorNDirection){
        //Tell display direction
        int direction = floorNDirection.getDirection().getIntegerVersion();

        // Tell mux what direction we're going
        softwareBus.publish(new Message(DISPLAY_DIRECTION,elevatorID,direction));

        //Tell mux where we are
        softwareBus.publish(new Message(CAR_POSITION,elevatorID, floorNDirection.floor()));
    }

    /**
     * Notify the MUX to play the capacity buzzer
     */
    public void playCapacityNoise(){}

    /**
     * Notify the MUX to stop playing the capacity buzzer
     */
    public void stopCapacityNoise(){}

}
