package amplastudio.csvgenerator.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import amplastudio.csvgenerator.MainActivity;
import amplastudio.csvgenerator.R;
import amplastudio.csvgenerator.fragments.adapter.SerialMonitorRecyclerViewAdapter;
import amplastudio.csvgenerator.fragments.bluetooth.BluetoothDeviceChooserDialogFragment;
import amplastudio.csvgenerator.fragments.bluetooth.communicator.BluetoothCommunicator;

/**
 * Handles the serial monitor and edittext that receives string to be sent to a bluetooth device
 */
public class SerialStreamFragment extends Fragment implements Button.OnClickListener, BluetoothCommunicator.Callback {

    /**
     * Request code for activity result.
     */
    private static final int BLUETOOTH_REQUEST_CODE = 10;

    /**
     * Identifies {@link #isConnecting} in Bundle
     */
    private static final String KEY_IS_CONNECTING = "bundle_connecting";

    private View connectingInfoHolder;
    private EditText serialEditText;
    private FloatingActionButton sendSerialButton;
    private BluetoothCommunicator communicator;

    private RecyclerView serialMonitorRecyclerView;
    private SerialMonitorRecyclerViewAdapter serialMonitorRecyclerViewAdapter;

    private BluetoothDeviceChooserDialogFragment deviceChooserDialogFragment = new BluetoothDeviceChooserDialogFragment();

    private boolean isConnecting = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);

        View layoutView = inflater.inflate(R.layout.fragment_serial_stream, container, false);

        communicator = BluetoothCommunicator.getInstance();
        communicator.setCallback(this);

        serialEditText = (EditText) layoutView.findViewById(R.id.fragment_serial_stream_edittext_serial);
        sendSerialButton = (FloatingActionButton) layoutView.findViewById(R.id.fragment_serial_stream_button_sendSerial);
        serialMonitorRecyclerView = (RecyclerView) layoutView.findViewById(R.id.fragment_serial_stream_recyclerview_serialMonitor);
        connectingInfoHolder = layoutView.findViewById(R.id.fragment_serial_stream_connecting_holder);

        assert serialEditText != null;
        assert sendSerialButton != null;
        assert serialMonitorRecyclerView != null;

        sendSerialButton.setOnClickListener(this);

        if(serialMonitorRecyclerView.getAdapter() == null) {
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(inflater.getContext());
            serialMonitorRecyclerView.setLayoutManager(linearLayoutManager);
            serialMonitorRecyclerView.setItemAnimator(new DefaultItemAnimator());
            serialMonitorRecyclerViewAdapter = new SerialMonitorRecyclerViewAdapter();
            serialMonitorRecyclerView.setAdapter(serialMonitorRecyclerViewAdapter);
        }

        if(savedInstanceState != null){
            isConnecting = savedInstanceState.getBoolean(KEY_IS_CONNECTING);
            if(isConnecting)
                connectingInfoHolder.setVisibility(View.VISIBLE);
        }

        return layoutView;
    }

    /**
     * Clears the monitor
     */
    public void clearMonitor(){
        serialMonitorRecyclerViewAdapter.clearSerialList();
    }

    /**
     * Connect to a bluetooth device
     */
    public void connectBluetooth(){
        if(!communicator.isBluetoothEnabled())
            requestBluetoothEnable();
        else
            deviceChooserDialogFragment.show(getActivity().getSupportFragmentManager(), SerialStreamFragment.class.getName());
    }

    /**
     * Disconnect bluetooth connection, releasing system resources.
     */
    public void disconnectBluetooth(){
        communicator.disconnect();
    }

    /**
     * Exports the monitor data to file
     */
    public void exportSerialMonitor(){
        if(BluetoothCommunicator.getInstance().getHeldData().isEmpty()){
            Toast.makeText(getContext(), getResources().getString(R.string.file_export_nothing), Toast.LENGTH_SHORT).show();
            //return;
        }
        ExportSerialStreamDialogFragment dialog = new ExportSerialStreamDialogFragment();
        dialog.show(getActivity().getSupportFragmentManager(), ExportSerialStreamDialogFragment.class.getName());
    }


    @Override
    public void onClick(View view){

        switch (view.getId()){

            case R.id.fragment_serial_stream_button_sendSerial:

                if(!communicator.isConnected()){
                    connectBluetooth();
                    return;
                }
                String input = serialEditText.getText().toString();
                if(input.equals("")) return;

                communicator.sendData(input.getBytes());
                break;

            default:
                communicator.sendData("s".getBytes());

        }
        serialEditText.setText("");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==  BLUETOOTH_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            connectBluetooth(); /* attempt to connect again */
        }

    }

    @Override
    public void onDestroy(){
        //communicator.disconnect();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putBoolean(KEY_IS_CONNECTING, isConnecting);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onBluetoothCommunicatorCallBack(BluetoothCommunicator.CallbackAction action) {

        switch (action.getAction()){

            case BluetoothCommunicator.ACTION_CONNECTION_LOST:
                communicator.disconnect();
                break;

            case BluetoothCommunicator.ACTION_BLUETOOTH_NOT_ENABLED:
                requestBluetoothEnable();
                break;

            case BluetoothCommunicator.ACTION_DATA_RECEIVED:
                serialMonitorRecyclerViewAdapter.notifyDataSetChanged();
                serialMonitorRecyclerView.scrollToPosition(serialMonitorRecyclerViewAdapter.getItemCount() - 1);
                break;

            case BluetoothCommunicator.ACTION_DEVICE_FOUND:
                deviceChooserDialogFragment.updateNearbyDevices((BluetoothDevice) action.getActionData());
                break;

            case BluetoothCommunicator.ACTION_DEVICE_CONNECTED:
                connectingInfoHolder.setVisibility(View.GONE);
                Toast.makeText(getContext(), getResources().getString(R.string.bluetooth_connection_success), Toast.LENGTH_LONG).show();
                deviceChooserDialogFragment.dismiss();
                ((MainActivity) getActivity()).displayConnectedBluetoothMenuIcon();
                break;

            case BluetoothCommunicator.ACTION_BLUETOOTH_NOT_SUPPORTED:

                break;

            case BluetoothCommunicator.ACTION_CONNECTION_FAILED:
                isConnecting = false;
                connectingInfoHolder.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), getResources().getString(R.string.bluetooth_connection_failed), Toast.LENGTH_LONG).show();
                break;

            case BluetoothCommunicator.ACTION_CONNECTION_CLOSED:
                Toast.makeText(getContext(), getResources().getString(R.string.bluetooth_connection_closed)
                        , Toast.LENGTH_SHORT).show();
                ((MainActivity) getActivity()).displayEnableBluetoothMenuIcon();
                break;

            case BluetoothCommunicator.ACTION_DISCOVERY_FINISHED:

                try { /* because the dialog may be closed and this exception will be thrown,
                       * we can ignore the error since this method call is only intent to update a dialog button */
                    deviceChooserDialogFragment.onDeviceScanFinished();
                }
                catch(IllegalStateException e){
                    Log.e(SerialStreamFragment.class.getName(), "Acceptable exception since we are trying to update a dialog that was already closed");
                    e.printStackTrace();
                }

                break;

            case BluetoothCommunicator.ACTION_CONNECTION_ATTEMPT:
                connectingInfoHolder.setVisibility(View.VISIBLE);
                break;

        }

    }

    private void requestBluetoothEnable(){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, BLUETOOTH_REQUEST_CODE);
    }
}
