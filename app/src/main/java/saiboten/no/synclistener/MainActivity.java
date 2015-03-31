// TutorialApp
// Created by Spotify on 25/02/14.
// Copyright (c) 2014 Spotify. All rights reserved.
package saiboten.no.synclistener;

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
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class MainActivity extends FragmentActivity {

    ViewFragmentsPagerAdapter mViewFragmentsPagerAdapter;

    ViewPager mViewPager;

    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
    public static final int REQUEST_CODE = 1337;

    //Your activity will respond to this action String
    public static final String SYNCHRONIZE = "no.saiboten.synclistener.SYNCHRONIZE";
    public static final String SEEK = "no.saiboten.synclistener.SEEK";

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MainActivity", "Received intent in MainActivity. From the service? OMG!" + intent.getAction());
            if(intent.getAction().equals(SYNCHRONIZE)) {
                Log.d("MainActivity", "Synchronizing with server playlist");
                musicPlayerFragment().synchronizeViewWithPlaylist();
            }
            else if(intent.getAction().equals(SEEK)) {
                Log.d("MainActivity", "Seeking to the right place");
                musicPlayerFragment().seek();
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
        bManager.registerReceiver(bReceiver, intentFilter);

        Log.d("MainActivity", "onCreate");
        setContentView(R.layout.activity_main);

        mViewFragmentsPagerAdapter =
                new ViewFragmentsPagerAdapter(
                        getSupportFragmentManager(), this);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewFragmentsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                WebViewFragment webViewFragment = (WebViewFragment) mViewFragmentsPagerAdapter.getItem(1); // 1 == webview
                MusicPlayerFragment musicPlayerFragment = (MusicPlayerFragment)  mViewFragmentsPagerAdapter.getItem(0); // 0 == player
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

    public MusicPlayerFragment musicPlayerFragment() {
        return (MusicPlayerFragment) mViewFragmentsPagerAdapter.getItem(0);
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
                    musicPlayerFragment().startMusicService(response);
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
        super.onDestroy();
        Log.d("MainActivity", "Main activity destroyed");
    }


}