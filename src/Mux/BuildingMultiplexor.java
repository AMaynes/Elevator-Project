package Mux;

import Message.*;
import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import PFDAPI.Building;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import PFDAPI.FloorCallButtons;
import java.util.Arrays;
import static java.lang.Math.abs;
import Util.SoundUtil;

/**
 * Class that defines the BuildingMultiplexor, which coordinates communication from the Elevator
 * Command Center to the relevant devices. Communication is accomplished via the software bus,
 * and both the PFDs and the motion devices are subject to control.
 *
 * Note: car and elevator are used interchangeably in this context.
 */
public class BuildingMultiplexor {
    private final SoftwareBus bus;
    // Constructor
    public BuildingMultiplexor(SoftwareBus softwareBus){
        bus = softwareBus;
        initialize();
    }

    // Listener for GUI/API integration
    private final Building bldg = new Building(10);;
    private boolean[][] lastCallState = new boolean[bldg.totalFloors][3]; // Up/Down/Null
    private boolean lastFireState = false;
    private int[] elevatorPos = new int[4];
    private MediaPlayer fireAlarmPlayer = null; // Reference to fire alarm player for stopping

    private int DIR_UP = 0;
    private int DIR_DOWN = 1;
    private int FIRE_OFF = 0;
    private int FIRE_ON = 1;

    // Initialize the MUX
    private void initialize() {
        bus.subscribe(SoftwareBusCodes.fireAlarm, 5);
        bus.subscribe(SoftwareBusCodes.resetCall, 5);
        bus.subscribe(SoftwareBusCodes.callsEnable, 5);

        //NOTE: MUX EATS THESE MESSAGES FOR OPTIMAL CALL DISPATCHING
        bus.subscribe(SoftwareBusCodes.cabinPosition, 1);
        bus.subscribe(SoftwareBusCodes.cabinPosition, 2);
        bus.subscribe(SoftwareBusCodes.cabinPosition, 3);
        bus.subscribe(SoftwareBusCodes.cabinPosition, 4);
        Arrays.fill(elevatorPos, 1);

        System.out.println("BuildingMUX initialized and subscribed");
        startBusPoller();
        startStatePoller();
    }

    /**
     * Incoming Message Polling
     */

    // Polls the software bus for messages and handles them accordingly
    private void startBusPoller() {
        Thread t = new Thread(() -> {
            // keep polling
            while (true) {
                Message msg;
                msg = bus.get(SoftwareBusCodes.fireAlarm, 5);
                if (msg != null) {
                    handleFireAlarm(msg);
                }
                msg = bus.get(SoftwareBusCodes.resetCall, 5);
                if (msg != null) {
                    handleCallReset(msg);
                }
                msg = bus.get(SoftwareBusCodes.callsEnable, 5);
                if (msg != null) {
                    handleCallEnable(msg);
                }
                msg = bus.get(SoftwareBusCodes.cabinPosition, 1);
                if (msg != null) {
                    handleElevatorPos(msg);
                }
                msg = bus.get(SoftwareBusCodes.cabinPosition, 2);
                if (msg != null) {
                    handleElevatorPos(msg);
                }
                msg = bus.get(SoftwareBusCodes.cabinPosition, 3);
                if (msg != null) {
                    handleElevatorPos(msg);
                }
                msg = bus.get(SoftwareBusCodes.cabinPosition, 4);
                if (msg != null) {
                    handleElevatorPos(msg);
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
                    Thread.sleep(200); // Poll more frequently for better responsiveness
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
            int elevator = bestElevator(floor);
            if (bldg.callButtons[floor].isUpCallPressed() && !lastCallState[floor][0]) {
                System.out.println("[BuildingMUX] UP call on floor " + (floor+1) + " -> dispatching to Elevator " + (elevator+1));
                SoundUtil.playButtonClick();
                bus.publish(new Message(SoftwareBusCodes.hallCall, elevator + 1, floor + 1 + 100));
                lastCallState[floor][0] = true;
            }
            if (bldg.callButtons[floor].isDownCallPressed() && !lastCallState[floor][1]) {
                System.out.println("[BuildingMUX] DOWN call on floor " + (floor+1) + " -> dispatching to Elevator " + (elevator+1));
                SoundUtil.playButtonClick();
                bus.publish(new Message(SoftwareBusCodes.hallCall, elevator + 1, floor + 1));
                lastCallState[floor][1] = true;
            }
        }
    }

    // Poll fire alarm state
    private void pollFireAlarm() {
        boolean state = bldg.fireAlarm.getFireAlarmStatus();
        if (state != lastFireState) {
            bus.publish(new Message(SoftwareBusCodes.fireAlarmActive, 1, state ? FIRE_ON : FIRE_OFF));
            bus.publish(new Message(SoftwareBusCodes.fireAlarmActive, 2, state ? FIRE_ON : FIRE_OFF));
            bus.publish(new Message(SoftwareBusCodes.fireAlarmActive, 3, state ? FIRE_ON : FIRE_OFF));
            bus.publish(new Message(SoftwareBusCodes.fireAlarmActive, 4, state ? FIRE_ON : FIRE_OFF));
            lastFireState = state;
            if(state){
                fireAlarmResets(true);
                playFireAlarm();
            } else {
                stopFireAlarm();
            }
        }
    }

    /**
     * Incoming Message Handlers
     */

    // Handle Fire Alarm Message
    private void handleFireAlarm(Message msg) {
        int modeCode = msg.getBody();
        if ((modeCode == FIRE_ON) && (!lastFireState)) {
            bldg.fireAlarm.setFireAlarm(true);
            lastFireState = true;
            fireAlarmResets(false);
            playFireAlarm();
        } else if(modeCode == FIRE_OFF){
            bldg.fireAlarm.setFireAlarm(false);
            lastFireState = false;
            stopFireAlarm();
        }
    }

    // Handle Call Reset Message
    private void handleCallReset(Message msg) {
        int floor = msg.getBody()/10;
        int directionCode = msg.getBody()%10;
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
    private void handleCallEnable(Message msg){
        int body = msg.getBody();
        fireAlarmResets(false);
        bldg.callButtons[1].setButtonsEnabled(body);
    }

    // Handle all elevator position (for call button servicing)
    private void handleElevatorPos(Message msg){
        int elevator = msg.getSubTopic()-1;
        int floor =  msg.getBody();
        elevatorPos[elevator] = floor;
    }

    /**
     * Util
     */

    // Choose the closet elevator to give the hall call to
    private int bestElevator(int floor){
        int choose = 0;
        int bestDistance = 1000;
        for(int i = 0; i < 4; i++){
            if(bestDistance > abs(elevatorPos[i]-floor)){
                bestDistance = abs(elevatorPos[i]-floor);
                choose = i;
            }
        }
        return choose;
    }

    // Resets all buttons upon fire alarm ON event
    private void fireAlarmResets(boolean sendMsg){
        for(FloorCallButtons buttons : bldg.callButtons){
            buttons.resetCallButton("DOWN");
            buttons.resetCallButton("UP");
        }
        if(sendMsg){
            bus.publish(new Message(SoftwareBusCodes.fireAlarm, 1, 1));
            bus.publish(new Message(SoftwareBusCodes.fireAlarm, 2, 1));
            bus.publish(new Message(SoftwareBusCodes.fireAlarm, 3, 1));
            bus.publish(new Message(SoftwareBusCodes.fireAlarm, 4, 1));
        }
    }

    // Plays the fire alarm sound
    private void playFireAlarm(){
        // Stop any existing fire alarm first
        stopFireAlarm();
        
        System.out.println("FIRE!");
        
        // Use Platform.runLater to create MediaPlayer on JavaFX thread
        // but don't block - just schedule it
        Platform.runLater(() -> {
            try {
                java.io.File soundFile = new java.io.File("res/sounds/firealarm.mp3");
                if (!soundFile.exists()) {
                    System.err.println("Fire alarm sound file not found: " + soundFile.getAbsolutePath());
                    return;
                }
                
                try {
                    Media media = new Media(soundFile.toURI().toString());
                    MediaPlayer player = new MediaPlayer(media);
                    player.setCycleCount(MediaPlayer.INDEFINITE); // Loop continuously
                    player.setVolume(0.7);
                    player.play();
                    
                    // Store player reference for stopping later
                    synchronized(BuildingMultiplexor.this) {
                        fireAlarmPlayer = player;
                    }
                } catch (Exception e) {
                    System.err.println("JavaFX media player unavailable: " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("Error playing fire alarm sound: " + e.getMessage());
            }
        });
    }
    // Stops the fire alarm sound
    private void stopFireAlarm() {
        synchronized(this) {
            if (fireAlarmPlayer != null) {
                final MediaPlayer playerToStop = fireAlarmPlayer;
                fireAlarmPlayer = null;
                
                // Stop on JavaFX thread to avoid threading issues
                Platform.runLater(() -> {
                    try {
                        playerToStop.stop();
                        playerToStop.dispose();
                    } catch (Exception e) {
                        System.err.println("Error stopping fire alarm: " + e.getMessage());
                    }
                });
            }
        }
    }
}