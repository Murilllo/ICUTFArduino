package amplastudio.csvgenerator.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import amplastudio.csvgenerator.fragments.SerialStreamFragment;

/**
 * Created by Murillo on 21/05/2016.
 */
public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

    public static final int POSITION_FRAGMENT_SERIAL_MONITOR = 0;
    private static final int NUM_PAGES = 1;

    /**
     * Contains a list of fragment instance that can be accessed via {@link #getFragmentFromPosition(int)}.
     * Fragment list is updated every time {@link #instantiateItem(ViewGroup, int)} is called
     */
    private Fragment[] fragments;

    public ScreenSlidePagerAdapter(FragmentManager manager){
        super(manager);
        fragments = new Fragment[NUM_PAGES];
    }

    @Override
    public Fragment getItem(int position) {

        switch(position){

            default:
                return new SerialStreamFragment();


        }

    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position){
        Fragment f = (Fragment) super.instantiateItem(container, position);
        fragments[position] = f;
        return f;
    }

    /**
     *
     * @param position Fragment position in pager.
     * @return Fragment instance associated to the given position.
     */
    public Fragment getFragmentFromPosition(int position){
        return fragments[position];
    }

}
