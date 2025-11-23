package ElevatorController.Processes;

import ElevatorController.Util.State;

/**
 * Control mode is one where movement is controlled by the Control Room.
 * Using the Software Bus. The Control Room can give commands to the elevators and assumes full control over the system.
 */
public class Control {
    public static State control(){return State.NULL;};
}
