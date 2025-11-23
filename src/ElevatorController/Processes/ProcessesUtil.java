package ElevatorController.Processes;

import ElevatorController.LowerLevel.DoorAssembly;
import ElevatorController.Util.Timer;

import static ElevatorController.Util.ConstantsElevatorControl.DOOR_CLOSE_TIMEOUT;

public class ProcessesUtil {


    public static boolean closeDoors(DoorAssembly doorAssembly){
        Timer timer =new Timer(DOOR_CLOSE_TIMEOUT);

        while(!doorAssembly.fullyClosed()){
            if(doorAssembly.obstructed()){
                doorAssembly.open();
            }else{
                doorAssembly.close();

            }
            if(timer.timeout()&&!doorAssembly.fullyClosed()){
                return false;
            }

        }
        return true;

    }
}
