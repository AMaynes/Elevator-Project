package ElevatorController.Processes;

import ElevatorController.LowerLevel.*;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.State;

/**
 * Control mode is one where movement is controlled by the Control Room. Using
 * the Software Bus. The Control Room can give commands to the elevators and
 * assumes full control over the system.
 */
public class Control {
    /**
     * Create an instance of the Fire Procedure
     * @param mode the mode lower level object
     * @param buttons the buttons lower level object
     * @param cabin the cabin lower level object
     * @param doorAssembly the door assembly lower level object
     * @param notifier the notifier lower level object
     */

    /**
     * Control mode implementation
     * @return The state to switch too (normal or fire)
     */
    public static State control(Mode mode, Buttons buttons, Cabin cabin,
                                DoorAssembly doorAssembly, Notifier notifier){
        //Check if control
        if (mode.getMode() != State.CONTROL) return mode.getMode();

        //Prepare for use
        buttons.disableCalls();
        buttons.disableAllRequest();
        FloorNDirection currentService = null;
        boolean alreadyServiced = false;

        //Process Requests until state changes
        while (mode.getMode() == State.CONTROL) {
            //get next service
            if (currentService == null) {
                currentService = mode.nextService();
            }
            //go to floor of current service
            if (currentService != null){
                if(ProcessesUtil.doorClose(doorAssembly,notifier)) {
                    cabin.gotoFloor(currentService.floor());
                    alreadyServiced = false;
                }
            }
            //arrive (open doors, wait,)
            if (cabin.arrived() && currentService != null && !alreadyServiced) {
                ProcessesUtil.arriveProcess(buttons,doorAssembly,notifier,currentService);
                currentService = null;
                alreadyServiced = true;
            }
        }
        //Exit mode
        return mode.getMode();
    }
}
