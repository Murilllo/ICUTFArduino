package amplastudio.csvgenerator.fragments.bluetooth.communicator;

/**
 * Interface which contains methods do connect or disconnect from something
 */
public interface Connectable {

    /**
     *
     * @return True if connection attempt success
     */
    boolean connect();

    /**
     * Disconnect from something
     */
    void disconnect();

    /**
     *
     * @return True if is connected, false otherwise
     */
    boolean isConnected();
}
