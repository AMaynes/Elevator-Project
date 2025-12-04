package Util;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class for playing sounds throughout the elevator system
 */
public class SoundUtil {
    
    /**
     * Play a button click sound
     * Removed debouncing - each button press should make a sound
     */
    public static void playButtonClick() {
        playSound("res/sounds/call_button_click.mp3", "Button click", null);
    }
    
    /**
     * Play an elevator motion sound
     */
    public static void playMotionSound() {
        playSound("res/sounds/elevator_motion.mp3", "Elevator motion", null);
    }
    
    /**
     * Generic method to play any sound file
     */
    private static void playSound(String filePath, String soundName, AtomicBoolean playingFlag) {
        new Thread(() -> {
            try {
                File soundFile = new File(filePath);
                if (!soundFile.exists()) {
                    if (playingFlag != null) playingFlag.set(false);
                    return;
                }
                
                // Use Platform.runLater for MediaPlayer creation on JavaFX thread
                Platform.runLater(() -> {
                    try {
                        Media media = new Media(soundFile.toURI().toString());
                        MediaPlayer player = new MediaPlayer(media);
                        player.setVolume(0.5); // 50% volume for button clicks
                        
                        player.setOnEndOfMedia(() -> {
                            player.dispose();
                            if (playingFlag != null) playingFlag.set(false);
                        });
                        player.setOnError(() -> {
                            player.dispose();
                            if (playingFlag != null) playingFlag.set(false);
                        });
                        
                        player.play();
                    } catch (Exception e) {
                        if (playingFlag != null) playingFlag.set(false);
                    }
                });
            } catch (Exception e) {
                if (playingFlag != null) playingFlag.set(false);
            }
        }).start();
    }
}
