package saiboten.no.synclistener.mainscreen;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import saiboten.no.synclistener.webview.WebViewFragment;

public class ViewFragmentsPagerAdapter extends FragmentStatePagerAdapter { //extends SmartFragmentStatePagerAdapter {

    private final static String TAG = "ViewFragPagerAdapter";

    public WebViewFragment webViewFragment;

    public MusicPlayerFragment musicPlayerFragment;

    private String tabTitles[] = new String[]{"Avspiller", "Spilleliste"};

    public ViewFragmentsPagerAdapter(FragmentManager fm, WebViewFragment webViewFragment, MusicPlayerFragment musicPlayerFragment) {
        super(fm);
        this.webViewFragment = webViewFragment;
        this.musicPlayerFragment = musicPlayerFragment;
    }

    @Override
    public Fragment getItem(int i) {
        Log.d(TAG, "Request item number: " + i);

        if(i == 0) {
            Log.d(TAG, "Creating MusicPlayerFragment");
            return musicPlayerFragment;
        } else {
            Log.d(TAG, "Creating WebViewFragment");
            return webViewFragment;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}