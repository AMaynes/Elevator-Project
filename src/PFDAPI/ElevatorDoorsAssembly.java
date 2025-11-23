package PFDAPI;

import PFDGUI.gui;

/**
 * Class that defines the functionality of the Elevator doors. Represents
 * the pair of doors that open to a specific elevator on each floor.
 * API:
 *      public void open()
 *      public void close()
 *      public boolean isObstructed()
 *      public boolean isFullyOpen()
 *      public boolean isFullyClosed()
 */
public class ElevatorDoorsAssembly {
    private boolean isOpen;
    private boolean isClosed;
    // True when an obstruction is placed
    private boolean isObstructed;
    // Represents whether the doors are actively opening/closing
    private boolean isMoving;

    // GUI Control reference
    private final gui.GUIControl guiControl;
    // Car ID reference
    private final int carId;

    /**
     * Constructor of the ElevatorDoorsAssembly.
     */
    public ElevatorDoorsAssembly(int carId, gui.GUIControl guiControl) {
        this.carId = carId;
        this.guiControl = guiControl;
        this.isOpen = true;
        this.isClosed = false;
        this.isObstructed = false;
        this.isMoving = false;
    }

    /**
     * Commands the door assembly to open.
     * If an obstruction is detected, opening is halted automatically.
     */
    public synchronized void open() {
        // Do NOT start a new open if moving or obstructed
        if (isMoving) {
            System.out.println("[Doors] Cannot open - door is currently moving.");
            return;
        }
        if (isObstructed) {
            System.out.println("[Doors] Cannot open - obstruction detected.");
            return;
        }

        if (!isOpen) {
            isMoving = true;
            System.out.println("[Doors] Opening...");
            simulateDelay(500);
            isOpen = true;
            guiControl.changeDoorState(carId, true); // true = open
            isMoving = false;
            System.out.println("[Doors] Fully open.");
        }
    }


    /**
     * Commands the door assembly to close.
     * If obstruction occurs during closing, doors reopen automatically.
     */
    public synchronized void close() {
        if (isObstructed) {
            System.out.println("[Doors] Obstruction detected reopening.");
            isOpen = true;
            open();
            return;
        }

        if (isOpen) {
            isMoving = true;
            isClosed = false;
            System.out.println("[Doors] Closing...");

            simulateDelay(200);    // small delay before animation
            guiControl.changeDoorState(carId, false);

            // Now wait for the GUI to finish closing (2 seconds)
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                synchronized (this) {
                    if (!isObstructed) {
                        isOpen = false;
                        isClosed = true;
                        System.out.println("[Doors] Fully closed.");
                    }
                    isMoving = false;
                }
            }).start();
        }
    }

    /**
     * Simulates time delay for the open and close animations. For simulation purposes.
     * @param ms time to be elapsed
     */
    private synchronized void simulateDelay(long ms) {
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
    public synchronized boolean isObstructed() {
        isObstructed = guiControl.getIsDoorObstructed(carId);
        return isObstructed;
    }

    /**
     * Sets obstruction state manually for simulation/testing.
     * @param obstructed whether the GUI has the obstruction box present
     */
    public synchronized void setObstruction(boolean obstructed) {
        this.isObstructed = obstructed;
        guiControl.setDoorObstruction(carId, obstructed);
    }

    /**
     * Returns whether the doors are completely open (not closed, not half-open).
     * Elevator cannot be allowed to move.
     * @return boolean isOpen
     */
    public synchronized boolean isFullyOpen() {
        return isOpen;
    }

    /**
     * Returns whether the doors are completely closed (not open, not half-open).
     * Elevator can now move.
     * @return boolean isOpen
     */
    public synchronized boolean isFullyClosed() {
        return isClosed;
    }


}
