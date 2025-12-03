package pfdAPI;
import pfdGUI.gui;

/**
 * Class that encompasses the state of the fire alarm. Is either active or inactive. Can be set by the
 * GUI, or the elevator controller (this is why there is a public setter in the API).
 * API:
 *      public void setFireAlarm()
 *      public boolean getFireAlarmStatus()
 */
public class FireAlarm {
    // GUI Control reference
    private final gui.GUIControl guiControl;

    /**
     * Constructs the fire alarm.
     * @param guiControl the PFD GUI
     */
    public FireAlarm(gui.GUIControl guiControl) {
        this.guiControl = guiControl;
    }

    /**
     * Set the fire alarm status
     * @param status true if fire alarm is active, false otherwise
     */
    public synchronized void setFireAlarm(boolean status) {
        guiControl.setFireAlarm(status);
    }

    /**
     * Get the fire alarm status
     */
    public synchronized boolean getFireAlarmStatus() {
        return guiControl.getFireAlarm();
    }
}