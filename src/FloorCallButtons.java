/**
 * Class that defines the functionality of the Floor Call Buttons. Represents
 * the pair of buttons on each floor that allow users to call an elevator for a
 * specific travel directions, UP/DOWN.
 * API:
 *      public boolean isUpCallPressed()
 *      public boolean isDownCallPressed()
 *      public void resetCallButton(String direction)
 * For GUI purposes:
 *      public void pressUpCall()
 *      public void pressDownCall()
 *      public static void setGuiListener(gui.listener l)
 */
class FloorCallButtons implements FloorCallButtonsAPI {

    // Optional GUI listener
    private static gui.listener guiListener = null;

    /**
     * Sets the ElevatorFloorDisplay's guiListener.
     * @param l The ElevatorFloorDisplay's guiListener.
     */
    public static void setGuiListener(gui.listener l) {
        guiListener = l;
    }

    // Which landing this panel belongs to
    private final int floorNumber;
    // Total number of building floors (=10)
    private final int totalFloors;
    // Top floor has no Up
    private final boolean hasUp;
    // Bottom floor has no Down
    private final boolean hasDown;
    // True if Up call is active
    private boolean upPressed;
    // True if Down call is active
    private boolean downPressed;
    // TODO: Floor call buttons don't have their own elevators and vice-versa.
    private final int carId;

    /**
     * Constructs the floor call button panel.
     * @param carId the ID of the elevator TODO: Should only belong to a floor
     * @param floorNumber the floor the panel is located on
     * @param totalFloors total number of floors in the building (=10)
     */
    public FloorCallButtons(int carId, int floorNumber, int totalFloors) {
        this.carId = carId;
        this.floorNumber = floorNumber;
        this.totalFloors = totalFloors;
        this.hasUp = floorNumber < totalFloors;
        this.hasDown = floorNumber > 1;
        this.upPressed = false;
        this.downPressed = false;
    }

    /**
     * Simulate pressing the Up call
     */
    public void pressUpCall() {
        if (hasUp) {
            upPressed = true;
            if (guiListener != null) guiListener.notify("FloorCall.pressUp", Integer.toString(floorNumber));
            DeviceMultiplexor.getInstance().emitCallButtonClick(carId, floorNumber - 1, "UP", floorNumber - 1);

        }
    }

    /**
     * Simulate pressing the Down call
     */
    public void pressDownCall() {
        if (hasDown) {
            downPressed = true;
            if (guiListener != null) guiListener.notify("FloorCall.pressDown", Integer.toString(floorNumber));
            DeviceMultiplexor.getInstance().emitCallButtonClick(carId, floorNumber - 1, "DOWN", floorNumber - 1);

        }
    }

    /**
     * Returns whether the Up request button has been pressed. Inactive on the top floor.
     * @return boolean hasUp (false when top floor) && upPressed
     */
    @Override
    public boolean isUpCallPressed() {
        return hasUp && upPressed;
    }

    /**
     * Returns whether the Down request button has been pressed. Inactive on the bottom floor.
     * @return boolean hasDown (false when bottom floor) && downPressed
     */
    @Override
    public boolean isDownCallPressed() {
        return hasDown && downPressed;
    }

    /**
     * Reset the specified call indicator ("Up" or "Down") after service.
     * Both must be reset upon emergency mode activation.
     * @param direction the button to be reset
     */
    @Override
    public void resetCallButton(String direction) {
        if (direction.equals("UP") && hasUp) {
            upPressed = false;
            if (guiListener != null) guiListener.notify("FloorCall.resetCallButton", "UP:" + floorNumber);
        } else if (direction.equals("DOWN") && hasDown) {
            downPressed = false;
            if (guiListener != null) guiListener.notify("FloorCall.resetCallButton", "DOWN:" + floorNumber);
        }
    }
}