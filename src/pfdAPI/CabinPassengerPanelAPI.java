package pfdAPI;

/**
 * Required API for the CabinPassengerPanel.
 */
public interface CabinPassengerPanelAPI {

    // Return all floor selections made since last poll, panel queues presses so none are missed.
    int getPressedFloor();

    // Clear the pending floor selections after they’ve been processed.
    void clearPressedFloors();

    // Turn off the lamp for a serviced floor button.
    void resetFloorButton(int floorNumber);

    // Turn on/off the buttons.
    void setButtonsDisabled(int disabled);

    // Make the selection mode single/multiple.
    void setButtonsSingle(int single);

    // Update the in-cabin display with current floor and travel direction.
    void setDisplay(int currentFloor, String direction);

    // Play the arrival chime (“ding”) upon arrival/leveling.
    void playCabinArrivalChime();

    // Play the overload warning (“buzz”) when cabin load exceeds max.
    void playCabinOverloadWarning();

    // Read fire service key switch state for emergency operations.
    boolean isFireKeyActive();

    // Read the overload sensor to determine if the cabin is overloaded.
    boolean isOverloaded();
}

/**
 * Required API for the FloorCallButtons.
 */
interface FloorCallButtonsAPI {

    // True if the landing panel’s “Up” call is active (not functional for the top floor).
    boolean isUpCallPressed();

    // True if the landing panel’s “Down” call is active (not functional for the bottom floor).
    boolean isDownCallPressed();

    // Reset the specified call indicator ("Up" or "Down") after service.
    void resetCallButton(String direction);

    // Set the buttons to disabled/enabled
    void setButtonsEnabled(int enabled);
}