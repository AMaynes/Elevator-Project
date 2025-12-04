package PFDAPI;

import PFDGUI.gui;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.sound.sampled.*;
import java.io.File;

/**
 * Class that defines the functionality of the Elevator floor displays. Represents
 * the panel above elevator doors that show the elevator's location and direction of movement.
 * API:
 *      public void updateFloorIndicator(int currentFloor, String direction)
 *      public void playArrivalChime()
 *      public void playOverLoadWarning()
 */
public class ElevatorFloorDisplay {
    private final gui.GUIControl guiControl;
    // The ID of the associated elevator
    private final int carId;

    /**
     * Constructs the ElevatorFloorDisplay.
     * @param carId the ID of the associated elevator
     */
    public ElevatorFloorDisplay(int carId, gui.GUIControl guiControl) {
        this.carId = carId;
        this.guiControl = guiControl;
    }

    /**
     * Updates the display to show the elevator's current floor and direction.
     * @param currentFloor the floor currently displayed
     * @param direction the direction the elevator is going (UP/DOWN/IDLE)
     */
    public synchronized void updateFloorIndicator(int currentFloor, String direction) {
        guiControl.setDisplay(carId, currentFloor, direction);
    }

    /**
     * Simulates the arrival noise.
     */
    public void playArrivalChime() {
        System.out.println("*Ding! Elevator " + carId + " has arrived.");
        playSound("res/sounds/ding.mp3", "Arrival sound");
    }

    /**
     * Simulates the overload buzz.
     */
    public void playOverLoadWarning() {
        System.out.println("*Buzz! Elevator " + carId + " is overloaded!");
        playSound("res/sounds/buzz.mp3", "Overload sound");
    }
    
    /**
     * Helper method to play sounds
     */
    private void playSound(String filePath, String soundName) {
        new Thread(() -> {
            try {
                File soundFile = new File(filePath);
                if (!soundFile.exists()) {
                    System.err.println(soundName + " file not found: " + soundFile.getAbsolutePath());
                    return;
                }
                
                // Use Platform.runLater for MediaPlayer creation on JavaFX thread
                // but don't block the calling thread
                Platform.runLater(() -> {
                    try {
                        Media media = new Media(soundFile.toURI().toString());
                        MediaPlayer player = new MediaPlayer(media);
                        
                        // Dispose after playing completes
                        player.setOnEndOfMedia(() -> player.dispose());
                        player.setOnError(() -> {
                            System.err.println("MediaPlayer error for " + soundName + ": " + player.getError());
                            player.dispose();
                        });
                        
                        player.play();
                    } catch (Exception e) {
                        System.err.println("JavaFX media player error for " + soundName + ": " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("Error playing " + soundName + ": " + e.getMessage());
            }
        }).start();
    }
}

