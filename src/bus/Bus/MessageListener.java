package bus.Bus;

import bus.Message.Message;

public interface MessageListener {
    void onMessage(Message message);
}
