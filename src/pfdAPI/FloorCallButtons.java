package pfdAPI;
import pfdGUI.gui;

/**
 * Class that defines the functionality of the Floor Call Buttons. Represents
 * the pair of buttons on each floor that allow users to call an elevator for a
 * specific travel directions, UP/DOWN.
 * API:
 *      public boolean isUpCallPressed()
 *      public boolean isDownCallPressed()
 *      public void resetCallButton(String direction)
 *      public void setButtonsEnabled(int enabled)
 */
public class FloorCallButtons implements FloorCallButtonsAPI {

    // Which landing this panel belongs to
    private final int floorNumber;
    private final gui.GUIControl guiControl;

    /**
     * Constructs the floor call button panel.
     * @param floorNumber the floor the panel is located on
     */
    public FloorCallButtons(int floorNumber, gui.GUIControl guiControl) {
        this.guiControl = guiControl;
        this.floorNumber = floorNumber;
    }

    /**
     * Returns whether the Up request button has been pressed. Inactive on the top floor.
     * @return boolean hasUp (false when top floor) && upPressed
     */
    @Override
    public synchronized boolean isUpCallPressed() {
        return guiControl.isCallButtonActive(floorNumber, "UP");
    }

    /**
     * Returns whether the Down request button has been pressed. Inactive on the bottom floor.
     * @return boolean hasDown (false when bottom floor) && downPressed
     */
    @Override
    public synchronized boolean isDownCallPressed() {
        return guiControl.isCallButtonActive(floorNumber, "DOWN");
    }

    /**
     * Reset the specified call indicator ("Up" or "Down") after service.
     * Both must be reset upon fire mode activation.
     * @param direction the button to be reset
     */
    @Override
    public synchronized void resetCallButton(String direction) {
        guiControl.resetCallButton(floorNumber, direction);
    }

    /**
     * Set EVERY button panel to disabled or enabled.
     * Applies to the entire building, despite being 1 panel. So only called on 1!
     * @param enabled, 0 = disabled 1 = enabled
     */
    @Override
    public synchronized void setButtonsEnabled(int enabled){
        if(enabled == 1){
            guiControl.setCallButtonsDisabled(false);
        }else{
            guiControl.setCallButtonsDisabled(true);
        }
    }
}