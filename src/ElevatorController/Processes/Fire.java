package ElevatorController.Processes;

import ElevatorController.LowerLevel.*;
import ElevatorController.Util.State;

/**
 * In fire mode, the elevator only listens to request buttons in the cabin if the fire key has been inserted.
 * Only one service button can be lit up at a time.
 * If two buttons are pressed, the most recently pressed button is the only service request.
 */
public class Fire {
    /**
     * Fire Process
     * @param mode the mode lower level object
     * @param buttons the buttons lower level object
     * @param cabin the cabin lower level object
     * @param doorAssembly the door assembly lower level object
     * @param notifier the notifier lower level object
     * @return
     */
    public static State fire(Mode mode, Buttons buttons, Cabin cabin,
                             DoorAssembly doorAssembly, Notifier notifier){
        //In fire mode, move all elevators down to floor one
        //Mode gets fire key message

        //buttons handles fire key
        //if single calls are enable then the fire key is inserted
        buttons.disableCalls();

        //Close doors
        while(!ProcessesUtil.closeDoors(doorAssembly)){
            notifier.playCapacityNoise();
        }
        //TODO: if key inserted...do something else?
        //Go to first floor
        cabin.gotoFloor(1);


        while (!cabin.arrived()){
            System.out.println("Yeoowchhhhhh");
        }
        //Open all doors
        doorAssembly.open();



        State currentState=mode.getMode();

        return currentState;
    }


}
