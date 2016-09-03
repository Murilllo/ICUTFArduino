package amplastudio.csvgenerator.fragments.bluetooth.communicator;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


/**
 * <p>Class intent to receive and send messages via bluetooth communication.</p>
 * <p>Uses singleton pattern to manage connections,
 * use {@link #getInstance()} to get class instance. Also, don't forget to pass the app context with {@link #register(Context)},
 * this function registers all receivers needed for this class to work.</p>
 * <p>This class holds the data received by the bluetooth device connected to this device and can be accessed via {@link #getHeldData()},
 * this avoid possible data miss due to screen orientation changes, which can consume significant time. </p>
 */
public class BluetoothCommunicator extends Communicator implements Connectable, DataHolder, MessageListen<BluetoothDevice, byte[]>{

    /**
     * Static inner class that holds outer class instance, allowing thread-safe singleton pattern
     */
    private static class InstanceHolder{
        private static final BluetoothCommunicator INSTANCE = new BluetoothCommunicator();
    }

    /**
     * Flag set when a message is received from connected bluetooth device. Message object is an instance of {@link String}
     */
    private static final int MESSAGE_DATA_RECEIVED = 1;

    /**
     * Flag set when a device is found from discovery. Message object is an instance of {@link BluetoothDevice}.
     */
    private static final int MESSAGE_DEVICE_FOUND = 2;

    /**
     * Flag set when a connection is lost, when this flag is set, this class automatically calls
     * {@link #stopListen(Object)}. Message object is null.
     */
    private static final int MESSAGE_CONNECTION_LOST = 3;

    /**
     * Flag set when bluetooth is not enabled and should be turned on. Message object is null.
     */
    private static final int MESSAGE_BLUETOOTH_NOT_ENABLED = 4;

    /**
     * Flag set when the device successfully connected to device. When this flag is set, this class
     * enable data listen automatically. Message object is null.
     */
    private static final int MESSAGE_DEVICE_CONNECTED = 5;

    /**
     * Flag set when bluetooth is not supported on this device. Message object is null.
     */
    private static final int MESSAGE_BLUETOOTH_NOT_SUPPORTED = 6;

    /**
     * Flag set when device failed to connect to another bluetooth device. When this flag is set, this
     * class disable data listen automatically. Message object is null.
     */
    private static final int MESSAGE_CONNECTION_FAILED = 7;

    /**
     * Flag set when connections is closed. Message object is null.
     */
    private static final int MESSAGE_CONNECTION_CLOSED = 8;

    /**
     * Flag set when bluetooth discovery finished. Message object is null
     */
    private static final int MESSAGE_DISCOVERY_FINISHED = 9;

    /**
     * Flag set when this device started to connect to another device. Message object is null
     */
    private static final int MESSAGE_CONNECTION_ATTEMPT = 10;

    /**
     * Flag set when a message is received from connected bluetooth device.
     * Action data is the same instance returned by {@link #getHeldData()}
     */
    public static final int ACTION_DATA_RECEIVED = MESSAGE_DATA_RECEIVED;

    /**
     * Flag set when a device is found from discovery. Action data is an instance of {@link BluetoothDevice}.
     */
    public static final int ACTION_DEVICE_FOUND = MESSAGE_DEVICE_FOUND;

    /**
     * Flag set when a connection is lost, when this flag is set,
     * {@link #stopListen(Object)} is called automatically. Action data is null.
     */
    public static final int ACTION_CONNECTION_LOST = MESSAGE_CONNECTION_LOST;

    /**
     * Flag set when bluetooth is not enabled and should be turned on. Action data is null.
     */
    public static final int ACTION_BLUETOOTH_NOT_ENABLED = MESSAGE_BLUETOOTH_NOT_ENABLED;

    /**
     * Flag set when the device successfully connected to device. When this flag is set,
     * data listen is enabled automatically and can be accessed via {@link #getHeldData()}. Action data is null.
     * @see #ACTION_DATA_RECEIVED
     */
    public static final int ACTION_DEVICE_CONNECTED = MESSAGE_DEVICE_CONNECTED;

    /**
     * Flag set when bluetooth is not supported on this device. Action data is null.
     */
    public static final int ACTION_BLUETOOTH_NOT_SUPPORTED = MESSAGE_BLUETOOTH_NOT_SUPPORTED;

    /**
     * Flag set when device failed to connect to another bluetooth device. When this flag is set,
     * data listen is disabled automatically. Action object is null.
     */
    public static final int ACTION_CONNECTION_FAILED = MESSAGE_CONNECTION_FAILED;

    /**
     * Flag set when connections is closed. Action data is null.
     */
    public static final int ACTION_CONNECTION_CLOSED = MESSAGE_CONNECTION_CLOSED;

    /**
     * Flag set when bluetooth discovery finished. Action data is null
     */
    public static final int ACTION_DISCOVERY_FINISHED = MESSAGE_DISCOVERY_FINISHED;

    /**
     * Flag set when this device is attempting do connect to another device. Action data is null
     */
    public static final int ACTION_CONNECTION_ATTEMPT = MESSAGE_CONNECTION_ATTEMPT;

    /**
     * Data received from bluetooth communication. Accessed via {@link #getHeldData()}
     */
    private ArrayList<String> receivedData = new ArrayList<>(200);

    private BluetoothAdapter bluetoothAdapter;

    private Context context;

    /**
     * Thread to listen for data receive.
     */
    private ConnectThread connectThread = null;

    /**
     * Indicates whether the {@link #bluetoothConnectionReceiver} is registered.
     */
    private boolean isBluetoothConnectionReceiverRegistered;

    /**
     * Indicates whether the {@link #bluetoothDiscoveryReceiver} is registered.
     */
    private boolean isBluetoothDiscoveryReceiverRegistered;

    private final IntentFilter bluetoothConnectionActionFilter;

    private final IntentFilter bluetoothDiscoveryActionFilter;

    /**
     * Flag that indicates whether this app is connected to another bluetooth device
     */
    private boolean isConnected = false;

    /**
     * Call back for bluetooth events
     */
    private Callback mCallBack;


    /**
     * Receiver triggered when bluetooth connection is made or lost.
     */
    private final BroadcastReceiver bluetoothConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()){

                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    isConnected = true;
                    sendCallbackMessage(MESSAGE_DEVICE_CONNECTED, null);
                    connectThread.start(); /* start listen */
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    isConnected = false;
                    sendCallbackMessage(MESSAGE_CONNECTION_LOST, null);
                    stopListen(null);
                    break;

            }


        }
    };

    /**
     * Receiver triggered when bluetooth discovery found a new available device or discovery finished.
     */
    private final BroadcastReceiver bluetoothDiscoveryReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    sendCallbackMessage(MESSAGE_DEVICE_FOUND, device);
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    sendCallbackMessage(MESSAGE_DISCOVERY_FINISHED, null);
                    break;
            }
        }

    };

    /**
     * Handler that receive messages from bluetooth events.
     */
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){

                case BluetoothCommunicator.MESSAGE_DATA_RECEIVED:
                    receivedData.add((String) msg.obj);
                    if(mCallBack != null) mCallBack.onBluetoothCommunicatorCallBack(new CallbackAction(msg.what, getHeldData()));
                    break;

                case BluetoothCommunicator.MESSAGE_DEVICE_FOUND:
                    if(mCallBack != null) mCallBack.onBluetoothCommunicatorCallBack(new CallbackAction(msg.what, msg.obj));
                    break;

                default:
                    if(mCallBack != null) mCallBack.onBluetoothCommunicatorCallBack(new CallbackAction(msg.what, null));

            }

        }

    };

    /**
     * Due to singleton pattern, a context must be given in order to this class work properly, use {@link #register(Context)}
     * before any other function call.
     * @return Bluetooth communicator instance
     */
    public static BluetoothCommunicator getInstance(){
        return InstanceHolder.INSTANCE;
    }

    private BluetoothCommunicator(){
        super();
        setHandlerForCallback(mHandler);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothDiscoveryActionFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        bluetoothDiscoveryActionFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        bluetoothConnectionActionFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        bluetoothConnectionActionFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
    }

    /**
     *
     * @return True if bluetooth is enabled, false otherwise
     */
    public boolean isBluetoothEnabled(){
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * Listen for bluetooth events.
     * @param callback Callback instance
     * @see Callback
     */
    public void setCallback(Callback callback){
        this.mCallBack = callback;
    }

    /**
     * Registers all receivers. Aways call this function before any other function calls.
     * It is recommended to call this function in {@link Activity#onResume()}, unregisters occurs with
     * {@link #unregister()}, which may be called in {@link Activity#onPause()} as suggested in {@link BroadcastReceiver} docs.
     */
    public void register(Context context){
        this.context = context;
        context.registerReceiver(bluetoothConnectionReceiver, bluetoothConnectionActionFilter);
        context.registerReceiver(bluetoothDiscoveryReceiver, bluetoothDiscoveryActionFilter);
        isBluetoothDiscoveryReceiverRegistered = true;
        isBluetoothConnectionReceiverRegistered = true;
    }

    /**
     * Unregister class receivers, without disconnecting any device connections or stop discovering.
     * @see #register(Context)
     * @see #disconnect()
     * @see #stopDiscovery()
     */
    public void unregister(){
        if(isBluetoothDiscoveryReceiverRegistered) {
            context.unregisterReceiver(bluetoothDiscoveryReceiver);
            isBluetoothDiscoveryReceiverRegistered = false;
        }

        if(isBluetoothConnectionReceiverRegistered){
            context.unregisterReceiver(bluetoothConnectionReceiver);
            isBluetoothConnectionReceiverRegistered = false;
        }
    }

    /**
     *
     * @return True if the bluetooth is scanning for available devices nearby.
     */
    public boolean isDiscovering(){
        return bluetoothAdapter != null && bluetoothAdapter.isDiscovering();
    }

    /**
     * Start device discovery and send messages to target handler. To stop discovery, use {@link #stopDiscovery()}.
     * Be sure you called {@link #register(Context)} before call this method.
     * @return True if discovery started, false if bluetooth is not supported or bluetooth is not enabled.
     * @see #sendCallbackMessage(int, Object)
     * @see #MESSAGE_DEVICE_FOUND
     * @see #MESSAGE_BLUETOOTH_NOT_ENABLED
     * @see #MESSAGE_BLUETOOTH_NOT_SUPPORTED
     */
    @Override
    public boolean connect() {

        if(bluetoothAdapter == null){
            sendCallbackMessage(MESSAGE_BLUETOOTH_NOT_SUPPORTED, null);
            return false;
        }

        if(!bluetoothAdapter.isEnabled()){
            sendCallbackMessage(MESSAGE_BLUETOOTH_NOT_ENABLED, null);
            return false;
        }

        bluetoothAdapter.startDiscovery();
        return true;

    }

    /**
     * Release any bluetooth resources, such as stop discovering, stop listening for messages, etc.
     * @see #register(Context)
     */
    @Override
    public void disconnect() {

        stopDiscovery();

        if(connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        sendCallbackMessage(MESSAGE_CONNECTION_CLOSED, null);

    }

    /**
     *
     * @return All received data from bluetooth communication.
     */
    @Override
    public @NonNull ArrayList<String> getHeldData() {
        return receivedData;
    }

    /**
     * Clears all received data from bluetooth communication,
     */
    @Override
    public void releaseData(){
        receivedData.clear();
    }

    /**
     * Send data to the connected device.
     * @param data Data to be sent
     */
    @Override
    public void sendData(byte[] data) {

        if(connectThread == null) return;
        connectThread.write(data);

    }

    /**
     * Start listening for data received from the connected bluetooth device.
     * To be notified when data is received, use {@link #setCallback(Callback)} and
     * check for the flag {@link #ACTION_DATA_RECEIVED}
     * @param device Available device to connect.
     */
    @Override
    public void startListen(BluetoothDevice device) {
        if(connectThread != null) connectThread.cancel();
        connectThread = new ConnectThread(device);
    }

    /**
     * Calls {@link #disconnect()}
     * @param device Parameter ignored, can be null
     */
    @Override
    public void stopListen(@Nullable BluetoothDevice device) {
        disconnect();
    }

    /**
     * Cancel bluetooth device discovery and release resources.
     * To start discovery, use {@link #connect()}
     */
    public void stopDiscovery(){
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }

    /**
     * @return True if is connected to a device, false otherwise
     */
    @Override
    public boolean isConnected(){
        return isConnected;
    }

    /**
     *
     * @return Null whether bluetooth is not supported or there is no paired devices, a list of paired devices otherwise
     */
    public @Nullable BluetoothDevice[] getPairedDevices(){
        if(bluetoothAdapter == null){
            sendCallbackMessage(MESSAGE_BLUETOOTH_NOT_SUPPORTED, null);
            return null;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() == 0) return null;

        BluetoothDevice[] devices = new BluetoothDevice[pairedDevices.size()];
        pairedDevices.toArray(devices);
        return devices;
    }


    private class ConnectThread extends Thread{

        private BluetoothSocket bluetoothSocket = null;
        private InputStream inputStream;
        private OutputStream outputStream;

        /**
         * Bluetooth connections may block the UI thread
         */
        private volatile Thread thread;

        public ConnectThread(final BluetoothDevice device){

            thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try{
                        sendCallbackMessage(MESSAGE_CONNECTION_ATTEMPT, null);
                        // UUID FROM GOOGLE DOCs found in http://developer.android.com/intl/pt-br/guide/topics/connectivity/bluetooth.html
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                        bluetoothSocket.connect();
                        inputStream = bluetoothSocket.getInputStream();
                        outputStream = bluetoothSocket.getOutputStream();

                    }catch(IOException e){
                        e.printStackTrace();
                        sendCallbackMessage(MESSAGE_CONNECTION_FAILED, null);
                    }

                }
            });

            thread.start();

        }

        @Override
        public void run(){

            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int bytesAvailable;
            int delimiterIndex;
            byte buffer[];

            String data = "";
            StringBuilder stringBuilder = new StringBuilder();

            //final byte delimiter = 10; //This is the ASCII code for newline character

            while(!isInterrupted()){

                try {

                    bytesAvailable = inputStream.available();

                    if(bytesAvailable > 0) {

                        buffer = new byte[bytesAvailable];
                        inputStream.read(buffer);

                        data = new String(buffer, "UTF-8");
                        delimiterIndex = data.indexOf('\n');

                        if(delimiterIndex == -1){
                            stringBuilder.append(data);
                        }

                        else{

                            if(delimiterIndex > 0)
                                stringBuilder.append(data.substring(0, delimiterIndex));

                            sendCallbackMessage(MESSAGE_DATA_RECEIVED, stringBuilder.toString());

                            stringBuilder.setLength(0);
                        }

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

            }

        }

        public void write(byte[] buffer){

            try{
                outputStream.write(buffer);
            }catch(IOException e){
                e.printStackTrace();
            }

        }

        public void cancel(){

            try{

                bluetoothSocket.close();
                inputStream.close();
                outputStream.close();

            }catch(IOException | NullPointerException e){
                e.printStackTrace();
            }

        }

    }

    /**
     * Callback interface for actions that may occur with bluetooth communication.
     * @see CallbackAction
     */
    public interface Callback{

        /**
         *
         * @param action Action that triggers this callback
         */
        void onBluetoothCommunicatorCallBack(CallbackAction action);

    }

    /**
     * Class that contains information about what triggered the callback.
     * @see #getAction()
     */
    public class CallbackAction{
        private int action;
        private Object actionData;

        private CallbackAction(int action, Object actionData){
            this.action = action;
            this.actionData = actionData;
        }

        /**
         *
         * @return Action that triggers the callback.
         * @see #ACTION_BLUETOOTH_NOT_SUPPORTED
         * @see #ACTION_BLUETOOTH_NOT_ENABLED
         * @see #ACTION_CONNECTION_CLOSED
         * @see #ACTION_CONNECTION_FAILED
         * @see #ACTION_CONNECTION_LOST
         * @see #ACTION_DATA_RECEIVED
         * @see #ACTION_DISCOVERY_FINISHED
         * @see #ACTION_DEVICE_CONNECTED
         * @see #ACTION_DEVICE_FOUND
         * @see #ACTION_CONNECTION_ATTEMPT
         */
        public int getAction(){
            return action;
        }

        /**
         *
         * @return Action data. May vary depending on the value returned by {@link #getAction()}
         */
        public Object getActionData(){
            return actionData;
        }
    }

}
