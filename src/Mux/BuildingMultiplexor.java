package Mux;

import Message.*;
import Bus.SoftwareBus;
import PFDAPI.Building;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import PFDAPI.Building;
import PFDAPI.FloorCallButtons;

import java.net.URL;

// Below are the import statements used by Team 10
//package mux;

//import bus.Bus.SoftwareBus;
//import bus.Bus.SoftwareBusCodes;
//import bus.Message.Message;

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

    int DIR_UP = 0;
    int DIR_DOWN = 1;

    int FIRE_OFF = 0;
    int FIRE_ON = 1;

    // Initialize the MUX
    public void initialize() {
        bus.subscribe(Topic.fireAlarm, 0);
        bus.subscribe(Topic.resetCall, 0);

        bus.subscribe(Topic.callsEnable, 0);

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
                msg = bus.get(Topic.fireAlarm, 0);
                if (msg != null) {
                    handleFireAlarm(msg);
                }
                msg = bus.get(Topic.resetCall, 0);
                if (msg != null) {
                    handleCallReset(msg);
                }

                msg = bus.get(Topic.callsEnable, 0);
                if (msg != null) {
                    handleCallEnable(msg);
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
                bus.publish(new Message(Topic.hallCall, floor + 1, DIR_UP));
                lastCallState[floor][0] = true;
            }

            if (bldg.callButtons[floor].isDownCallPressed() && !lastCallState[floor][1]) {
                bus.publish(new Message(Topic.hallCall, floor + 1, DIR_DOWN));
                lastCallState[floor][1] = true;
            }
        }
    }

    // Poll fire alarm state
    private void pollFireAlarm() {
        boolean state = bldg.callButtons[0].getFireAlarmStatus();
        if (state != lastFireState) {
            bus.publish(new Message(Topic.fireAlarmActive, 0, state ? FIRE_ON : FIRE_OFF));
            lastFireState = state;
            if(state){
                fireAlarmResets(true);
                playFireAlarm();
            }
        }
    }

    /**
     * Incoming Message Handlers
     */

    // Handle Fire Alarm Message
    public void handleFireAlarm(Message msg) {
        int modeCode = msg.getBody();
        if ((modeCode == FIRE_ON) && (!lastFireState)) {
            bldg.callButtons[0].setFireAlarm(true);
            lastFireState = true;
            fireAlarmResets(false);
            playFireAlarm();
        } else if(modeCode == FIRE_OFF){
            bldg.callButtons[0].setFireAlarm(false);
            lastFireState = false;
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

    // Handle Call Enable/Disable Message
    public void handleCallEnable(Message msg){
        int body = msg.getBody();
        bldg.callButtons[1].setButtonsEnabled(body);
    }


    private void fireAlarmResets(boolean sendMsg){
        for(FloorCallButtons buttons : bldg.callButtons){
            buttons.resetCallButton("DOWN");
            buttons.resetCallButton("UP");
        }
        if(sendMsg){
            bus.publish(new Message(Topic.fireAlarm, 0, 1));
        }
    }

    private void playFireAlarm(){
        System.out.println("FIRE!");
        Platform.runLater(() -> {
            try {
                URL sound = getClass().getResource("/sounds/firealarm.mp3");
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
    }
}
