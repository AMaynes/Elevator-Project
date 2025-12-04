package PFDAPI;

import PFDGUI.gui;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

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
    public synchronized void playArrivalChime() {
        System.out.println("*Ding! Elevator " + carId + " has arrived.");
        Platform.runLater(() -> {
            try {
                URL sound = getClass().getResource("/sounds/ding.mp3");
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

    /**
     * Simulates the overload buzz.
     */
    public synchronized void playOverLoadWarning() {
        Platform.runLater(() -> {
            try {
                URL sound = getClass().getResource("/sounds/buzz.mp3");
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

