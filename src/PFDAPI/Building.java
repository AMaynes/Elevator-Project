package PFDAPI;

import PFDGUI.gui;

public class Building {

    // The building's elevator call buttons on each floor
    public final PFDAPI.FloorCallButtons[] callButtons;
    public final int totalFloors;

    /**
     * Constructs a Building.
     * @param totalFloors the number of floors in the building (=10)
     */
    public Building(int totalFloors) {
        this.totalFloors = totalFloors;
        gui g = gui.getInstance();
        this.callButtons = new PFDAPI.FloorCallButtons[totalFloors];
        for (int i = 0; i < totalFloors; i++) {
            this.callButtons[i] = new PFDAPI.FloorCallButtons(i+1, totalFloors, g.internalState);
        }
    }
}
