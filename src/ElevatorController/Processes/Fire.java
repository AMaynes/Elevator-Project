package ElevatorController.Processes;

import ElevatorController.LowerLevel.*;
import ElevatorController.Util.FloorNDirection;
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
        if(buttons.isFireKeyActive()){
            buttons.enableSingleRequest();
        } else {
            buttons.disableAllRequest();
        }

        FloorNDirection fireKeyService = null;
        boolean fireKeyState;
        boolean lastFKState =  false;
        boolean alreadyServiced = false;

        //Listen to requests if fire key is inserted, otherwise go to first floor
        while (mode.getMode() == State.FIRE) {
            fireKeyState = buttons.isFireKeyActive();
            if(fireKeyState && lastFKState != fireKeyState){
                buttons.enableSingleRequest();
            } else if (lastFKState != fireKeyState){
                buttons.disableAllRequest();
            }
            lastFKState = fireKeyState;

            //Get any services enabled by fire key
            if (fireKeyService == null && fireKeyState){
                fireKeyService = buttons.nextService(cabin.currentStatus());
            }
            //Process services enabled by fire key
            if (fireKeyService != null) {
                if(ProcessesUtil.doorClose(doorAssembly,notifier)){
                    cabin.gotoFloor(fireKeyService.floor());
                    alreadyServiced = false;
                }
            } else if (!fireKeyState && !alreadyServiced) {
                if(ProcessesUtil.doorClose(doorAssembly,notifier)){
                    cabin.gotoFloor(1);
                    alreadyServiced = false;
                }
            }
            //Arrival process (open doors)
            if (cabin.arrived() && !alreadyServiced) {
                ProcessesUtil.arriveProcess(buttons, doorAssembly, notifier, fireKeyService);
                fireKeyService = null;
                alreadyServiced = true;
            }
        }
        //Return exit mode
        return mode.getMode();
    }
}
