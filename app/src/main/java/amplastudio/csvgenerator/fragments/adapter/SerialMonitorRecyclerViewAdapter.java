package amplastudio.csvgenerator.fragments.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import amplastudio.csvgenerator.R;
import amplastudio.csvgenerator.fragments.bluetooth.communicator.BluetoothCommunicator;

/**
 * Adapter that displays the data held by {@link BluetoothCommunicator#getHeldData()}.
 */
public class SerialMonitorRecyclerViewAdapter extends RecyclerView.Adapter<SerialMonitorRecyclerViewAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView receivedSerialText;

        public ViewHolder(View itemView) {
            super(itemView);
            receivedSerialText = (TextView) itemView.findViewById(R.id.serialMonitor_recyclerview_row_receivedSerial);
        }

    }

    private ArrayList<String> serialTexts;

    public SerialMonitorRecyclerViewAdapter(){
        this.serialTexts = BluetoothCommunicator.getInstance().getHeldData();
    }

    /**
     * Clears the data displayed by this adapter. Calls {@link BluetoothCommunicator#releaseData()}
     */
    public void clearSerialList(){
        BluetoothCommunicator.getInstance().releaseData();
        notifyDataSetChanged();
    }

    @Override
    public SerialMonitorRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_serial_monitor_recyclerview_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SerialMonitorRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.receivedSerialText.setText(serialTexts.get(position));
    }

    @Override
    public int getItemCount() {
        return serialTexts.size();
    }

}
