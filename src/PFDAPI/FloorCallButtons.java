package PFDAPI;
import PFDGUI.gui;
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
public class FloorCallButtons implements FloorCallButtonsAPI {

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
    private final gui.GUIControl guiControl;

    /**
     * Constructs the floor call button panel.
     * @param floorNumber the floor the panel is located on
     * @param totalFloors total number of floors in the building (=10)
     */
    public FloorCallButtons(int floorNumber, int totalFloors, gui.GUIControl guiControl) {
        this.guiControl = guiControl;
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
    public synchronized void pressUpCall() {
        if (hasUp) {
            upPressed = true;
            guiControl.setCallButton(floorNumber, "UP");
        }
    }

    /**
     * Simulate pressing the Down call
     */
    public synchronized void pressDownCall() {
        if (hasDown) {
            downPressed = true;
            guiControl.setCallButton(floorNumber, "DOWN");
        }
    }

    /**
     * Set the fire alarm status
     * @param status true if fire alarm is active, false otherwise
     */
    public synchronized void setFireAlarm(boolean status) { guiControl.setFireAlarm(status); }

    /**
     * Get the fire alarm status
     */
    public synchronized boolean getFireAlarmStatus() { return guiControl.getFireAlarm(); }

    /**
     * Returns whether the Up request button has been pressed. Inactive on the top floor.
     * @return boolean hasUp (false when top floor) && upPressed
     */
    @Override
    public synchronized boolean isUpCallPressed() {
        return hasUp && guiControl.isCallButtonActive(floorNumber, "UP");
    }

    /**
     * Returns whether the Down request button has been pressed. Inactive on the bottom floor.
     * @return boolean hasDown (false when bottom floor) && downPressed
     */
    @Override
    public synchronized boolean isDownCallPressed() {
        return hasDown && guiControl.isCallButtonActive(floorNumber, "DOWN");
    }

    /**
     * Reset the specified call indicator ("Up" or "Down") after service.
     * Both must be reset upon emergency mode activation.
     * @param direction the button to be reset
     */
    @Override
    public synchronized void resetCallButton(String direction) {
        if (direction.equalsIgnoreCase("UP") && hasUp) {
            guiControl.resetCallButton(floorNumber);
        } else if (direction.equalsIgnoreCase("DOWN") && hasDown) {
            guiControl.resetCallButton(floorNumber);
        }
    }
}