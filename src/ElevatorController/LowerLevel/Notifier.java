package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import ElevatorController.Util.FloorNDirection;
import Message.Message;
import Message.Topic;

/**
 * The notifier object is used to communicate all necessary visual and audio
 * information. The notifier sends messages to the speakers, button lights, and
 * floor display (up/down arrows and LEDs for displaying the floor number). The
 * notifier object does not receive any messages from the Software Bus.
 */

//Todo: what am I supposed to do with overloaded here?
    // Seems like the MUX should be telling us overloaded?
public class Notifier {
    private int elevatorID;
    private SoftwareBus softwareBus;

    // Topic for car postion
    private static final int TOPIC_CABIN_POSITION = Topic.cabinPosition;
    private static final int TOPIC_DISPLAY_DIRECTION =Topic.displayDirection;
    private static final int TOPIC_PLAY_SOUND = Topic.playSound;
    //bodies
    private static final int BODY_ARRIVAL = 0;
    private static final int BODY_OVERLOAD = 1;

    public  Notifier(int elevatorID, SoftwareBus softwareBus){
        this.elevatorID = elevatorID;
        this.softwareBus = softwareBus;
    }

    /**
     * Notify Control Center and MUX of elevator status (arrived => play arrival
     * chime)
     * @param floorNDirection This elevator's current floor and direction
     */
    public void arrivedAtFloor(FloorNDirection floorNDirection){
        softwareBus.publish(new Message(TOPIC_PLAY_SOUND, elevatorID, BODY_ARRIVAL));
    }

    /**
     * Notify Control Center and MUX of this elevator's status
     * @param floorNDirection This elevator's floor and direction
     */
    public void elevatorStatus(FloorNDirection floorNDirection){
        //Tell display direction
        int direction = floorNDirection.getDirection().getIntegerVersion();

        // Tell mux what direction we're going
        softwareBus.publish(new Message(TOPIC_DISPLAY_DIRECTION,elevatorID,direction));

        //Tell mux where we are
        softwareBus.publish(new Message(TOPIC_CABIN_POSITION,elevatorID, floorNDirection.floor()));
    }

    /**
     * Notify the MUX to play the capacity buzzer
     */
    public void playCapacityNoise(){
        // Todo: I p sure the mux needs to be ready to receive this command.
        //  Don't see anything for it in the Photo Val sent me.
    }

    /**
     * Notify the MUX to stop playing the capacity buzzer
     */
    public void stopCapacityNoise(){
        // Todo: I p sure the mux needs to be ready to receive this command.
        //  Don't see anything for it in the Photo Val sent me.
    }

}
