/**
 * Class that defines a given Elevator (4 in total).
 * Elevators each have their own doors and floor displays for
 * the sake of simplicity (in actuality, would exist on all 10 floors,
 * but still ascribed to a specific elevator).
 */
public class Elevator {
    // ID of the elevator
    public final int carId;
    // The elevator's doors
    public final ElevatorDoorsAssembly doors;
    // The elevator's passenger panel
    public final CabinPassengerPanel panel;
    // The elevator's floor display
    public final ElevatorFloorDisplay display;
    // TODO: Unsure of why an elevator would hold a floor call button panel for the first floor.
    public final FloorCallButtons hall;

    /**
     * Constructs an Elevator.
     * @param carId the ID of the elevator (1-4)
     * @param totalFloors the number of floors in the building (=10)
     */
    public Elevator(int carId, int totalFloors) {
        this.carId = carId;
        this.doors  = new ElevatorDoorsAssembly(carId);
        this.panel  = new CabinPassengerPanel(carId, totalFloors);
        this.display = new ElevatorFloorDisplay(carId);
        this.hall   = new FloorCallButtons(carId, 1, totalFloors);
    }
}
