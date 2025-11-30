package CommandCenterNew;

import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import ElevatorController.Util.Direction;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.State;
import Message.Message;

public class CommandCenter {

    //Given to us by a startup
    public SoftwareBus bus;

    //TODO: Update with actual codes

    private static final int SET_MODE= 1;

    private static final int ELEVATOR_STATUS= 2; //TODO

    private static final int DOOR_STATUS=0; //TODO

    private static final int START_ELEVATOR=3;

    private static final int STOP_ELEVATOR=4;

    private static final int SERVICE_MESSAGE = 5;

    private static final int GET_MODE = 6;
    private static final int GET_ELEVATOR_STATUS = 7;
    private static final int GET_DOOR_STATUS = 8;
    private State currMode = State.NORMAL;

    public CommandCenter(SoftwareBus bus){
        this.bus=bus;


        bus.subscribe(SET_MODE,0);
        bus.subscribe(ELEVATOR_STATUS, 0);
        bus.subscribe(DOOR_STATUS,0);

    }

    /**
     * Publishes starts single elevator
     */
    public void enableSingleElevator(int elevatorId){
        bus.publish(new Message(START_ELEVATOR,elevatorId,0));
    }

    /**
     * Publishes start all elevators
     */
    public void enableElevator(){
        bus.publish(new Message(START_ELEVATOR,0,0));
    }

    /**
     * Due to the way the command center GUI is set up, we added the functionality
     * to stop/start single elevators
     * @param elevatorId the elevator to be stoped
     */
    public void disableSingleElevator(int elevatorId){
        //The body has no meaning
        bus.publish(new Message(STOP_ELEVATOR,elevatorId,0));
    }

    /**
     * Stops all elevators
     */

    public void disableElevator(){
        bus.publish(new Message(STOP_ELEVATOR,0,0));
    }

    /**
     * Turns off fire message, sent to all elevators, with a dummy body
     */
    public void clearFireMessage() {
        bus.publish(new Message(SoftwareBusCodes.clearFire,0,0));
    }



    /**
     * Send mode message
     */
    public void sendModeMessage(int modeMessage){
        bus.publish(new Message(SET_MODE,0,modeMessage));
    }

    /**
     * Send Service Message
     */
    public void sendServiceMessage(int elevatorID, int floor) {
        bus.publish(new Message(SERVICE_MESSAGE,elevatorID,floor));
    }
    /**
     * Gets a new mode from software bus if there is one, otherwise returns current mode
     */
    private State getMode() {
        //hard coded for fire alarm, would need to handle other cases if we are told to switch to other modes
        if (bus.get(GET_MODE,0) != null) return State.FIRE;
        return currMode;
    }

    /**
     * Returns the current elevator status
     * @param id of the elevator
     * @return current floor and motion of the elevator
     */
    private FloorNDirection getElevatorStatus(int id) {
        int message = bus.get(GET_ELEVATOR_STATUS,id).getBody();
        if (message > 200) return new FloorNDirection(message-200,Direction.UP);
        if (message > 100) return new FloorNDirection(message-100,Direction.STOPPED);
        return new FloorNDirection(message,Direction.DOWN);
    }

    /**
     * Gets the door status of specified elevator
     * @param id of the elevator
     * @return 0 for open, 1 for closed, -1 error
     */
    private int getDoorStatus(int id) {
        int message = bus.get(GET_DOOR_STATUS,id).getBody();
        if (message == 0 || message == 1) return message;
        return -1;
    }



    /**
     *
     * @param message
     */

    private void handleMessage(Message message){
        // Command center should listen for new modes, elevator status and door status
        int topic =message.getTopic();
        int subTopic= message.getSubTopic();
        int body = message.getBody();

//        switch(topic){
//            case SET_MODE->
//
//        }
    }


}
