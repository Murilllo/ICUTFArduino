package amplastudio.csvgenerator.fragments.bluetooth;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import amplastudio.csvgenerator.MainActivity;
import amplastudio.csvgenerator.R;
import amplastudio.csvgenerator.fragments.bluetooth.adapter.DeviceChooserAdapter;
import amplastudio.csvgenerator.fragments.bluetooth.communicator.BluetoothCommunicator;

/**
 * Shows a list of devices to connect.
 */
public class BluetoothDeviceChooserDialogFragment extends AppCompatDialogFragment implements View.OnClickListener, DialogInterface.OnShowListener, DialogInterface.OnClickListener, DeviceChooserAdapter.OnDeviceChosenListener{

    /**
     * Extra that identify devices nearby list in bundle, to restore dialog state in case of screen rotation
     */
    private static final String EXTRA_DEVICES_NEARBY = "devices_nearby";

    private AlertDialog alertDialog;
    private BluetoothCommunicator communicator;

    private DeviceChooserAdapter adapter;

    public BluetoothDeviceChooserDialogFragment(){
        communicator = BluetoothCommunicator.getInstance();
    }


    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState){

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_bluetooth_devices, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(getResources().getString(R.string.dialog_fragment_bluetooth_devices_title))
                .setPositiveButton(getResources().getString(R.string.dialog_fragment_bluetooth_devices_positive_button), this)
                .setNegativeButton(getResources().getString(R.string.dialog_fragment_bluetooth_devices_negative_button), this);

        RecyclerView devicesRecyclerView = (RecyclerView) v.findViewById(R.id.dialog_fragment_bluetooth_devices_recycler_view);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        devicesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new DeviceChooserAdapter(getContext());
        adapter.setOnDeviceChosenListener(this);
        adapter.setPairedDevices(communicator.getPairedDevices());
        devicesRecyclerView.setAdapter(adapter);

        if(savedInstanceState != null){ /* restore previous state before screen rotation */
            adapter.setDevicesNearby((ArrayList<BluetoothDevice>) savedInstanceState.getSerializable(EXTRA_DEVICES_NEARBY));
        }

        alertDialog = builder.create();
        alertDialog.setOnShowListener(this);

        return alertDialog;
    }


    @Override
    public void onShow(DialogInterface dialog) {
        Button positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setOnClickListener(this);
    }

    @Override
    public void onPause(){

        communicator.stopDiscovery();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(getResources()
                .getString(R.string.dialog_fragment_bluetooth_devices_positive_button));

        super.onPause();
    }

    @Override
    public void onClick(View v) {

        if(communicator.isDiscovering()){
            communicator.stopDiscovery();
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(getResources()
            .getString(R.string.dialog_fragment_bluetooth_devices_positive_button));
        }

        else{
            scanDevices();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putSerializable(EXTRA_DEVICES_NEARBY, adapter.getDevicesNearby());
    }



    /* behaviour overridden since after this call the dialog is dismissed,
    this function is here only because it is needed to create the positive and negative buttons */
    @Override
    public void onClick(DialogInterface dialog, int which) {}

    @Override
    public void onDeviceChosen(BluetoothDevice device) {
        communicator.stopDiscovery();
        communicator.startListen(device);
        dismiss();
    }

    /**
     * Updates the dialog fragment if a bluetooth device was found
     * @param device Device to be displayed
     */
    public void updateNearbyDevices(BluetoothDevice device){
        adapter.addDeviceNearby(device);
    }

    /**
     * Start to scan nearby devices, updating the dialog list when a new device is found.
     */
    public void scanDevices(){
        adapter.clearDevicesNearbyList();
        if(!communicator.connect()) return;
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(getResources()
                .getString(R.string.dialog_fragment_bluetooth_devices_positive_button_stop_scanning));
    }

    /**
     * Warns this dialog that the bluetooth device discovery is finished in order to enable the dialog positive button,
     * so user can start a new device scanning
     */
    public void onDeviceScanFinished(){
        if(alertDialog == null) return; /* can be null if this function is called after a screen rotation */
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(getResources().getString(R.string.dialog_fragment_bluetooth_devices_positive_button));
        // put no devices found in list
    }

}
