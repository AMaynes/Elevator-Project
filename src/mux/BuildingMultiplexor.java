package mux;

import bus.Bus.SoftwareBus;
import bus.Bus.SoftwareBusCodes;
import bus.Message.Message;
import pfdAPI.Building;

/**
 * Class that defines the BuildingMultiplexor, which coordinates communication from the Elevator
 * Command Center to the relevant devices. Communication is accomplished via the software bus,
 * and both the PFDs and the motion devices are subject to control.
 * 
 * Note: car and elevator are used interchangeably in this context.
 */
public class BuildingMultiplexor {

    // Constructor
    public BuildingMultiplexor(){ 
        initialize(); 
    }

    // Listener for GUI/API integration
    private final SoftwareBus bus = new SoftwareBus(false);
    private final Building bldg = new Building(10);;
    boolean[][] lastCallState = new boolean[bldg.totalFloors][3]; // Up/Down/Null
    private boolean lastFireState = false;


    // Door Commands
    int DOOR_OPEN = 1;
    int DOOR_CLOSE = 2;

    int DIR_UP = 0;
    int DIR_DOWN = 1;

    int FIRE_OFF = 0;
    int FIRE_ON = 1;

    // Initialize the MUX
    public void initialize() { 
        bus.subscribe(SoftwareBusCodes.fireAlarm, 0);
        bus.subscribe(SoftwareBusCodes.resetCall, 0);
        bus.subscribe(SoftwareBusCodes.hallCall, 0);
        System.out.println("BuildingMUX initialized and subscribed");
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
                msg = bus.get(SoftwareBusCodes.fireAlarm, 0);
                if (msg != null) {
                    handleFireAlarm(msg);
                }
                msg = bus.get(SoftwareBusCodes.resetCall, 0);
                if (msg != null) {
                    handleCallReset(msg);
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

    // Polls the bldg state periodically and publishes updates to the bus
    private void startStatePoller() {
        Thread statePoller = new Thread(() -> {
            while (true) {
                pollCallButtons();
                pollFireAlarm();
                
                try {
                    Thread.sleep(1000); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        statePoller.start();
    }

    // Poll all call buttons
    private void pollCallButtons() {
        for (int floor = 0; floor < bldg.callButtons.length; floor++) {
            if (bldg.callButtons[floor].isUpCallPressed() && !lastCallState[floor][0]) {
                bus.publish(new Message(SoftwareBusCodes.hallCall, floor + 1, DIR_UP));
                lastCallState[floor][0] = true;
            }

            if (bldg.callButtons[floor].isDownCallPressed() && !lastCallState[floor][1]) {
                bus.publish(new Message(SoftwareBusCodes.hallCall, floor + 1, DIR_DOWN));
                lastCallState[floor][1] = true;
            }
        }
    }

    // Poll fire alarm state
    private void pollFireAlarm() {
        boolean state = bldg.callButtons[0].getFireAlarmStatus();
        if (state != lastFireState) {
            bus.publish(new Message(SoftwareBusCodes.fireAlarm, 0, state ? FIRE_ON : FIRE_OFF));
            lastFireState = state;
        }
    }

    /**
     * Incoming Message Handlers
     */

    // Handle Fire Alarm Message
    public void handleFireAlarm(Message msg) {
        int modeCode = msg.getBody();
        if (modeCode == FIRE_ON) {
            bldg.callButtons[0].setFireAlarm(true);
        } else if(modeCode == FIRE_OFF){
            bldg.callButtons[0].setFireAlarm(false);
        }
    }

    // Handle Call Reset Message
    public void handleCallReset(Message msg) {
        int floor = msg.getSubTopic()-1;
        int directionCode = msg.getBody();
        if (directionCode == DIR_UP) {
            bldg.callButtons[floor].resetCallButton("UP");
            lastCallState[floor][0] = false;
        } 
        else if (directionCode == DIR_DOWN) {
            bldg.callButtons[floor].resetCallButton("DOWN");
            lastCallState[floor][1] = false;
        }
    }
}
