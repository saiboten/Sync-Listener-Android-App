// TutorialApp
// Created by Spotify on 25/02/14.
// Copyright (c) 2014 Spotify. All rights reserved.
package saiboten.no.synclistener.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import saiboten.no.synclistener.R;
import saiboten.no.synclistener.dagger.BaseApplication;
import saiboten.no.synclistener.mainscreen.MusicPlayerFragment;
import saiboten.no.synclistener.mainscreen.ViewFragmentsPagerAdapter;
import saiboten.no.synclistener.musicservicecommunicator.MusicServiceCommunicator;
import saiboten.no.synclistener.preferences.AccessTokenHelper;
import saiboten.no.synclistener.webview.WebViewFragment;

public class MainActivity extends FragmentActivity {

    //Your activity will respond to this action String
    public static final String SYNCHRONIZE = "no.saiboten.synclistener.SYNCHRONIZE";

    public static final String SEEK = "no.saiboten.synclistener.SEEK";

    public static final String PAUSE = "no.saiboten.synclistener.PAUSE";

    public static final String RESUME = "no.saiboten.synclistener.RESUME";

    public static final String PLAYINGSTATUS = "no.saiboten.synclistener.PLAYINGSTATUS";

    public static final String SYNCHRONIZE_AND_PLAY = "no.saiboten.synclistener.SYNCHRONIZE_AND_PLAY";

    private final static String TAG = "MainActivity";

    public static final String SERVICE_STOPPED = "no.saiboten.synclistener.SERVICE_STOPPED";

    @Inject
    public MusicServiceCommunicator musicServiceCommunicator;

    @Inject
    public MusicPlayerFragment musicPlayerFragment;

    @Inject
    public AccessTokenHelper accessTokenHelper;

    @Bind(R.id.tablayout)
    TabLayout tabLayout;

    @Bind(R.id.MainActivity_ViewPager_pager)
    public ViewPager viewPager;

    ViewFragmentsPagerAdapter viewFragmentsPagerAdapter;

    @Inject
    WebViewFragment webViewFragment;

    public ProgressDialog popup;

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received intent in MainActivity from the service, action:" + intent.getAction());

            if(intent.getAction().equals(SYNCHRONIZE)) {
                Log.d(TAG, "Synchronizing with server playlist");
                musicPlayerFragment().setInfo();
            } else if(intent.getAction().equals(SYNCHRONIZE_AND_PLAY)) {
                Log.d(TAG, "Synchronizing and playing");
                musicPlayerFragment().synchronize();
            } else if(intent.getAction().equals(SERVICE_STOPPED)) {
                Log.d(TAG, "Synchronizing and playing");
                //TODO
                serviceStopped();
            } else if(intent.getAction().equals(SEEK)) {
                Log.d(TAG, "Seeking to the right place");
                musicPlayerFragment().seek();
            } else if(intent.getAction().equals(PAUSE)) {
                Log.d(TAG, "Pausing");
                musicPlayerFragment().pause();
            } else if(intent.getAction().equals(RESUME)) {
                Log.d(TAG, "Resuming");
                musicPlayerFragment().resume();
            } else if(intent.getAction().equals(PLAYINGSTATUS)) {
                Log.d(TAG, "Play status received");
                musicPlayerFragment().playStatusCallback(intent.getBooleanExtra("status", false));
            }
        }
    };

    private void serviceStopped() {
        musicPlayerFragment.serviceStopped();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity onCreate");

        this.setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ((BaseApplication) getApplication()).inject(this);
        this.musicServiceCommunicator.setActivity(this);

        setupBroadcasterRegistration();

        setupFragmentView();
        checkAutoplay();
    }

    private void checkAutoplay() {
        Intent intent = getIntent();
        if(intent.getBooleanExtra("autoplay", false)) {
            musicPlayerFragment.synchronize();
        }
    }

    private void setupBroadcasterRegistration() {
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SYNCHRONIZE);
        intentFilter.addAction(SEEK);
        intentFilter.addAction(PLAYINGSTATUS);
        intentFilter.addAction(SYNCHRONIZE_AND_PLAY);
        intentFilter.addAction(SERVICE_STOPPED);
        intentFilter.addAction(PAUSE);
        intentFilter.addAction(RESUME);
        bManager.registerReceiver(bReceiver, intentFilter);
    }

    private void setupFragmentView() {
        viewFragmentsPagerAdapter = new ViewFragmentsPagerAdapter(getSupportFragmentManager(), webViewFragment, musicPlayerFragment);
        viewPager.setAdapter(viewFragmentsPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

        //Bind the title indicator to the adapter
        // TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
        // titleIndicator.setViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                WebViewFragment webViewFragment = getWebViewFragment(); // 1 == webview
                MusicPlayerFragment musicPlayerFragment = musicPlayerFragment(); // 0 == player

                String playlistString = musicPlayerFragment.playlist.getText().toString();
                Log.d(TAG, "Tring to load this playlist in webview: " + playlistString);
                webViewFragment.changeUrl(playlistString);
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
        return (MusicPlayerFragment) viewFragmentsPagerAdapter.getItem(0);
    }

    public WebViewFragment getWebViewFragment() {
        return (WebViewFragment) viewFragmentsPagerAdapter.getItem(1);
    }

    public void setupProgressDialog() {
        popup = new ProgressDialog(this);
        popup.setMessage("Laster");
        popup.setCancelable(false);
        popup.show();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        bManager.unregisterReceiver(bReceiver);
        ButterKnife.unbind(this);
        super.onDestroy();

        Log.d("MainActivity", "Main activity destroyed");
    }
}