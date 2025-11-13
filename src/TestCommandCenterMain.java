import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Runnable - test version of the command center that utilizes the bus to send messages.
 */
public class TestCommandCenterMain extends Application {
    // Topic 1
    private int topic = 2;
    // Subtopic 1
    private int subtopic = 0;
    // Topic 2
    private int otherTopic = 3;
    // Subtopic 2
    private int otherSubtopic = 0;

    /**
     * Set up the TCC's stage and bus.
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     */
    @Override
    public void start(Stage primaryStage) {
        SoftwareBus softwareBus = new SoftwareBus(true);
        softwareBus.subscribe(topic, subtopic);
        softwareBus.subscribe(otherTopic, otherSubtopic);

        TestCommandCenterDisplay display = new TestCommandCenterDisplay(softwareBus, topic, subtopic);

        primaryStage.setTitle("Test Command Center, subscribed to t" +
                topic + ":s" + subtopic + ", t" + otherTopic + "s:" + otherSubtopic);
        Scene scene = new Scene(display.getPane());
        primaryStage.setScene(scene);
        primaryStage.setX(0);
        primaryStage.setY(0);

        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    /**
     * Launch the TCC.
     * @param args commandline arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}