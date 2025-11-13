/**
 * Old testing code that tests the PFD API.
 */
public class testApi {
    public static void main(String[] args) {
        ElevatorFloorDisplay display = new ElevatorFloorDisplay(1);
        ElevatorDoorsAssembly doors = new ElevatorDoorsAssembly(1);

        // Display updates
        display.updateFloorIndicator(4, "UP");
        display.playArrivalChime();

        // Door operations
        doors.open();
        doors.setObstruction(true);
        doors.close();  // Should detect obstruction and reopen
        doors.setObstruction(false);
        doors.close();  // Now closes successfully

        display.playOverLoadWarning();
    }
}
