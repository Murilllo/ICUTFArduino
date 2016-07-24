package amplastudio.csvgenerator.fragments.bluetooth.communicator;

/**
 * Class that define basic methods to listen for external messages and send messages
 */
public interface MessageListen<T, U> {

    /**
     * Send data to target
     * @param data Data to be sent
     */
    void sendData(U data);

    /**
     * Start listen for external messages
     * @param what Abstract target to start listen
     */
    void startListen(T what);

    /**
     * Stop listen for external messages
     * @param what Abstract to stop listen
     */
    void stopListen(T what);

}
