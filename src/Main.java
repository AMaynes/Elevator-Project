import Bus.SoftwareBus;
import ElevatorController.HigherLevel.ElevatorMain;

public class Main {
    public static void main(String[] args) {
        int elevator1 = 1;
        int elevator2 = 2;
        int elevator3 = 3;
        int elevator4 = 4;
        SoftwareBus clientBus = new SoftwareBus(false);
        ElevatorMain em1 = new ElevatorMain(elevator1, clientBus);
        ElevatorMain em2 = new ElevatorMain(elevator2, clientBus);
        ElevatorMain em3 = new ElevatorMain(elevator3, clientBus);
        ElevatorMain em4 = new ElevatorMain(elevator4, clientBus);
    }
}