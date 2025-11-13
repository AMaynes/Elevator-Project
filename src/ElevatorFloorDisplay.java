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
    // The ID of the associated elevator
    private final int carId;

    // Optional GUI listener
    private static gui.listener guiListener = null;

    /**
     * Sets the ElevatorFloorDisplay's guiListener.
     * @param l The ElevatorFloorDisplay's guiListener.
     */
    public static void setGuiListener(gui.listener l) {
        guiListener = l;
    }

    /**
     * Constructs the ElevatorFloorDisplay.
     * @param carId the ID of the associated elevator
     */
    public ElevatorFloorDisplay(int carId) {
        this.carId = carId;
        this.currentFloor = 1;
        this.direction = "IDLE";
    }

    /**
     * Updates the display to show the elevator's current floor and direction.
     * @param currentFloor the floor currently displayed
     * @param direction the direction the elevator is going
     */
    public void updateFloorIndicator(int currentFloor, String direction) {
        this.currentFloor = currentFloor;
        this.direction = direction;
        System.out.println("[Display]");
        if (guiListener != null) guiListener.notify("FloorDisplay.update",
                currentFloor + ":" + direction);
        DeviceMultiplexor.getInstance().onDisplaySet(carId, currentFloor + " " + direction);
        DeviceMultiplexor.getInstance().emitCarPosition(carId, currentFloor, direction);
    }

    /**
     * Simulates the arrival noise.
     */
    public void playArrivalChime() {
        // again simulating the Ding noise
        System.out.println("*Ding! Elevator has arrived at floor");
        if (guiListener != null) guiListener.notify("FloorDisplay.arrivalChime", Integer.toString(currentFloor));
    }

    /**
     * Simulates the overload buzz.
     */
    public void playOverLoadWarning() {
        // simulating the buzzing noise
        System.out.println("*Buzz! Warning: Overload detected at floor" + currentFloor);
        if (guiListener != null) guiListener.notify("FloorDisplay.overloadWarning", Integer.toString(currentFloor));
    }

    /**
     * TODO: Remove.
     * this next functions I'm just going to add just in case
     * we need them in the future they just return direction
     * and the current floor.
     */
    public int getCurrentFloor() {
        return currentFloor;
    }

    public String getDirection() {
        return direction;
    }
}
