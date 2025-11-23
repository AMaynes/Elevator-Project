package Mux;

import Message.Topic;
import Message.Message;
import PFDAPI.Elevator;
import Bus.SoftwareBus;
import Team7MotionControl.Elevator_Controler.MotionController;
import Team7MotionControl.Util.Direction;


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
    private final MotionController motionController =new MotionController();
    private boolean lastFireKeyState = false;
    private boolean lastObstructedState = false;
    private boolean lastOverloadState = false;
    private boolean pendingDispatch = false;
    private int lastPressedFloor = 0;
    private int targetFloor = 0;

    
    // Initialize the MUX  (placeholder example subscriptions)
    public void initialize() {
        bus.subscribe(Topic.DOOR_CONTROL, ID);
        bus.subscribe(Topic.DISPLAY_FLOOR, ID);
        bus.subscribe(Topic.DISPLAY_DIRECTION, ID);
        bus.subscribe(Topic.CAR_DISPATCH, ID);
        bus.subscribe(Topic.MODE_SET, 0);  // Global mode changes
        bus.subscribe(Topic.CABIN_SELECT, ID);
        bus.subscribe(Topic.CAR_POSITION, ID);
        bus.subscribe(Topic.DOOR_SENSOR, ID);
        bus.subscribe(Topic.DOOR_STATUS, ID);
        bus.subscribe(Topic.CABIN_LOAD, ID);
        bus.subscribe(Topic.FIRE_KEY, ID);
        bus.subscribe(Topic.CABIN_RESET, ID);
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
                msg = bus.get(Topic.DOOR_CONTROL, ID);
                if (msg != null) {
                    handleDoorControl(msg);
                }
                msg = bus.get(Topic.DISPLAY_FLOOR, ID);
                if (msg != null) {
                    handleDisplayFloor(msg);
                }
                msg = bus.get(Topic.DISPLAY_DIRECTION, ID);
                //TODO: SENT BY NOTIFIER
                if (msg != null) {
                    handleDisplayDirection(msg);
                }
                msg = bus.get(Topic.CAR_DISPATCH, ID);
                //TODO: SENT BY CABIN but not correct
                if (msg != null) {
                    handleCarDispatch(msg);
                }
                msg = bus.get(Topic.MODE_SET, 0);
                //TODO: NOT IMPLEMENTED
                if (msg != null) {
                    handleModeSet(msg);
                }
                msg = bus.get(Topic.CABIN_SELECT, ID);
                if (msg != null) {
                    handleCabinSelect(msg);
                }
                msg = bus.get(Topic.DOOR_SENSOR, ID);
                if (msg != null) {
                    handleDoorSensor(msg);
                }
                msg = bus.get(Topic.DOOR_STATUS, ID);
                if (msg != null) {
                    handleDoorStatus(msg);
                }
                msg = bus.get(Topic.CABIN_LOAD, ID);
                if (msg != null) {
                    handleCabinLoad(msg);
                }
                msg = bus.get(Topic.FIRE_KEY, ID);
                if (msg != null) {
                    handleFireKey(msg);
                }

                msg = bus.get(Topic.CABIN_RESET, ID);
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
            Message fireMsg = new Message(Topic.FIRE_KEY, ID, v);
            bus.publish(fireMsg);
            lastFireKeyState = fireKeyActive;
        }
    }

    // Poll and publish pressed floor buttons
    private void pollPressedFloors() {
        int targetFloor = elev.panel.getPressedFloor();
        if (targetFloor != 0 && targetFloor != lastPressedFloor) {
            Message selectMsg = new Message(Topic.CABIN_SELECT, ID, targetFloor);
            bus.publish(selectMsg);
            lastPressedFloor = targetFloor;
        }
    }

    // Poll and publish door obstruction state changes
    private void pollDoorObstruction() {
        boolean isObstructed = elev.door.isObstructed();

        // Update obstruction state
        if (isObstructed != lastObstructedState) {
            lastObstructedState = isObstructed;
        }
        if (pendingDispatch && !isObstructed && elev.door.isFullyClosed()) {
            System.out.println("[MUX] Doors closed & obstruction cleared — executing pending dispatch.");
            pendingDispatch = false;
            handleCarDispatch(new Message(Topic.CAR_DISPATCH, ID, targetFloor));
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
            Message loadMsg = new Message(Topic.CABIN_LOAD, ID, v);
            bus.publish(loadMsg);
            lastOverloadState = isOverloaded;
        }
    }

    // Poll car position
    private void pollCarPosition() {
        Integer sensor = motionController.bottom_alignment();
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
            bus.publish(new Message(Topic.CAR_POSITION, ID, currentFloor));
        }

        // Arrival logic
        if (targetFloor > 0 && currentFloor == targetFloor) {
            motionController.stop();
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
        if (command == 1) {
            elev.door.open();
        } else if (command == 2) {
            elev.door.close();
        }
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

        elev.door.close();
        if (elev.door.isObstructed() || !elev.door.isFullyClosed()) {
            System.out.println("[MUX] Dispatch blocked — waiting for doors to close...");
            pendingDispatch = true;
            return;
        }
        pendingDispatch = false;

        if (dir > 0) {
            currentDirection = "UP";
            motionController.set_direction(Direction.UP);
        } else if (dir < 0) {
            currentDirection = "DOWN";
            motionController.set_direction(Direction.DOWN);
        } else {
            currentDirection = "IDLE";
            motionController.set_direction(Direction.NULL);
        }

        elev.display.updateFloorIndicator(currentFloor, currentDirection);
        elev.panel.setDisplay(currentFloor, currentDirection);
        motionController.start();
    }

    // Handle mode set messages
    private void handleModeSet(Message msg) {
        // TODO: implement mode set logic
    }

    // Handle cabin select messages
    private void handleCabinSelect(Message msg) {
        int floor = msg.getBody();
        elev.panel.pressFloorButton(floor);
    }

    // Handle door sensor messages
    private void handleDoorSensor(Message msg) {
        int status = msg.getBody(); 
        System.out.println("[MUX] Door sensor status for car " + ID + " = " + status);
        boolean obstructed = (status == 1);
        elev.door.setObstruction(obstructed);    
    }

    // Handle door status messages
    private void handleDoorStatus(Message msg) {    
        int status = msg.getBody(); 
        if (status == 1)
            elev.door.open();
        else
            elev.door.close();
    }

    // Handle cabin load messages
    private void handleCabinLoad(Message msg) {
        int status = msg.getBody();
        elev.panel.setOverloadWarning(status == 1);
    }

    // Handle fire key messages
    private void handleFireKey(Message msg) {
        elev.panel.toggleFireKey();
    }
}
