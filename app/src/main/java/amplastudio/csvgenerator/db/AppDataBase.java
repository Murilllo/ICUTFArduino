package amplastudio.csvgenerator.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 * Class that can access app database. Value can be stored with {@link #putString(String, String)}
 * and accessed via {@link #getString(String, String)}
 */
public class AppDataBase {

    /**
     * Key to access default export path
     * @see #putString(String, String)
     * @see #getString(String, String)
     */
    public static final String KEY_EXPORT_FILE_LOCATION = "default_export_path";

    /**
     * Default return value
     * @see #getString(String, String)
     */
    public static final String DEFAULT_RETURN_VALUE_EXPORT_FILE_LOCATION = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    private static final String SHARED_PREFERENCES_NAME = "shared_pref";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private static AppDataBase instance;


    private AppDataBase(Context context){
        preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    /**
     *
     * @param context App context
     * @return Class instance to access app database
     */
    public synchronized static AppDataBase getInstance(Context context){
        if(instance == null){
            instance = new AppDataBase(context);
        }
        return instance;
    }

    /**
     * Store the value with the given identifier. Will not be fully stored until {@link #commit()} is called
     * @param key Identifier
     * @param val Value to be stored
     * @see #getString(String, String)
     */
    public void putString(String key, String val){
        editor.putString(key, val);
    }

    /**
     * Commit any {@link #putString(String, String)} calls.
     */
    public void commit(){
        editor.commit();
    }

    /**
     *
     * @param key Identifier
     * @param defaultValue Value to be returned if the stored String identified by the given key is not found.
     * @return String in database identified by the given key
     * @see #putString(String, String)
     */
    public String getString(String key, String defaultValue){
        return preferences.getString(key, defaultValue);
    }

    /**
     * Same as <b>putString({@link #KEY_EXPORT_FILE_LOCATION}, path)</b>
     * @param path New path
     * @see #commit()
     * @see #putString(String, String)
     */
    public void putExportFileLocation(String path){
        putString(KEY_EXPORT_FILE_LOCATION, path);
    }

    /**
     * Same as <b>getString({@link #KEY_EXPORT_FILE_LOCATION}, {@link #DEFAULT_RETURN_VALUE_EXPORT_FILE_LOCATION})</b>
     * @return Path to export files
     * @see #getString(String, String)
     */
    public String getExportFileLocation(){
        return getString(KEY_EXPORT_FILE_LOCATION, DEFAULT_RETURN_VALUE_EXPORT_FILE_LOCATION);
    }


}
