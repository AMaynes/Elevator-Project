/**
 * Class that defines the functionality of the Elevator doors. Represents
 * the pair of doors that open to a specific elevator on each floor.
 * API:
 *      public void open()
 *      public void close()
 *      public boolean isObstructed()
 *      public boolean isFullyOpen()
 *      public boolean isFullyClosed()
 * For GUI purposes:
 *      public void setObstruction(boolean obstructed)
 *      public static void setGuiListener(gui.listener l)
 */
public class ElevatorDoorsAssembly {
    // False when the doors are fully closed
    private boolean isOpen;
    // True when an obstruction is placed
    private boolean isObstructed;
    // Represents whether the doors are actively opening/closing
    private boolean isMoving;
    // The ID of the associated elevator
    private int carId;

    // Optional GUI listener
    private static gui.listener guiListener = null;

    /**
     * Sets the ElevatorDoorsAssembly's guiListener.
     * @param l The ElevatorDoorsAssembly's guiListener.
     */
    public static void setGuiListener(gui.listener l) {
        guiListener = l;
    }

    /**
     * Constructor of the ElevatorDoorsAssembly.
     * @param carId The ID of the associated elevator
     */
    public ElevatorDoorsAssembly(int carId) {
        this.isOpen = false;
        this.isObstructed = false;
        this.isMoving = false;
        this.carId = carId;

    }

    /**
     * Commands the door assembly to open.
     * If an obstruction is detected, opening is halted automatically.
     * TODO: An obstruction should make opening a MANDATORY action, rather than halting it
     */
    public void open(){
        if(isObstructed) {
            System.out.println("[Doors} Cannot open - obstruction detected.");
            if (guiListener != null) guiListener.notify("Doors.openAttemptBlocked", null);
            return;
        }
        if (!isOpen) {
            isMoving = true;
            System.out.println("[Doors] Opening...");
            if (guiListener != null) guiListener.notify("Doors.opening", null);
            DeviceMultiplexor.getInstance().notifyDoorChanged(carId, "OPENING");
            simulateDelay(2000);
            isOpen = true;
            isMoving = false;
            System.out.println("[Doors] Fully open.");
            if (guiListener != null) guiListener.notify("Doors.opened", null);
            DeviceMultiplexor.getInstance().notifyDoorChanged(carId, "OPENED");

        }
    }

    /**
     * Commands the door assembly to close.
     * If obstruction occurs during closing, doors reopen automatically.
     */
    public void close() {
        if (isObstructed) {
            System.out.println("[Doors] obstruction detected reopening.");
            if (guiListener != null) guiListener.notify("Doors.closeBlockedObstruction", null);
            DeviceMultiplexor.getInstance().emitDoorSensor(carId, true);
            open();;
            return;
        }
        if (isOpen) {
            isMoving = true;
            System.out.println("[Doors] Closing...");
            if (guiListener != null) guiListener.notify("Doors.closing", null);
            DeviceMultiplexor.getInstance().notifyDoorChanged(carId, "CLOSING");
            simulateDelay(1000);
            if (!isObstructed) {
                isOpen = false;
                System.out.println("[Doors] Fully closed.");
                if (guiListener != null) guiListener.notify("Doors.closed", null);
                DeviceMultiplexor.getInstance().notifyDoorChanged(carId, "CLOSED");
            } else {
                System.out.println("[Doors] Reopening due to obstruction.");
                open();
            }
            isMoving = false;
        }
    }

    /**
     * Simulates time delay for the open and close animations. For simulation purposes.
     * @param ms time to be elapsed
     */
    private void simulateDelay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns whether the doors are currently obstructed.
     * @return boolean isObstructed
     */
    public boolean isObstructed() {
        return isObstructed;
    }

    /**
     * Sets obstruction state manually for simulation/testing.
     * TODO: Make sure this isn't possible when the doors are fully closed in the mult
     * @param obstructed whether the GUI has the obstruction box present
     */
    public void setObstruction(boolean obstructed) {
        this.isObstructed = obstructed;
        if (guiListener != null) guiListener.notify("Doors.obstructionSet", Boolean.toString(obstructed));
        DeviceMultiplexor.getInstance().emitDoorSensor(carId, obstructed);

    }

    /**
     * Returns whether the doors are completely open (not closed, not half-open).
     * Elevator cannot be allowed to move.
     * @return boolean isOpen
     */
    public boolean isFullyOpen() {
        return isOpen;
    }

    /**
     * Returns whether the doors are completely closed (not open, not half-open).
     * Elevator can now move.
     * TODO: This returns true when the doors are half-open. This should not be possible. Need separate variables.
     * @return boolean isOpen
     */
    public boolean isFullyClosed() {
        return !isOpen;
    }


}
