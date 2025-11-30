package Mux;

import Message.Topic;
import Message.Message;
import PFDAPI.Elevator;
import Bus.SoftwareBus;
import Team7MotionControl.Elevator_Controler.MotionController;
import Team7MotionControl.Util.Direction;

// NOTE: below are Mux's import statements
//package mux;

//import bus.Bus.SoftwareBus;
//import bus.Bus.SoftwareBusCodes;
//import bus.Message.Message;
//import motion.MotionAPI;
//import motion.Util.Direction;
//import pfdAPI.*;

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
    private final MotionController motionAPI = new MotionController();
    private boolean lastFireKeyState = false;
    private boolean lastObstructedState = false;
    private boolean lastOverloadState = false;
    private int lastPressedFloor = 0;
    private int targetFloor = 0;

    // Initialize the MUX
    public void initialize() {
        bus.subscribe(Topic.doorControl, ID);
        bus.subscribe(Topic.displayFloor, ID);
        bus.subscribe(Topic.displayDirection, ID);
        bus.subscribe(Topic.carDispatch, ID);
        bus.subscribe(Topic.resetFloorSelection, ID);

        bus.subscribe(Topic.carStop, ID);
        bus.subscribe(Topic.selectionsEnable, ID);
        bus.subscribe(Topic.selectionsType, ID);
        bus.subscribe(Topic.playSound, ID);

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
                msg = bus.get(Topic.doorControl, ID);
                if (msg != null) {
                    handleDoorControl(msg);
                }
                msg = bus.get(Topic.displayFloor, ID);
                if (msg != null) {
                    handleDisplayFloor(msg);
                }
                msg = bus.get(Topic.displayDirection, ID);
                if (msg != null) {
                    handleDisplayDirection(msg);
                }
                msg = bus.get(Topic.carDispatch, ID);
                if (msg != null) {
                    handleCarDispatch(msg);
                }
                msg = bus.get(Topic.resetFloorSelection, ID);
                if (msg != null) {
                    int floorNumber = msg.getBody();
                    elev.panel.resetFloorButton(floorNumber);
                }

                msg = bus.get(Topic.carStop, ID);
                if (msg != null) {
                    handleCarStop(msg);
                }
                msg = bus.get(Topic.selectionsEnable, ID);
                if (msg != null) {
                    handleSelectionEnable(msg);
                }
                msg = bus.get(Topic.selectionsType, ID);
                if (msg != null) {
                    handleSelectionType(msg);
                }
                msg = bus.get(Topic.playSound, 0);
                if (msg != null) {
                    handlePlaySound(msg);
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
            Message fireMsg = new Message(Topic.fireKey, ID, v);
            bus.publish(fireMsg);
            lastFireKeyState = fireKeyActive;
        }
    }

    // Poll and publish pressed floor buttons
    private void pollPressedFloors() {
        int targetFloor = elev.panel.getPressedFloor();
        if (targetFloor != 0 && targetFloor != lastPressedFloor) {
            Message selectMsg = new Message(Topic.cabinSelect, ID, targetFloor);
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
            Message statusMsg = new Message(Topic.doorSensor, ID, body);
            bus.publish(statusMsg);
            lastObstructedState = isObstructed;
        }
    }


    // Poll and publish cabin overload state changes
    private void pollCabinOverload() {
        boolean isOverloaded = elev.display.isOverloaded();
        if (isOverloaded != lastOverloadState) {
            // Emit CABIN_LOAD message (Topic 205) only on state change
            int v;
            if (isOverloaded) v = 1;
            else v = 0;
            Message loadMsg = new Message(Topic.cabinLoad, ID, v);
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
        //TODO: && newFloor != currentFloor ?
        if (!currentDirection.equals("IDLE")) {
            elev.display.updateFloorIndicator(currentFloor, currentDirection);
            elev.panel.setDisplay(currentFloor, currentDirection);
            bus.publish(new Message(Topic.cabinPosition, ID, currentFloor));
        }

        //TODO: Remove this. Nice for the purposes of the Demo, though.
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
                positionMsg = new Message(Topic.doorStatus, ID, 0);
            } else {
                positionMsg = new Message(Topic.doorStatus, ID, 1);
            }
        } else if (command == 1) {
            elev.door.close();
            if(elev.door.isFullyClosed()){
                positionMsg = new Message(Topic.doorStatus, ID, 1);
            } else {
                positionMsg = new Message(Topic.doorStatus, ID, 0);
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
    //TODO: Fix so that it just starts the elevator in the direction indicated by body
    // 0 = up, 1 = down
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

    // Handle Car Stop Message
    private void handleCarStop(Message msg){
        //TODO
    }

    // Handle Selection Disable/Enable Message
    private void handleSelectionEnable(Message msg) {
        int body = msg.getBody();
        elev.panel.setButtonsDisabled(body);
    }

    // Handle Selection allow single/multiple Message
    private void handleSelectionType(Message msg) {
        int body = msg.getBody();
        elev.panel.setButtonsSingle(body);
    }

    // Handle play arrival/overload Message
    public void handlePlaySound(Message msg){
        int type = msg.getBody();
        if (type == 0) {
            elev.display.playArrivalChime();
        } else {
            elev.display.playOverLoadWarning();
        }
    }
}
