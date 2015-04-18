package saiboten.no.synclistener;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ViewFragmentsPagerAdapter extends SmartFragmentStatePagerAdapter {

    private final static String TAG = "ViewFragPagerAdapter";

   // MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();

   //  WebViewFragment webViewFragment = new WebViewFragment();

    public ViewFragmentsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Log.d(TAG, "Request item number: " + i);

       if(i==0) {
           Log.d(TAG, "Creating MusicPlayerFragment");
           return MusicPlayerFragment.newInstance();
       }
        else {
           Log.d(TAG, "Creating WebViewFragment");
           return WebViewFragment.newInstance();
       }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }
}