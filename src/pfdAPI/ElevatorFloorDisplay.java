package pfdAPI;

import pfdGUI.gui;

/**
 * Class that defines the functionality of the Elevator floor displays. Represents
 * the panel above elevator doors that show the elevator's location and direction of movement.
 * API:
 *      public void updateFloorIndicator(int currentFloor, String direction)
 *      public void playArrivalChime()
 *      public void playOverLoadWarning()
 * For GUI purposes:
 *      public static void setGuiListener(gui.listener l)
 */
public class ElevatorFloorDisplay {
    // The current floor location of the elevator
    private int currentFloor;
    // The current movement direction of the elevator, "UP" "DOWN" "IDLE"
    private String direction;
    // GUI Control reference
    private final gui.GUIControl guiControl;
    // The ID of the associated elevator
    private final int carId;

    /**
     * Constructs the ElevatorFloorDisplay.
     * @param carId the ID of the associated elevator
     */
    public ElevatorFloorDisplay(int carId, gui.GUIControl guiControl) {
        this.carId = carId;
        this.guiControl = guiControl;
        this.currentFloor = 1;
        this.direction = "IDLE";
    }

    /**
     * Updates the display to show the elevator's current floor and direction.
     * @param currentFloor the floor currently displayed
     * @param direction the direction the elevator is going (UP/DOWN/IDLE)
     */
    public synchronized void updateFloorIndicator(int currentFloor, String direction) {
        this.currentFloor = currentFloor;
        this.direction = direction;
        guiControl.setDisplay(carId, currentFloor, direction);
    }

    /**
     * Simulates the arrival noise.
     */
    public synchronized void playArrivalChime() {
        // again simulating the Ding noise
        System.out.println("*Ding! Elevator has arrived at floor");
    }

    /**
     * Simulates the overload buzz.
     */
    public synchronized void playOverLoadWarning() {
        // simulating the buzzing noise
        System.out.println("*Buzz! Warning: Overload detected at floor" + currentFloor);
    }

    public boolean isOverloaded(){
        return guiControl.getIsCabinOverloaded(carId);
    }
}
