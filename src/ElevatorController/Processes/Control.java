package ElevatorController.Processes;

import ElevatorController.LowerLevel.*;
import ElevatorController.Util.State;

/**
 * Control mode is one where movement is controlled by the Control Room.
 * Using the Software Bus. The Control Room can give commands to the elevators and assumes full control over the system.
 */
public class Control {
    /**
     * @param mode the mode lower level object
     * @param buttons the buttons lower level object
     * @param cabin the cabin lower level object
     * @param doorAssembly the door assembly lower level object
     * @param notifier the notifier lower level object
     * @return
     */
    public static State control(Mode mode, Buttons buttons, Cabin cabin,
                                DoorAssembly doorAssembly, Notifier notifier){
        return State.NULL;}
}
