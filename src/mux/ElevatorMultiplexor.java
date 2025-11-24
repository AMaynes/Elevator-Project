package mux;

import bus.Bus.SoftwareBus;
import bus.Bus.SoftwareBusCodes;
import bus.Message.Message;
import motion.MotionAPI;
import motion.Util.Direction;
import pfdAPI.*;

/**
 * Class that defines the ElevatorMultiplexor, which coordinates communication from the Elevator
 * Command Center to the relevant devices. Communication is accomplished via the software bus,
 * and both the PFDs and the motion devices are subject to control.
 * 
 * Note: car and elevator are used interchangeably in this context.
 */
public class ElevatorMultiplexor {

    // Constructor
    public ElevatorMultiplexor(int ID){
        this.ID = ID;
        this.elev = new Elevator(ID, 10);
        initialize(); 
    }

    // Globals
    private int currentFloor = 1;
    private String currentDirection = "IDLE";
    private final int ID;
    private final Elevator elev;
    private final SoftwareBus bus = new SoftwareBus(false);
    private final MotionAPI motionAPI = new MotionAPI();
    private boolean lastFireKeyState = false;
    private boolean lastObstructedState = false;
    private boolean lastOverloadState = false;
    private int lastPressedFloor = 0;
    private int targetFloor = 0;

    
    // Initialize the MUX  (placeholder example subscriptions)
    public void initialize() {
        bus.subscribe(SoftwareBusCodes.doorControl, ID);
        bus.subscribe(SoftwareBusCodes.displayFloor, ID);
        bus.subscribe(SoftwareBusCodes.displayDirection, ID);
        bus.subscribe(SoftwareBusCodes.carDispatch, ID);
        bus.subscribe(SoftwareBusCodes.resetFloorSelection, ID);
//        bus.subscribe(SoftwareBusCodes.setMode, 0);  // Global mode changes
//        bus.subscribe(SoftwareBusCodes.cabinSelect, ID);
//        bus.subscribe(SoftwareBusCodes.cabinPosition, ID);
//        bus.subscribe(SoftwareBusCodes.doorSensor, ID);
//        bus.subscribe(SoftwareBusCodes.doorStatus, ID);
//        bus.subscribe(SoftwareBusCodes.cabinLoad, ID);
//        bus.subscribe(SoftwareBusCodes.fireKey, ID);
        System.out.println("ElevatorMUX " + ID + " initialized and subscribed");
        startBusPoller();
        startStatePoller();
    }


    /**
     * Incoming Message Polling
     */

    // Polls the software bus for messages and handles them accordingly
    public void startBusPoller() {
        Thread t = new Thread(() -> {
            // keep polling
            while (true) {
                Message msg;
                msg = bus.get(SoftwareBusCodes.doorControl, ID);
                if (msg != null) {
                    handleDoorControl(msg);
                }
                msg = bus.get(SoftwareBusCodes.displayFloor, ID);
                if (msg != null) {
                    handleDisplayFloor(msg);
                }
                msg = bus.get(SoftwareBusCodes.displayDirection, ID);
                if (msg != null) {
                    handleDisplayDirection(msg);
                }
                msg = bus.get(SoftwareBusCodes.carDispatch, ID);
                if (msg != null) {
                    handleCarDispatch(msg);
                }
//                msg = bus.get(SoftwareBusCodes.setMode, 0);
//                if (msg != null) {
//                    handleModeSet(msg);
//                }
//                msg = bus.get(SoftwareBusCodes.cabinSelect, ID);
//                if (msg != null) {
//                    handleCabinSelect(msg);
//                }
//                msg = bus.get(SoftwareBusCodes.doorSensor, ID);
//                if (msg != null) {
//                    handleDoorSensor(msg);
//                }
//                msg = bus.get(SoftwareBusCodes.doorStatus, ID);
//                if (msg != null) {
//                    handleDoorStatus(msg);
//                }

//                msg = bus.get(SoftwareBusCodes.cabinLoad, ID);
//                if (msg != null) {
//                    handleCabinLoad(msg);
//                }
//                msg = bus.get(SoftwareBusCodes.fireKey, ID);
//                if (msg != null) {
//                    handleFireKey(msg);
//                }

                msg = bus.get(SoftwareBusCodes.resetFloorSelection, ID);
                if (msg != null) {
                    int floorNumber = msg.getBody();
                    elev.panel.resetFloorButton(floorNumber);
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    /**
     * Internal State Polling Functions
     */

    // Polls the elevator state periodically and publishes updates to the bus
    private void startStatePoller() {
        Thread statePoller = new Thread(() -> {
            while (true) {
                pollFireKeyState();
                pollPressedFloors();
                pollDoorObstruction();
                pollCabinOverload();
                pollCarPosition();
                
                try {
                    Thread.sleep(1000); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        statePoller.start();
    }

    // Poll and publish fire key state changes
    private void pollFireKeyState() {
        boolean fireKeyActive = elev.panel.isFireKeyActive();
        if (fireKeyActive != lastFireKeyState) {
            // Emit FIRE_KEY message (Topic 206) only on state change
            int v;
            if (fireKeyActive) v = 1;
            else v = 0;
            Message fireMsg = new Message(SoftwareBusCodes.fireKey, ID, v);
            bus.publish(fireMsg);
            lastFireKeyState = fireKeyActive;
        }
    }

    // Poll and publish pressed floor buttons
    private void pollPressedFloors() {
        int targetFloor = elev.panel.getPressedFloor();
        if (targetFloor != 0 && targetFloor != lastPressedFloor) {
            Message selectMsg = new Message(SoftwareBusCodes.cabinSelect, ID, targetFloor);
            bus.publish(selectMsg);
            lastPressedFloor = targetFloor;
        }
    }

    // Poll and publish door obstruction state changes
    private void pollDoorObstruction() {
        boolean isObstructed = elev.door.isObstructed();
        int body;
        if(isObstructed){
            body = 0;
        }else{
            body = 1;
        }
        // Update obstruction state
        if (isObstructed != lastObstructedState) {
            Message statusMsg = new Message(SoftwareBusCodes.doorSensor, ID, body);
            bus.publish(statusMsg);
            lastObstructedState = isObstructed;
        }
    }


    // Poll and publish cabin overload state changes
    private void pollCabinOverload() {
        boolean isOverloaded = false;
        if (isOverloaded != lastOverloadState) {
            // Emit CABIN_LOAD message (Topic 205) only on state change
            int v;
            if (isOverloaded) v = 1;
            else v = 0;
            Message loadMsg = new Message(SoftwareBusCodes.cabinLoad, ID, v);
            bus.publish(loadMsg);
            lastOverloadState = isOverloaded;
        }
    }

    // Poll car position
    private void pollCarPosition() {
        Integer sensor = motionAPI.bottom_alignment();
        if (sensor == null) return;

        int newFloor = (sensor / 2) + 1;
        if (newFloor < 1 || newFloor > elev.totalFloors) return;

        // Only update direction when moving between floors
        if (newFloor != currentFloor) {
            if (newFloor > currentFloor) currentDirection = "UP";
            else currentDirection = "DOWN";
        }

        currentFloor = newFloor;

        // Only update GUI when actually MOVING
        if (!currentDirection.equals("IDLE")) {
            elev.display.updateFloorIndicator(currentFloor, currentDirection);
            elev.panel.setDisplay(currentFloor, currentDirection);
            bus.publish(new Message(SoftwareBusCodes.cabinPosition, ID, currentFloor));
        }

        // Arrival logic
        if (targetFloor > 0 && currentFloor == targetFloor) {
            motionAPI.stop();
            currentDirection = "IDLE";

            elev.display.updateFloorIndicator(currentFloor, "IDLE");
            elev.panel.setDisplay(currentFloor, "IDLE");

            elev.door.open();
            elev.panel.resetFloorButton(currentFloor);
            targetFloor = 0;
        }
    }

    // Getter for Elevator
    public Elevator getElevator() {
        return elev;
    }

    /**
     * Incoming Message Handlers
     */

    // Handle door control messages
    private void handleDoorControl(Message msg) {
        int command = msg.getBody();
        Message positionMsg = null;
        if (command == 0) {
            elev.door.open();
            if(elev.door.isFullyOpen()){
                positionMsg = new Message(SoftwareBusCodes.doorStatus, ID, 0);
            } else {
                positionMsg = new Message(SoftwareBusCodes.doorStatus, ID, 1);
            }
        } else if (command == 1) {
            elev.door.close();
            if(elev.door.isFullyClosed()){
                positionMsg = new Message(SoftwareBusCodes.doorStatus, ID, 1);
            } else {
                positionMsg = new Message(SoftwareBusCodes.doorStatus, ID, 0);
            }
        }
        bus.publish(positionMsg);
    }

    // Handle display floor messages
    private void handleDisplayFloor(Message msg) {
        int floor = msg.getBody();
        elev.display.updateFloorIndicator(floor, currentDirection);
        elev.panel.setDisplay(floor, currentDirection);
    }

    // Handle display direction messages
    private void handleDisplayDirection(Message msg) {
        int dir = msg.getBody();
        if (dir == 0){
            elev.display.updateFloorIndicator(currentFloor, "UP");
            elev.panel.setDisplay(currentFloor, "UP");
        } else if (dir == 1) {
            elev.display.updateFloorIndicator(currentFloor, "DOWN");
            elev.panel.setDisplay(currentFloor, "DOWN");
        } else {
            elev.display.updateFloorIndicator(currentFloor, "IDLE");
            elev.panel.setDisplay(currentFloor, "IDLE");
        }
    }

    // Handle car dispatch messages
    private void handleCarDispatch(Message msg) {
        targetFloor = msg.getBody();
        int dir = targetFloor - currentFloor;

        if(elev.door.isFullyClosed()){
            if (dir > 0) {
                currentDirection = "UP";
                motionAPI.set_direction(Direction.UP);
            } else if (dir < 0) {
                currentDirection = "DOWN";
                motionAPI.set_direction(Direction.DOWN);
            } else {
                currentDirection = "IDLE";
                motionAPI.set_direction(Direction.NULL);
            }

            elev.display.updateFloorIndicator(currentFloor, currentDirection);
            elev.panel.setDisplay(currentFloor, currentDirection);
            motionAPI.start();
        }
    }
//
//    // Handle mode set messages
//    private void handleModeSet(Message msg) {
//        // TODO: This is probably not relevant to the MUX.
//    }

//    // Handle cabin select messages
//    private void handleCabinSelect(Message msg) {
//        int floor = msg.getBody();
//        elev.panel.pressFloorButton(floor);
//    }

//    // Handle door sensor messages
//    private void handleDoorSensor(Message msg) {
//        int status = msg.getBody();
//        System.out.println("[MUX] Door sensor status for car " + ID + " = " + status);
//        boolean obstructed = (status == 1);
//        elev.door.setObstruction(obstructed);
//    }

//    // Handle door status messages
//    private void handleDoorStatus(Message msg) {
//        int status = msg.getBody();
//        if (status == 1)
//            elev.door.open();
//        else
//            elev.door.close();
//    }

//    // Handle cabin load messages
//    private void handleCabinLoad(Message msg) {
//        int status = msg.getBody();
//        elev.panel.setOverloadWarning(status == 1);
//    }
//
//    // Handle fire key messages
//    private void handleFireKey(Message msg) {
//        elev.panel.toggleFireKey();
//    }
}
