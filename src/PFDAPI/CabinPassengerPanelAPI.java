package PFDAPI;

/**
 * Required API for the CabinPassengerPanel.
 */
public interface CabinPassengerPanelAPI {

    // Return all floor selections made since last poll, panel queues presses so none are missed.
    int getPressedFloor();

    // Clear the pending floor selections.
    void clearPressedFloors();

    // Makes the buttons disabled (off or fire mode)/enabled.
    void setButtonsDisabled(int disabled);

    // Sets the selection mode to single (fire key)/multiple.
    void setButtonsSingle(int single);

    // Turn off the lamp for a serviced floor button.
    void resetFloorButton(int floorNumber);

    // Update the in-cabin display with current floor and travel direction.
    void setDisplay(int currentFloor, String direction);

    // Play the arrival chime (“ding”) upon arrival/leveling.
    void playCabinArrivalChime();

    // Play the overload warning (“buzz”) when cabin load exceeds max.
    void playCabinOverloadWarning();

    // Read fire service key switch state for emergency operations.
    boolean isFireKeyActive();

    // Checks the weight sensor to determine if overloaded.
    boolean isOverloaded();
}

/**
 * Required API for the FloorCallButtons.
 */
interface FloorCallButtonsAPI {

    // True if the landing panel’s “Up” call is active (not functonal for the top floor).
    boolean isUpCallPressed();

    // True if the landing panel’s “Down” call is active (not functional for the bottom floor).
    boolean isDownCallPressed();

    // Reset the specified call indicator ("Up" or "Down") after service.
    void resetCallButton(String direction);

    // Turns the buttons on/off.
    void setButtonsEnabled(int enabled);
}