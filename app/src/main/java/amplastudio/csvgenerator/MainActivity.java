package amplastudio.csvgenerator;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import amplastudio.csvgenerator.adapter.ScreenSlidePagerAdapter;
import amplastudio.csvgenerator.folderchooser.FolderChooserActivity;
import amplastudio.csvgenerator.fragments.SerialStreamFragment;
import amplastudio.csvgenerator.fragments.bluetooth.communicator.BluetoothCommunicator;

public class MainActivity extends AppCompatActivity {

    public static final int CHOOSE_FOLDER_REQUEST_CODE = 10;

    private Menu menu;
    private ViewPager viewPager;
    private ScreenSlidePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_actionbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public void onResume(){
        BluetoothCommunicator.getInstance().register(this);
        super.onResume();

    }

    @Override
    public void onPause(){
        BluetoothCommunicator.getInstance().unregister();
        super.onPause();
    }

    @Override
    public void onBackPressed(){

        if(viewPager.getCurrentItem() == 0) super.onBackPressed();
        else viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        this.menu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        viewPager = (ViewPager) findViewById(R.id.activity_main_viewpager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        BluetoothCommunicator communicator = BluetoothCommunicator.getInstance();

        if(communicator.isConnected()){
            displayConnectedBluetoothMenuIcon();
        }

        else{
            displayEnableBluetoothMenuIcon();
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){

        SerialStreamFragment serialFragment;

        switch (menuItem.getItemId()){

            case R.id.menu_main_settings:

                break;
            case R.id.menu_main_exportMonitor:

                serialFragment = (SerialStreamFragment) pagerAdapter
                        .getFragmentFromPosition(ScreenSlidePagerAdapter.POSITION_FRAGMENT_SERIAL_MONITOR);

                //startActivityForResult(new Intent(this, FolderChooserActivity.class), CHOOSE_FOLDER_REQUEST_CODE);

                serialFragment.exportSerialMonitor();

                break;

            case R.id.menu_main_connectBluetooth:

                serialFragment = (SerialStreamFragment) pagerAdapter
                        .getFragmentFromPosition(ScreenSlidePagerAdapter.POSITION_FRAGMENT_SERIAL_MONITOR);

                serialFragment.connectBluetooth();

                break;

            case R.id.menu_main_clearMonitor:

                serialFragment = (SerialStreamFragment) pagerAdapter
                        .getFragmentFromPosition(ScreenSlidePagerAdapter.POSITION_FRAGMENT_SERIAL_MONITOR);

                serialFragment.clearMonitor();

                break;

            case R.id.menu_main_disconnectBluetooth:

                serialFragment = (SerialStreamFragment) pagerAdapter
                        .getFragmentFromPosition(ScreenSlidePagerAdapter.POSITION_FRAGMENT_SERIAL_MONITOR);

                serialFragment.disconnectBluetooth();

        }

        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == CHOOSE_FOLDER_REQUEST_CODE && resultCode == RESULT_OK){

            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Displays the action button to scan and connect to another bluetooth device, hiding the action to disconnect
     * from another bluetooth. Should be called if the application is not connected to another bluetooth device or
     * if bluetooth is disabled.
     * @see #displayConnectedBluetoothMenuIcon()
     */
    public void displayEnableBluetoothMenuIcon(){

        MenuItem enableIcon = menu.findItem(R.id.menu_main_connectBluetooth);
        MenuItem connectedIcon = menu.findItem(R.id.menu_main_disconnectBluetooth);

        enableIcon.setVisible(true);
        connectedIcon.setVisible(false);

    }

    /**
     * Displays the action button to release connection from another bluetooth device, releasing
     * bluetooth resources.
     * @see #displayEnableBluetoothMenuIcon()
     */
    public void displayConnectedBluetoothMenuIcon(){

        MenuItem enableIcon = menu.findItem(R.id.menu_main_connectBluetooth);
        MenuItem connectedIcon = menu.findItem(R.id.menu_main_disconnectBluetooth);

        enableIcon.setVisible(false);
        connectedIcon.setVisible(true);

    }



}
