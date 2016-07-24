package amplastudio.csvgenerator.fragments.bluetooth.communicator;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Holds a defined set of data.
 */
public interface DataHolder {

    /**
     *
     * @return Held data
     */
    Object getHeldData();

    /**
     * Release all held data.
     */
    void releaseData();

}
