package amplastudio.csvgenerator.folderchooser;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import amplastudio.csvgenerator.R;
import amplastudio.csvgenerator.folderchooser.adapter.FolderListAdapter;
import amplastudio.csvgenerator.folderchooser.fragments.CreateDirectoryDialogFragment;

/**
 * Janela que mostra pastas dentro de outras pastas, através dela será possível selecionar uma pasta de destino
 * para algum arquivo
 */
public class FolderChooserActivity extends AppCompatActivity implements View.OnClickListener, CreateDirectoryDialogFragment.OnRequestCreateDirectoryListener{

    public static final String EXTRA_FILE_PATH = "file_path_extra";
    public static final String EXTRA_FILE_NAME = "file_name_extra";
    private RecyclerView recyclerView;
    private FolderListAdapter adapter;

    /**
     * Hold the file from the same path given in the last call of {@link #getDirsNameFrom(String)}
     */
    private File currentFile;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_chooser);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_folder_chooser_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.activity_folder_chooser_title);

        recyclerView = (RecyclerView) findViewById(R.id.activity_folder_chooser_recyclerview);

        Log.i("TESTE", Environment.getExternalStorageDirectory().getAbsolutePath());
        Log.i("TESTE2", Environment.getDataDirectory().getAbsolutePath());
        Log.i("TESTE3", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, getResources()
                    .getString(R.string.activity_folder_chooser_no_storage_available_msg), Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
        adapter = new FolderListAdapter(getDirsNameFrom(Environment.getExternalStorageDirectory().getAbsolutePath()));
        adapter.setRecyclerItemClickListener(this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_folder_chooser_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){

        switch (menuItem.getItemId()){

            case R.id.menu_folder_chooser_activity_newfolder:
                showNewFolderDialogFragment();
                break;

            case R.id.menu_folder_chooser_activity_settings:

                break;

        }

        return true;
    }

    @Override
    public void onBackPressed(){

        if(!currentFile.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())){
            adapter.updateFoldersName(getDirsNameFrom(currentFile.getParent()));
        }

        else super.onBackPressed();
    }

    @Override
    public void onClick(View v) {

        /*if(v.getId() == R.id.activity_folder_chooser_imgbutton_select_folder) {

            if(fileNameEditText.getText().toString().equals("")){
                Toast.makeText(this, getResources()
                        .getText(R.string.activity_folder_chooser_provide_file_name), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent data = new Intent();
            data.putExtra(EXTRA_FILE_PATH, currentFile.getAbsolutePath());
            setResult(RESULT_OK, data);
            finish();
            return;
        }*/

        Log.i("TESTE", "Path: " + currentFile.getPath());
        adapter.updateFoldersName(getDirsNameFrom(currentFile.getPath() + File.separator + v.getTag()));

    }

    /**
     * Get all dirs from the given path. Updates {@link #currentFile}
     * @param path Path to directory that possibly containing other directories.
     * @return List of directory names inside f
     */
    private String[] getDirsNameFrom(@NonNull String path){
        File f = new File(path);
        currentFile = f;
        File[] files = f.listFiles();

        ArrayList<String> dirsArray = new ArrayList<>(20);

        if(files != null) {
            for (File ff : files) {

                if (!ff.isDirectory() || !ff.canRead() || ff.isHidden()) continue;

                if (f.isAbsolute()) // esconde diretórios do sistema
                    if (ff.getName().equals("sdcard") || ff.getName().equals("storage"))
                        dirsArray.add(ff.getName());

                    else dirsArray.add(ff.getName());

            }
        }

        return dirsArray.toArray(new String[dirsArray.size()]);
    }

    private void showNewFolderDialogFragment(){
        CreateDirectoryDialogFragment dialogFragment = new CreateDirectoryDialogFragment();
        dialogFragment.setOnRequestCreateDirectoryListener(this);
        dialogFragment.show(getSupportFragmentManager(), CreateDirectoryDialogFragment.class.getName());
    }

    @Override
    public void onCreateDirectoryRequest(String directoryName) {

        adapter.updateFoldersName(getDirsNameFrom(currentFile.getAbsolutePath())); /* update the list with the new folder */

        Toast.makeText(this, getResources()
                .getString(R.string.dialog_fragment_create_directory_folder_created), Toast.LENGTH_SHORT).show();
    }

}
