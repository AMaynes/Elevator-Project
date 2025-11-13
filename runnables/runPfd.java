import javafx.application.Application;
import java.math.*;
import java.util.Random;

/**
 * Old on-rails PFD demo testing code.
 */
public class runPfd {
    public static void main(String[] args) throws InterruptedException {
        DeviceMultiplexor mux = DeviceMultiplexor.getInstance();
        Random rand = new Random();

        // Start GUI
        Thread guiThread = new Thread(() -> Application.launch(gui.class));
        guiThread.start();
        Thread.sleep(3000); // Wait for GUI to initialize

        int startingFloor = 6;
        int carId = 1;
        int currentFloor = startingFloor;

        // API Calls
        mux.onDisplaySet(carId, startingFloor + " IDLE");
        mux.onDisplaySet(carId, startingFloor + " IDLE");
        Thread.sleep(2000);
        mux.emitHallCall(startingFloor, "UP");
        Thread.sleep(2000);
        mux.notifyCallReset(startingFloor);
        Thread.sleep(2000);
        for(int n = 0; n < 5; n++) {
            int i = rand.nextInt(10);
            Thread.sleep(2000);
            mux.onDoorCON(carId, 1);
            Thread.sleep(2000);
            mux.emitDoorSensor(carId, true);
            Thread.sleep(2000);
            if(rand.nextInt(10) > 5) mux.onModeSet(carId, "OVERLOAD"); // 50% chance to simulate weight overload call
            Thread.sleep(2000);
            mux.onDoorCON(carId, 2);  // Should detect obstruction and reopen
            Thread.sleep(2000);
            mux.emitDoorSensor(carId, false);
            Thread.sleep(2000);
            mux.onDoorCON(carId, 2);  // Now closes successfully
            Thread.sleep(2000);

            // Simulate passing floors
            for(int j = 0; j < 3; j++) {
                if(i > currentFloor) {
                    currentFloor++;
                    mux.onDisplaySet(carId, currentFloor + " UP");
                    mux.onDisplaySet(carId, currentFloor + " UP");
                } else if(i < currentFloor) {
                    currentFloor--;
                    mux.onDisplaySet(carId, currentFloor + " DOWN");
                    mux.onDisplaySet(carId, currentFloor + " DOWN");
                } else {
                    continue;
                }
                Thread.sleep(1000);
            }

            mux.emitCabinSelect(carId, i);
            Thread.sleep(2000);
            mux.onDisplaySet(carId, i + " IDLE");
            mux.onDisplaySet(carId, i + " IDLE");
        }
    } 
}
