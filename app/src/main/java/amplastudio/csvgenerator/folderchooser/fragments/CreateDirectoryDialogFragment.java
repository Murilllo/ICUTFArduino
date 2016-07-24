package amplastudio.csvgenerator.folderchooser.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import amplastudio.csvgenerator.R;

/**
 * Dialog that receive input from user to receive the name of the new directory that will be created.
 */
public class CreateDirectoryDialogFragment extends AppCompatDialogFragment implements View.OnClickListener, DialogInterface.OnClickListener, DialogInterface.OnShowListener{

    private EditText directoryNameEditText;
    private OnRequestCreateDirectoryListener listener;
    private AlertDialog alertDialog;


    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState){

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_edittext, null);
        directoryNameEditText = (EditText) v.findViewById(R.id.dialog_fragment_edittext_edittext);
        directoryNameEditText.requestFocus();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.dialog_fragment_create_directory_title))
                .setPositiveButton(getResources().getString(R.string.dialog_fragment_create_directory_positive_button), this)
                .setNegativeButton(getResources().getString(R.string.dialog_fragment_create_directory_negative_button), this)
                .setView(v);

        alertDialog = builder.create();
        alertDialog.setOnShowListener(this);
        return alertDialog;
    }

    /* behaviour overridden since after this call the dialog is dismissed,
    this function is here only because it is needed to create the positive and negative buttons */
    @Override
    public void onClick(DialogInterface dialog, int which) {}

    @Override
    public void onClick(View v) { /* only positive button receive this */
        if(directoryNameEditText.getText().toString().equals("")){
            toastShort(getResources().getString(R.string.dialog_fragment_create_directory_provide_directory_name));
            return;
        }

        if(listener != null) {
            listener.onCreateDirectoryRequest(directoryNameEditText.getText().toString());
            Log.i("TESTE", "calling " + String.valueOf(listener));
        }
        dismiss();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        Button positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setOnClickListener(this);
    }


    /**
     * Provides a listener class to receive the desired directory name to be created
     * @param listener Listener interface to callback
     * #see #OnRequestCreateDirectoryListener
     */
    public void setOnRequestCreateDirectoryListener(OnRequestCreateDirectoryListener listener){
        this.listener = listener;
    }

    /**
     * Shows a message via {@link android.widget.Toast} with {@link android.widget.Toast#LENGTH_SHORT}
     * @param message Message to be shown
     */
    private void toastShort(@NonNull String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public interface OnRequestCreateDirectoryListener{

        /**
         * Called when an user successfully inputted directory name, the directory should be created here
         * @param directoryName Directory name to be create, does not contain the path it will be created
         */
        void onCreateDirectoryRequest(String directoryName);

    }

}
