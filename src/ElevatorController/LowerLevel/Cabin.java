package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import ElevatorController.Util.ConstantsElevatorControl;
import ElevatorController.Util.Direction;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.Timer;

/**
 * The cabin provides a means for the elevator controller to send the elevator to a destination.
 * The cabin indirectly controls the motor by sending messages to the Software Bus.
 * Additionally, the cabin indirectly receives messages from physical sensors through the Software Bus.
 */
public class Cabin implements Runnable {
    private int currDest;
    private Direction currDirection;
    private int currFloor;
    private int topAlign;
    private int botAlign;
    private boolean motor;
    private SoftwareBus softwareBus;

    public Cabin(int elevatorID, SoftwareBus softwareBus){
        //TODO may need to take in int for elevator number for software bus subscription
        //TODO call subscribe on softwareBus w/ relevant topic/subtopic

        this.softwareBus = softwareBus;
        this.currDest = 0;
        this.currDirection = Direction.STOPPED;

        //Start Cabin Thread
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Run the Cabin
     */
    @Override
    public void run() {
        while (true) {
            stepTowardsDest();
            System.out.println("");
        }
    }

    public void gotoFloor(int floor){
        currDest = floor;
    }
    public FloorNDirection currentStatus(){return new FloorNDirection(currFloor,currDirection);}
    public boolean arrived(){return currFloor == currDest;}
    public int getTargetFloor(){return currDest;}


    // Internal methods

    private Timer timeToStop;

    /**
     * Main thread method, used to step towards a target floor
     */
    private synchronized void stepTowardsDest() {
        //Update alignment
        topAlign = topAlignment();
        botAlign = bottomAlignment();
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
        motor = true;
        //TODO: send message
    }

    private void stopMotor() {
        motor = false;
        //TODO: your sister
    }

    private int topAlignment() {
        //TODO: get message from software bus
        return 0;
    }
    private int bottomAlignment() {
        //TODO: get message from software bus
        return 0;
    }

}