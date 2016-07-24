package amplastudio.csvgenerator.fragments.bluetooth.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import amplastudio.csvgenerator.R;

/**
 * Adapter that handle how paired and discovered devices is in a recycler view
 */
public class DeviceChooserAdapter extends RecyclerView.Adapter<DeviceChooserAdapter.Holder> implements View.OnClickListener{

    private BluetoothDevice[] pairedDevices;
    private ArrayList<BluetoothDevice> devicesNearby = new ArrayList<>(50);

    private final String pairedDevicesTitle;
    private final String devicesNearbyTitle;

    private OnDeviceChosenListener listener;

    protected class Holder extends RecyclerView.ViewHolder{

        public TextView rowText;
        public View itemView;

        public Holder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            rowText = (TextView) itemView.findViewById(R.id.dialog_fragment_bluetooth_devices_list_row_textview);
        }

    }

    public DeviceChooserAdapter(Context context){
        pairedDevicesTitle = context.getResources().getString(R.string.dialog_fragment_bluetooth_devices_list_row_title_paired);
        devicesNearbyTitle = context.getResources().getString(R.string.dialog_fragment_bluetooth_devices_list_row_title_nearby);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dialog_fragment_bluetooth_devices_list_row, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {

        if(pairedDevices != null && pairedDevices.length > 0) {

            if (position == 0){
                holder.rowText.setText(pairedDevicesTitle);
                holder.itemView.setClickable(false);
            }
            else if (position == pairedDevices.length + 1 /* +1 since title is in 0 and list will begin in 1 */) {
                holder.rowText.setText(devicesNearbyTitle);
                holder.itemView.setClickable(false);
            }
            else if(position <= pairedDevices.length){
                holder.rowText.setText(pairedDevices[position - 1 /* -1 since title is in 0 */ ].getName());
                holder.itemView.setTag(pairedDevices[position - 1]);
                holder.itemView.setClickable(true);
                holder.itemView.setOnClickListener(this);
            }
            else{
                /* - pairedDevices.length - 2 since there is 2 titles and a list of paired devices before the list of available devices */
                holder.rowText.setText(devicesNearby.get(position - pairedDevices.length - 2 ).getName());
                holder.itemView.setTag(devicesNearby.get(position - pairedDevices.length - 2));
                holder.itemView.setClickable(true);
                holder.itemView.setOnClickListener(this);
            }

        }

        else if(position == 0) {
            holder.rowText.setText(devicesNearbyTitle);
            holder.itemView.setClickable(false);
        }

        else{
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(this);
            holder.rowText.setText(devicesNearby.get(position - 1 /* -1 since title is in 0 */).getName());
            holder.itemView.setTag(devicesNearby.get(position - 1));
        }

    }

    @Override
    public int getItemCount() {
        int numPairedDevices = 0;
        if(pairedDevices != null) numPairedDevices = pairedDevices.length + 1 /* +1 for title */;

        return numPairedDevices + (!devicesNearby.isEmpty() ? devicesNearby.size() + 1 : 0);
    }

    @Override
    public void onClick(View v) {

        if(listener != null) listener.onDeviceChosen((BluetoothDevice) v.getTag());

    }

    /**
     * Enable listener to know whenever a device was chosen from list
     * @param listener Listener to listen for chosen event
     */
    public void setOnDeviceChosenListener(OnDeviceChosenListener listener){
        this.listener = listener;
    }

    /**
     * Update the list of paired devices
     * @param pairedDevices List of paired devices
     */
    public void setPairedDevices(BluetoothDevice[] pairedDevices){
        this.pairedDevices = pairedDevices;
        notifyDataSetChanged();
    }

    /**
     *
     * @return List of devices nearby
     */
    public ArrayList<BluetoothDevice> getDevicesNearby(){
        return devicesNearby;
    }

    /**
     *
     * @param devicesNearby Devices nearby list to be displayed.
     */
    public void setDevicesNearby(ArrayList<BluetoothDevice> devicesNearby){
        this.devicesNearby = devicesNearby;
        notifyDataSetChanged();
    }

    /**
     * Updates the list of devices nearby
     * @param bluetoothDevice Device to be displayed
     */
    public void addDeviceNearby(BluetoothDevice bluetoothDevice){
        devicesNearby.add(bluetoothDevice);
        notifyDataSetChanged();
    }

    /**
     * Clear the devices nearby list
     */
    public void clearDevicesNearbyList(){
        if(!devicesNearby.isEmpty()) devicesNearby.clear();
        notifyDataSetChanged();
    }

    /**
     * Interface that warns when an device was chosen.
     */
    public interface OnDeviceChosenListener {

        /**
         * Called when a device is chosen from a list
         * @param device Device chosen
         */
        void onDeviceChosen(BluetoothDevice device);

    }


}
