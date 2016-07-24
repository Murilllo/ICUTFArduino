package amplastudio.csvgenerator.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import amplastudio.csvgenerator.R;
import amplastudio.csvgenerator.db.AppDataBase;
import amplastudio.csvgenerator.fragments.bluetooth.communicator.BluetoothCommunicator;

/**
 * Dialog that displays an edittext to receive a file name and export the content returned by {@link BluetoothCommunicator#getHeldData()} to default file location.
 */
public class ExportSerialStreamDialogFragment extends AppCompatDialogFragment implements DialogInterface.OnClickListener, DialogInterface.OnShowListener, View.OnClickListener{

    private AlertDialog alertDialog;
    private EditText fileNameEditText;

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState){

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_edittext, null);
        fileNameEditText = (EditText) v.findViewById(R.id.dialog_fragment_edittext_edittext);
        fileNameEditText.setHint(getResources().getString(R.string.dialog_export_terminal_stream_edittext_hint));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(v)
                .setTitle(getResources().getString(R.string.dialog_export_terminal_stream_title))
                .setPositiveButton(getResources().getString(R.string.dialog_export_terminal_stream_button_positive), this)
                .setNegativeButton(getResources().getString(R.string.dialog_export_terminal_stream_button_negative), this);

        alertDialog = builder.create();
        alertDialog.setOnShowListener(this);
        return alertDialog;
    }

    @Override
    public void onShow(DialogInterface dialog){
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        String name = fileNameEditText.getText().toString();

        if(name.equals("")){
            Toast.makeText(getContext(), getResources().getString(R.string.dialog_export_terminal_stream_message_provide_file_name),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        export(name);
        dismiss();
    }

    /* Is here only because it is needed to create positive and negative buttons. Positive button click listener is implemented in onClick(View) */
    @Override
    public void onClick(DialogInterface dialog, int which) {}

    /**
     * Exports the file to default file location
     * @param fileName File name to export
     */
    private void export(@NonNull String fileName){

        StringBuilder builder = new StringBuilder();
        ArrayList<String> receivedData = BluetoothCommunicator.getInstance().getHeldData();
        String fileLocation = AppDataBase.getInstance(getContext()).getExportFileLocation();

        if(receivedData.size() == 0){
            //nothing to export
            Toast.makeText(getContext(), getResources().getString(R.string.file_export_nothing), Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (receivedData){ /* locks received data since another thread is updating this list */
            Iterator i = receivedData.iterator();
            while(i.hasNext()) {
                builder.append(i.next());
                builder.append(System.getProperty("line.separator"));
            }
        }

        try {
            File file = new File(fileLocation + File.separator + fileName);
            file.setReadable(true, false);
            file.setWritable(true, false);
            file.setExecutable(false);

            byte[] outputData = builder.toString().getBytes();

            /*if(!hasEnoughSpace(file, outputData)){
                Toast.makeText(getContext(), getResources().getString(R.string.file_export_not_enough_space), Toast.LENGTH_LONG).show();
                return;
            }*/

            if(!file.createNewFile()){ // file already exists, abort
                Toast.makeText(getContext(), getResources().getString(R.string.file_to_export_exists), Toast.LENGTH_SHORT).show();
                return;
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(outputData);
            outputStream.flush();
            outputStream.close();

        } catch(IOException e){
            e.printStackTrace();
        }

        Toast.makeText(getContext(), getResources().getString(R.string.file_exported)
                + fileLocation + File.separator + fileName,Toast.LENGTH_LONG).show();

    }

    /**
     *
     * @param f File to check
     * @param data Data that is intent to append in file
     * @return True if have enough bytes available, false otherwise
     */
    private boolean hasEnoughSpace(File f, byte data[]){
        StatFs sf = new StatFs(f.getAbsolutePath());
        long bytesAvailable;

        if(Build.VERSION.SDK_INT >= 18)
            bytesAvailable = sf.getBlockSizeLong() * sf.getAvailableBlocksLong();

        else
            bytesAvailable = sf.getBlockSize() * sf.getAvailableBlocks();

        return data.length >= bytesAvailable;

    }

}
