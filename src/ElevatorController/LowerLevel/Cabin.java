package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import ElevatorController.Util.ConstantsElevatorControl;
import ElevatorController.Util.Direction;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.Timer;
import Message.Message;
import Message.MessageHelper;
import Message.Topic;

/**
 * The cabin provides a means for the elevator controller to send the elevator to a destination.
 * The cabin indirectly controls the motor by sending messages to the Software Bus.
 * Additionally, the cabin indirectly receives messages from physical sensors through the Software Bus.
 */
public class Cabin implements Runnable {
    private int currDest;
    private Direction currDirection;
    private int elevatorID;
    private int currFloor;
    private int topAlign;
    private int botAlign;
    private boolean motor;
    private SoftwareBus softwareBus;

    // Constants for cabin topic
    private static final int TOPIC_CAR_STOP = Topic.carStop;
    private static final int TOPIC_CAR_DISPATCH = Topic.carDispatch;
    private static final int TOPIC_TOP_FLOOR_SENSOR = Topic.topSensor;
    private static final int TOPIC_BOTTOM_SENSOR = Topic.bottomSensor;
    private static final int TOPIC_CABIN_POSITION = Topic.cabinPosition;
    private static final int TOPIC_CURR_DIRECTION = Topic.currDirection;

    //Constants for cabin bodies
    private static final int STOP_MOTOR = 0;
    private static final int START_MOTOR = 1;

    public Cabin(int elevatorID, SoftwareBus softwareBus){
        //TODO call subscribe on softwareBus w/ relevant topic/subtopic

        this.softwareBus = softwareBus;
        this.currDest = 0;
        this.currDirection = Direction.STOPPED;
        this.elevatorID = elevatorID;

        //Start Cabin Thread
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Run the Cabin
     */
    @Override
    public void run() {
        //Todo: papa bird, mama bird! Check if the alignment logic is right please please
        while (true) {
            stepTowardsDest();
            System.out.println(""); // <- why?
        }
    }

    /**
     * sets the direction and the destination
     * @param floor the target floor
     */
    public void gotoFloor(int floor){
        if(floor > currFloor) currDirection = Direction.UP;
        else if (floor < currFloor) currDirection = Direction.DOWN;
        else currDirection = Direction.STOPPED;
        currDest = floor;
    }

    /**
     * @return the floor and direction of the elevator
     */
    public FloorNDirection currentStatus(){
        // Todo: ok we handle motor moving in house and dont have a method to check if the motor is moving??
        Message floorMessage = MessageHelper.pullAllMessages(softwareBus, elevatorID, TOPIC_CABIN_POSITION);
        Message directionMessage = MessageHelper.pullAllMessages(softwareBus, elevatorID, TOPIC_CURR_DIRECTION);
        currFloor = floorMessage.getBody();
        int annoyingAF = directionMessage.getBody();
        switch (annoyingAF){
            case 0 -> currDirection = Direction.UP;
            case 1 -> currDirection = Direction.DOWN;
            case 2 -> currDirection = Direction.STOPPED;
        }
        return new FloorNDirection(currFloor,currDirection);}

    /**
     * @return true if the elevator has arrived at its destination
     */
    public boolean arrived(){return currFloor == currDest;}

    /**
     * @return the current target floor
     */
    public int getTargetFloor(){return currDest;}


    // Internal methods

    private Timer timeToStop;

    /**
     * Main thread method, used to step towards a target floor
     */
    private synchronized void stepTowardsDest() {
        //Update alignment
        topAlignment();
        bottomAlignment();
        //Last sensor before stop
        boolean almostThere;
        if (currDirection == Direction.UP) almostThere = sensorToFloor(botAlign) == currDest;
        else almostThere = sensorToFloor(topAlign) == currDest;
        //Should time stop
        if (motor && almostThere) {
            //Time to stop!
            if (timeToStop != null && timeToStop.timeout()) stopMotor();
            //Determine time to stop
            else if (timeToStop == null) timeToStop = timeStop();
        } else if (!motor){
            //Turn motor on if needed
            if (currFloor > currDest) currDirection = Direction.DOWN;
            else currDirection = Direction.UP;
            startMotor(currDirection);
        } else {
            //Reset time to stop
            timeToStop = null;
        }
    }
    private int closestFloor() {
        return -69420;
    }

    /**
     * Updates the correct time to stop the motor based on constants file
     * @return a timer that times out when it's time to stop
     */
    private Timer timeStop() {
        return new Timer(ConstantsElevatorControl.TIME_TO_STOP);
    }


    /**
     * Translates sensor to floor
     * @param sensorPos a sensor position to a floor #
     * @return a floor number 1-20
     */
    private int sensorToFloor(int sensorPos) {
        return sensorPos/2 + 1;
    }


    //Wrapper methods for software bus messages
    private void startMotor(Direction direction) {
        //We set the direction number based on current mux 11/23/2025
        motor = true;
        int dir = -1;
        switch (direction) {
            case UP -> dir = 0;
            case DOWN -> dir = 1;
            case STOPPED -> dir = -1;
        }
        softwareBus.publish(new Message(TOPIC_CAR_DISPATCH, elevatorID, dir));
    }

    private void stopMotor() {
        motor = false;
        softwareBus.publish(new Message(TOPIC_CAR_STOP, elevatorID, STOP_MOTOR));
    }

    private void topAlignment() {
       Message message = MessageHelper.pullAllMessages(softwareBus, elevatorID, TOPIC_TOP_FLOOR_SENSOR);
       if (message != null) topAlign = message.getBody();
    }
    private void bottomAlignment() {
        Message message = MessageHelper.pullAllMessages(softwareBus, elevatorID, TOPIC_BOTTOM_SENSOR);
        if (message != null) botAlign = message.getBody();
    }

}