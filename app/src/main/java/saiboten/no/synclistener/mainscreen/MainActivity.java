// TutorialApp
// Created by Spotify on 25/02/14.
// Copyright (c) 2014 Spotify. All rights reserved.
package saiboten.no.synclistener.mainscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.EditText;

import com.spotify.sdk.android.authentication.AuthenticationResponse;

import javax.inject.Inject;

import saiboten.no.synclistener.musicservicecommunicator.MusicServiceCommunicator;
import saiboten.no.synclistener.R;
import saiboten.no.synclistener.dagger.BaseApplication;
import saiboten.no.synclistener.synclisterwebview.WebViewFragment;

public class MainActivity extends FragmentActivity {

    ViewFragmentsPagerAdapter mViewFragmentsPagerAdapter;

    ViewPager mViewPager;

    @Inject
    public MusicServiceCommunicator musicServiceCommunicator;

    @Inject
    MusicPlayerFragment musicPlayerFragment;

    @Inject
    WebViewFragment webViewFragment;

    private final static String TAG = "MainActivity";

    //Your activity will respond to this action String
    public static final String SYNCHRONIZE = "no.saiboten.synclistener.SYNCHRONIZE";
    public static final String SEEK = "no.saiboten.synclistener.SEEK";
    public static final String PAUSE = "no.saiboten.synclistener.PAUSE";
    public static final String RESUME = "no.saiboten.synclistener.RESUME";

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received intent in MainActivity. From the service? OMG!" + intent.getAction());

            if(intent.getAction().equals(SYNCHRONIZE)) {
                Log.d(TAG, "Synchronizing with server playlist");
                musicPlayerFragment().synchronizeViewWithPlaylist();
            }
            else if(intent.getAction().equals(SEEK)) {
                Log.d(TAG, "Seeking to the right place");
                musicPlayerFragment().seek();
            }
            else if(intent.getAction().equals(PAUSE)) {
                Log.d(TAG, "Pausing");
                musicPlayerFragment().pause();
            }
            else if(intent.getAction().equals(RESUME)) {
                Log.d(TAG, "Resuming");
                musicPlayerFragment().resume();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseApplication) getApplication()).inject(this);


        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SYNCHRONIZE);
        intentFilter.addAction(SEEK);
        intentFilter.addAction(PAUSE);
        intentFilter.addAction(RESUME);
        bManager.registerReceiver(bReceiver, intentFilter);
        this.musicServiceCommunicator.setActivity(this);

        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        // getFragmentManager().findFragmentByTag(""); //WTF?

        mViewFragmentsPagerAdapter =
                new ViewFragmentsPagerAdapter(
                        getSupportFragmentManager(), webViewFragment, musicPlayerFragment);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mViewPager.setAdapter(mViewFragmentsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                WebViewFragment webViewFragment = getWebViewFragment(); // 1 == webview
                MusicPlayerFragment musicPlayerFragment = musicPlayerFragment(); // 0 == player
                EditText playlistName = (EditText) musicPlayerFragment.rootView.findViewById(R.id.playlist);
                String playlist = playlistName.getText().toString();
                Log.d("MainActivity", playlist);
                webViewFragment.changeUrl(playlist);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Not in use
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Not in use
            }
        });
    }

    public MusicServiceCommunicator getMusicServiceCommunicator() {
        return this.musicServiceCommunicator;
    }

    public MusicPlayerFragment musicPlayerFragment() {
        return (MusicPlayerFragment) mViewFragmentsPagerAdapter.getRegisteredFragment(0);
    }

    public WebViewFragment getWebViewFragment() {
        return (WebViewFragment) mViewFragmentsPagerAdapter.getRegisteredFragment(1);
    }



    @Override
    protected void onDestroy() {
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        bManager.unregisterReceiver(bReceiver);
        super.onDestroy();
        Log.d("MainActivity", "Main activity destroyed");
    }


}