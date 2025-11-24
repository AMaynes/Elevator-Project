package pfdAPI;
import java.util.ArrayList;
import java.util.List;

import java.net.URL;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import pfdGUI.gui;

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
 */
public class CabinPassengerPanel implements CabinPassengerPanelAPI {

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
    // The ID of the associated elevator
    private final int carId;
    // GUI Control reference
    private final gui.GUIControl guiControl;

    /**
     * Constructor of the CabinPassengerPanel.
     * @param totalFloors Number of floors in the building (=10)
     */
    public CabinPassengerPanel(int carId, int totalFloors, gui.GUIControl guiControl) {
        this.carId = carId;
        this.guiControl = guiControl;
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
    public synchronized void pressFloorButton(int floorNumber) {
        if (floorNumber >= 1 && floorNumber <= totalFloors && !floorButtons[floorNumber - 1]) {
            floorButtons[floorNumber - 1] = true;
            pressedFloorsQueue.add(floorNumber);
            guiControl.pressPanelButton(carId, floorNumber);
        }
    }

    /**
     * Returns all pressed floor numbers since the last poll.
     * Requests made by the riders must be serviced when not in emergency mode.
     * @return copy of ArrayList<Integer> pressedFloorsQueue
     */
    @Override
    public synchronized int getPressedFloor() {
        if (pressedFloorsQueue.isEmpty()) {
            return 0;
        }
        return pressedFloorsQueue.remove(0); // Remove and return the head
    }

    /**
     * Clears all stored pressed floor events. Called upon the suspension of
     * regular activities (emergency mode).
     */
    @Override
    public synchronized void clearPressedFloors() {
        pressedFloorsQueue.clear();
        guiControl.resetPanel(carId);
    }

    /**
     * Resets a specific floor button's indicator. Resets occur after the travel
     * request has been serviced.
     * @param floorNumber the reset floor button's associated floor
     */
    @Override
    public synchronized void resetFloorButton(int floorNumber) {
        if (floorNumber >= 1 && floorNumber <= totalFloors) {
            floorButtons[floorNumber - 1] = false;
            guiControl.resetPanelButton(carId, floorNumber);
            System.out.println("DEBUG: Reset floor button " + floorNumber + " for elevator " + carId);


        }
    }

    /**
     * Updates the cabin display to show the current floor and direction. Must be updated
     * when either of these two aspects change.
     * @param currentFloor Location of the elevator
     * @param direction Direction the cabin is moving in
     */
    @Override
    public synchronized void setDisplay(int currentFloor, String direction) {
        this.currentFloor = currentFloor;
        this.direction = direction;
        guiControl.setDisplay(carId, currentFloor, direction);
        System.out.println("Display: Floor " + currentFloor + " | Direction: " + direction);
    }

    /**
     * Plays the arrival chime sound. Called when travel requests have been successfully serviced.
     */
    @Override
    public void playCabinArrivalChime() {
        int floor;
        synchronized (this) {
            floor = currentFloor;
        }

        System.out.println("*Ding!* Elevator arrived at floor " + floor);

        Platform.runLater(() -> {
            try {
                URL sound = getClass().getResource("/sounds/ding.mp3");
                if (sound == null) {
                    System.err.println("Sound file not found.");
                    return;
                }

                Media media = new Media(sound.toExternalForm());
                MediaPlayer player = new MediaPlayer(media);
                player.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        guiControl.resetPanelButton(carId, currentFloor);
    }

    /**
     * Plays the overload warning buzz. Called when the GUI option for overload is selected.
     * In this state, the elevator cannot move and the doors will remain open.
     */
    @Override
    public synchronized void playCabinOverloadWarning() {
        // same as chime
        System.out.println("*Buzz!* Overload detected — please reduce cabin weight.");
    }

    /**
     * Sets the overload warning state.
     * Used when the overload state is set externally (e.g., from the MUX).
     */
    public synchronized void setOverloadWarning(boolean overload) {
        // same as chime
        if(overload){ playCabinOverloadWarning(); }
        guiControl.setCabinOverload(carId, overload);
    }

    /**
     * Reads the fire key state. Must be read in order to check for the current
     * emergency status.
     * @return boolean fireKeyActive
     */
    @Override
    public synchronized boolean isFireKeyActive() {
        return guiControl.getIsFireKeyActive(carId);
    }

    /**
     * Toggles the fire key for simulation purposes.
     */
    public synchronized void toggleFireKey() {
        this.fireKeyActive = !this.fireKeyActive;
        guiControl.setFireAlarm(fireKeyActive);
    }
}