// TutorialApp
// Created by Spotify on 25/02/14.
// Copyright (c) 2014 Spotify. All rights reserved.
package saiboten.no.synclistener;

import android.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class MainActivity extends FragmentActivity {

    ViewFragmentsPagerAdapter mViewFragmentsPagerAdapter;

    ViewPager mViewPager;

    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
    public static final int REQUEST_CODE = 1337;

    private MusicServiceCommunicator musicServiceCommunicator;

    private final static String TAG = "MainActivity";

    //Your activity will respond to this action String
    public static final String SYNCHRONIZE = "no.saiboten.synclistener.SYNCHRONIZE";
    public static final String SEEK = "no.saiboten.synclistener.SEEK";
    public static final String PAUSE = "no.saiboten.synclistener.PAUSE";
    public static final String RESUME = "no.saiboten.synclistener.RESUME";

    private static final String CLIENT_ID = "b60120e0052b4973b2a89fab00925019";

    private static final String REDIRECT_URI = "spotocracy://callback";

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

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SYNCHRONIZE);
        intentFilter.addAction(SEEK);
        intentFilter.addAction(PAUSE);
        intentFilter.addAction(RESUME);
        bManager.registerReceiver(bReceiver, intentFilter);

        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        getFragmentManager().findFragmentByTag("");

        mViewFragmentsPagerAdapter =
                new ViewFragmentsPagerAdapter(
                        getSupportFragmentManager());
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

        musicServiceCommunicator = new MusicServiceCommunicator(this);
        setupOrConnectToService();

    }

    public void setupOrConnectToService() {
        if(musicServiceCommunicator.isMusicServiceRunning()) {
            Log.d(TAG,"Music Service already running.");
        }
        else {
            Log.d(TAG,"Authenticating and starting music service");
            AuthenticationRequest.Builder builder =
                    new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

            builder.setScopes(new String[]{"streaming"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(this, this.REQUEST_CODE, request);
        }
    }

    public MusicServiceCommunicator getMusicServiceCommunicator() {
        return this.musicServiceCommunicator;
    }

    public void startMusicService(AuthenticationResponse response) {
        // Handle successful response
        Log.d(TAG, "SpotifyPlayer: " + musicServiceCommunicator);
        musicServiceCommunicator.startMusicService(response);
    }

    public MusicPlayerFragment musicPlayerFragment() {
        return (MusicPlayerFragment) mViewFragmentsPagerAdapter.getRegisteredFragment(0);
    }

    public WebViewFragment getWebViewFragment() {
        return (WebViewFragment) mViewFragmentsPagerAdapter.getRegisteredFragment(1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.d("MainActivity", "onActivityResult" + resultCode);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            Log.d("MainActivity", "Response type:" + response.getType());

            switch (response.getType()) {
                case TOKEN:
                    Log.d("MainActivity", "Token granted!");
                    startMusicService(response);
                    musicPlayerFragment().synchronizeViewWithPlaylist();
                    break;

                case ERROR:
                    Log.d("MainActivity", "Some error has occured: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.d("MainActivity", "Default, user cancelled? " + response.getError());
                    // Handle other cases
            }
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        bManager.unregisterReceiver(bReceiver);
        super.onDestroy();
        Log.d("MainActivity", "Main activity destroyed");
    }


}