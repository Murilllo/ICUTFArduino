package amplastudio.csvgenerator.fragments.bluetooth.communicator;

import android.os.Handler;
import android.support.annotation.Nullable;

/**
 * Class that defines way to communicate with something, with default way to receive callback messages.
 * Callback messages are sent via {@link Handler}, you must give it with {@link #setHandlerForCallback(Handler)}
 * or using {@link #Communicator(Handler)}
 */
public abstract class Communicator {

    private Handler mHandler;

    protected Communicator(){ mHandler = null; }

    /**
     *
     * @param handlerForCallback Handler that will receive callback messages.
     */
    protected Communicator(@Nullable Handler handlerForCallback){
        mHandler = handlerForCallback;
    }

    protected void sendCallbackMessage(int what, Object obj){

        if(mHandler != null) mHandler.obtainMessage(what, obj).sendToTarget();

    }

    /**
     *
     * @return Current Handler used to receive callback messages.
     */
    protected Handler getHandlerForCallback(){
        return mHandler;
    }

    /**
     *
     * @param handlerForCallback Handler that will receive callback messages
     */
    protected void setHandlerForCallback(@Nullable Handler handlerForCallback){ this.mHandler = handlerForCallback; }

}
