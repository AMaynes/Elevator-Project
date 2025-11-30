package CommandCenterNew;

import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import ElevatorController.Util.Direction;
import ElevatorController.Util.FloorNDirection;
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
     *
     */




    /**
     *
     * @param message
     */

    private void handleMessage(Message message){
        // Command center should listen for new modes, elevator status and door status
        int topic =message.getTopic();
        int subTopic= message.getSubTopic();
        int body = message.getBody();

        switch(topic){
            case SET_MODE->

        }
    }


}
