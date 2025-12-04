package ElevatorController.Processes;

import ElevatorController.LowerLevel.Buttons;
import ElevatorController.LowerLevel.DoorAssembly;
import ElevatorController.LowerLevel.Notifier;
import ElevatorController.Util.ConstantsElevatorControl;
import ElevatorController.Util.FloorNDirection;
import ElevatorController.Util.Timer;

import static ElevatorController.Util.ConstantsElevatorControl.DOOR_CLOSE_TIMEOUT;

public class ProcessesUtil {
    /**
     * Method for elevator arrival
     * @param buttons buttons to reset
     * @param doorAssembly doors to open/close
     * @param notifier notifier for elevator closure
     * @param currentRequest the current request to reset
     */
    public static void arriveProcess(Buttons buttons, DoorAssembly doorAssembly,
                                     Notifier notifier, FloorNDirection currentRequest) {
        buttons.callReset(currentRequest);
        notifier.arrivedAtFloor(currentRequest); // Play arrival sound
        doorAssembly.open();
        while (!ProcessesUtil.tryDoorOpen(doorAssembly)) ;
        ProcessesUtil.DoorsOpenWait();
    }
    /**
     * Holds the door open for specified time
     */
    public static void DoorsOpenWait() {
        try {
            Thread.sleep(ConstantsElevatorControl.DOOR_OPEN_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempt to open the doors
     * @param doorAssembly the doors to open
     * @return true if doors open successfully. Should always return successful
     * if timeout is not too little
     */
    public static boolean tryDoorOpen(DoorAssembly doorAssembly) {
        doorAssembly.open();
        Timer timer =new Timer(DOOR_CLOSE_TIMEOUT);
        while (!doorAssembly.fullyOpen()) {
            if (timer.timeout()) return false;
        }
        return true;
    }

    /**
     * Hang thread until doors close fully. Plays over-capacity noise if they take too long
     * @param doorAssembly the doors to close
     * @param notifier the notifier to play capacity noise
     * @return true if the doors closed without over capacity noise
     */
    public static boolean doorClose(DoorAssembly doorAssembly, Notifier notifier) {
        boolean success;
        success = ProcessesUtil.tryDoorClose(doorAssembly,notifier, false);
        if (success) return true;
        while(!success){
            success = ProcessesUtil.tryDoorClose(doorAssembly,notifier, true);
        }
        return false;
    }

    /**
     * Attempts to close the doors but opens if there's an obstruction
     * @param doorAssembly the doors to close
     * @param notifier the notifier to play capacity noise
     * @param capacity true if over time limit, then plays capacity noise for door obstruction
     * @return true if doors successfully close before timeout
     */
    public static boolean tryDoorClose(DoorAssembly doorAssembly, Notifier notifier, boolean capacity){
        // Early return if doors are already fully closed
        if(doorAssembly.fullyClosed()){
            return true;
        }
        
        System.out.println("[DoorClose] Starting door close sequence");
        Timer timer =new Timer(DOOR_CLOSE_TIMEOUT);
        boolean alreadyPlayedNoise = false;

        boolean lastCommand = true;
        while(!doorAssembly.fullyClosed()){
            if(doorAssembly.obstructed()){
                if (capacity && !alreadyPlayedNoise) {
                    alreadyPlayedNoise = true;
                    notifier.playCapacityNoise();
                }
                if(lastCommand == false){
                    doorAssembly.open();
                    lastCommand = true;
                }
            }
            else if (doorAssembly.overCapacity()) {
                if(lastCommand == false) {
                    doorAssembly.open();
                    lastCommand = true;
                }
                if(!alreadyPlayedNoise){
                    notifier.playCapacityNoise();
                    alreadyPlayedNoise = true;
                }
            }
            else {
                if(lastCommand) {
                    System.out.println("Trying to close doors");
                    doorAssembly.close();
                    lastCommand = false;
                }
            }
            if(timer.timeout()&&!doorAssembly.fullyClosed()){
                System.out.println("[DoorClose] Timeout reached, doors not fully closed");
                return false;
            }
            // Small sleep to prevent excessive CPU usage and logging
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("[DoorClose] Doors successfully closed");
        return true;
    }
}
