package TestElevatorController;

import Bus.SoftwareBus;
import ElevatorController.LowerLevel.Notifier;
import Message.Topic;
import Message.Message;

import javax.xml.transform.Source;
import java.sql.SQLOutput;

/**
 * For testing the Notifier object in ElevatorController/LowerLevel
 */
public class TestNotifier {
    // Topic for updating car position
    private static final int TOPIC_DISPLAY_FLOOR = Topic.displayFloor;
    private static final int TOPIC_DISPLAY_DIREC = Topic.displayDirection;

    // Topics for playing sounds
    private static final int TOPIC_SPEAKER = Topic.playSound;

    static void main() {
        Message message;
        int elevator;
        int testsPassed = 0;
        SoftwareBus serverBus = new SoftwareBus(true);
        SoftwareBus clientBus = new SoftwareBus(false);
        Notifier notifier1 = new Notifier(1, serverBus);
        Notifier notifier2 = new Notifier(2, serverBus);

        // TEST 1 playCapacityNoise()
        elevator = 2;
        notifier2.playCapacityNoise();
        message = serverBus.get(TOPIC_SPEAKER, elevator);
        if (message != null) {
            if (message.getTopic() != TOPIC_SPEAKER){
                System.out.println("ERROR: in software bus? getting from topic = "
                        + TOPIC_SPEAKER +", receiving topic = " + message.getTopic());
            } else if (message.getSubTopic() != elevator){
                System.out.println("ERROR: in software bus? getting from subtopic = "
                        + elevator + ", recieving from subtopic = " + message.getSubTopic());
            } else if (message.getBody() != Topic.emptyBody){
                System.out.println("ERROR: sending wrong body, sending body = " +
                        message.getBody() + ", expecting body = " + Topic.emptyBody);
            } else {
                // Passed test for playCapacity
                testsPassed++;
            }
        } else {
            System.out.println("ERROR: playCapacityNoise() not sending message");
        }

        // TEST 2 stopCapacityNoise()
        elevator = 1;
        notifier1.stopCapacityNoise();
        message =  serverBus.get(TOPIC_SPEAKER, elevator);
        if (message != null) {
            if (message.getTopic() != TOPIC_SPEAKER){
                System.out.println("Error in software bus? getting from topic = "
                        + TOPIC_SPEAKER +", receiving topic = " + message.getTopic());
            }
            if (message.getSubTopic() != elevator){
                System.out.println("Error in software bux? getting from subtopic = "
                        + elevator + ", recieving from subtopic = " + message.getSubTopic());
            }
            // TODO no known body for stopping the overweight buzzer
        } else  {
            System.out.println("stopCapacityNoise() not sending message");
        }

        System.out.println("testsPassed: " + testsPassed);
    }
}
