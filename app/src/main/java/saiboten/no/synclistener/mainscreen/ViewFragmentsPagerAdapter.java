package saiboten.no.synclistener.mainscreen;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import saiboten.no.synclistener.webview.WebViewFragment;

public class ViewFragmentsPagerAdapter extends FragmentStatePagerAdapter { //extends SmartFragmentStatePagerAdapter {

    private final static String TAG = "ViewFragPagerAdapter";

    private String tabTitles[] = new String[]{"Player", "Playlist", "Search"};

    public MusicPlayerFragment musicPlayerFragment;

    public WebViewFragment webViewPlaylistFragment;

    public WebViewFragment webViewPlaylistSearch;

    public ViewFragmentsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    // Here we can finally safely save a reference to the created
    // Fragment, no matter where it came from (either getItem() or
    // FragmentManger). Simply save the returned Fragment from
    // super.instantiateItem() into an appropriate reference depending
    // on the ViewPager position.
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d(TAG, "Time to instantiate a new item in position :" + position);
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                musicPlayerFragment = (MusicPlayerFragment) createdFragment;
                break;
            case 1:
                webViewPlaylistFragment = (WebViewFragment) createdFragment;
                break;
            case 2:
                webViewPlaylistSearch = (WebViewFragment) createdFragment;
                break;
        }
        return createdFragment;
    }

    @Override
    public Fragment getItem(int i) {
        Log.d(TAG, "Getting item. This should only happen once per fragment. Position: " + i);
        if(i == 0) {
            Log.d(TAG, "Returning MusicPlayerFragment");
            return MusicPlayerFragment.getInstance();
        }  else if(i == 1) {
            Log.d(TAG, "Returning webViewFragmentPlaylist");
            return WebViewFragment.getInstance();
        }
        else {
            Log.d(TAG, "Returning webViewFragmentSearch");
            return WebViewFragment.getInstance();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}