/**
 * SOFTWARE BUS: SoftwareBusClients Class obtained from a separate group.
 * TODO: This doesn't even do anything lol what the helly
 */
public class SoftwareBusClients {
    // The software bus
    private SoftwareBus softwareBus;

    /**
     * Constructor for the SoftwareBusClients.
     * @param softwareBus the utilized software bus
     */
    public SoftwareBusClients(SoftwareBus softwareBus) {
        this.softwareBus = softwareBus;
    }

    /**
     * Sends a given message to each connected client.
     * @param message Message to be sent
     */
    public void broadCast(Message message) {

    }
}