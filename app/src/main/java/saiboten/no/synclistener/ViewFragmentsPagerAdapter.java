package saiboten.no.synclistener;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.webkit.WebSettings;
import android.widget.EditText;

public class ViewFragmentsPagerAdapter extends FragmentPagerAdapter {

    Fragment main;

    Fragment webView;

    MainActivity mainActivity;

    public ViewFragmentsPagerAdapter(FragmentManager fm, MainActivity mainActivity) {
        super(fm);
        this.mainActivity = mainActivity;
        main = new MusicPlayerFragment();
        webView = new WebViewFragment();
    }

    @Override
    public Fragment getItem(int i) {
        Log.d("ViewFragPagerAdapter", "Request item number: " + i);

       if(i==0) {
           return main;
       }
        else {
           return webView;
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