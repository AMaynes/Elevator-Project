import java.util.ArrayList;
import java.util.List;

/**
 * Device inside of elevators that allows for user-interaction. Allows cabin riders to
 * select destination floors, view the elevator's current floor and direction, and
 * interact with the fire key.
 * API:
 *      public List<Integer> getPressedFloors()
 *      public void clearPressedFloors()
 *      public void resetFloorButton(int floorNumber)
 *      public void setDisplay(int currentFloor, String direction)
 *      public void playCabinArrivalChime()
 *      public void playCabinOverloadWarning()
 *      public boolean isFireKeyActive()
 * For GUI purposes:
 *      public void pressFloorButton(int floorNumber)
 *      public void toggleFireKey()
 *      public static void setGuiListener(gui.listener l)
 */
class CabinPassengerPanel implements CabinPassengerPanelAPI {

    // Optional GUI listener (static so the simple GUI can register once).
    private static gui.listener guiListener = null;

    /**
     * Sets the CabinPassengerPanel's guiListener.
     * @param l The CabinPassengerPanel's guiListener.
     */
    public static void setGuiListener(gui.listener l) {
        guiListener = l;
    }

    // The total number of floors (=10)
    private final int totalFloors;
    // Array of the pressed state of each floor button. True when customer presses,
    // requesting service
    private final boolean[] floorButtons;
    // Array of the queue of pressed floor buttons
    private final List<Integer> pressedFloorsQueue;
    // The current floor the elevator is on/nearest
    private int currentFloor;
    // The current direction the elevator is moving in. "UP" "DOWN" and "IDLE
    private String direction;
    // State of the fire key; is active/is not active
    private boolean fireKeyActive;
    // The ID of the elevator the passenger panel belongs to
    private final int carId;

    /**
     * Constructor of the CabinPassengerPanel.
     * @param carId The elevator housing the panel
     * @param totalFloors Number of floors in the building (=10)
     */
    public CabinPassengerPanel(int carId, int totalFloors) {
        this.carId = carId;
        this.totalFloors = totalFloors;
        this.floorButtons = new boolean[totalFloors];
        this.pressedFloorsQueue = new ArrayList<>();
        this.currentFloor = 1;
        this.direction = "IDLE";
        this.fireKeyActive = false;
    }

    /**
     * Function for simulating pressing a floor button.
     * @param floorNumber The floor being requested
     */
    public void pressFloorButton(int floorNumber) {
        if (floorNumber >= 1 && floorNumber <= totalFloors && !floorButtons[floorNumber - 1]) {
            floorButtons[floorNumber - 1] = true;
            pressedFloorsQueue.add(floorNumber);
            if (guiListener != null) guiListener.notify("Cabin.pressFloorButton",
                    Integer.toString(floorNumber));
            DeviceMultiplexor.getInstance().emitCabinPanelClick(carId, carId - 1,
                    floorNumber);
            DeviceMultiplexor.getInstance().emitCabinSelect(carId, floorNumber);
        }
    }

    /**
     * Returns all pressed floor numbers since the last poll.
     * Requests made by the riders must be serviced when not in emergency mode.
     * @return copy of ArrayList<Integer> pressedFloorsQueue
     */
    @Override
    public List<Integer> getPressedFloors() {
        if (guiListener != null) guiListener.notify("Cabin.getPressedFloors", null);
        return new ArrayList<>(pressedFloorsQueue);
    }

    /**
     * Clears all stored pressed floor events. Called upon the suspension of
     * regular activities (emergency mode).
     */
    @Override
    public void clearPressedFloors() {
        pressedFloorsQueue.clear();
        if (guiListener != null) guiListener.notify("Cabin.clearPressedFloors", null);
    }

    /**
     * Resets a specific floor button's indicator. Resets occur after the travel
     * request has been serviced.
     * @param floorNumber the reset floor button's associated floor
     */
    @Override
    public void resetFloorButton(int floorNumber) {
        if (floorNumber >= 1 && floorNumber <= totalFloors) {
            floorButtons[floorNumber - 1] = false;
            if (guiListener != null) guiListener.notify("Cabin.resetFloorButton",
                    Integer.toString(floorNumber));
            DeviceMultiplexor.getInstance().onDisplaySet(carId, currentFloor + " " + direction);
        }
    }

    /**
     * Updates the cabin display to show the current floor and direction. Must be updated
     * when either of these two aspects change.
     * @param currentFloor Location of the elevator
     * @param direction Direction the cabin is moving in
     */
    @Override
    public void setDisplay(int currentFloor, String direction) {
        this.currentFloor = currentFloor;
        this.direction = direction;
        System.out.println("Display: Floor " + currentFloor + " | Direction: " + direction);
        if (guiListener != null) guiListener.notify("Cabin.setDisplay",
                currentFloor + ":" + direction);
    }

    /**
     * Plays the arrival chime sound. Called when travel requests have been successfully serviced.
     */
    @Override
    public void playCabinArrivalChime() {
        // for now im simulating a DING sound to see if it works
        System.out.println("*Ding!* Elevator arrived at floor " + currentFloor);
        if (guiListener != null) guiListener.notify("Cabin.playCabinArrivalChime",
                Integer.toString(currentFloor));
    }

    /**
     * Plays the overload warning buzz. Called when the GUI option for overload is selected.
     * In this state, the elevator cannot move and the doors will remain open.
     */
    @Override
    public void playCabinOverloadWarning() {
        // same as chime
        System.out.println("*Buzz!* Overload detected — please reduce cabin weight.");
        if (guiListener != null) guiListener.notify("Cabin.playCabinOverloadWarning", null);
    }

    /**
     * Reads the fire key state. Must be read in order to check for the current
     * emergency status.
     * @return boolean fireKeyActive
     */
    @Override
    public boolean isFireKeyActive() {
        return fireKeyActive;
    }

    /**
     * Toggles the fire key for simulation purposes.
     */
    public void toggleFireKey() {
        this.fireKeyActive = !this.fireKeyActive;
        if (guiListener != null) guiListener.notify("Cabin.fireKeyToggled",
                Boolean.toString(this.fireKeyActive));
    }
}