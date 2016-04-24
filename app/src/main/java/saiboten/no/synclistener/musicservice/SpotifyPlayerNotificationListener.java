package saiboten.no.synclistener.musicservice;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;

import saiboten.no.synclistener.intro.SetupActivity;
import saiboten.no.synclistener.mainscreen.MainActivity;

/**
 * Created by Tobias on 15.03.2015.
 */
public class SpotifyPlayerNotificationListener implements
        PlayerNotificationCallback, ConnectionStateCallback, Player.InitializationObserver {

    private final static String TAG = "SPlayerNotificationL";

    private MusicService musicService;

    public SpotifyPlayerNotificationListener(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d(TAG, "User logged out");
    }

    @Override
    public void onTemporaryError() {
        Log.d("SpotifyIntegrationComp", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(TAG, "Received connection message: " + message);
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d(TAG, "Login failed");
        openToSetupActivity();
    }

    private void openToSetupActivity() {
        Intent intent = new Intent(this.musicService, SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("loginFailed", true);
        this.musicService.startActivity(intent);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d(TAG, "Playback event received: " + eventType);

        if(eventType.equals(EventType.END_OF_CONTEXT)) {
            playNewSongAndSynchronize();
        } else if(eventType.equals(EventType.AUDIO_FLUSH)) {
            Log.d(TAG, "AUDIO_FLUSH: Try Seek and destroy");

            Intent seek = new Intent(MainActivity.SEEK);
            seek.setAction("no.saiboten.synclistener.SEEK");
            LocalBroadcastManager.getInstance(musicService).sendBroadcast(seek);
        }
        Log.d(TAG, "Playback event received: " + eventType.name());
    }

    private void playNewSongAndSynchronize() {
        Log.d(TAG, "We are allowed to get a new song. Updating last time played time");
        musicService.playNewSong();

        Intent synchronize = new Intent(MainActivity.SYNCHRONIZE);
        synchronize.setAction("no.saiboten.synclistener.SYNCHRONIZE");
        LocalBroadcastManager.getInstance(musicService).sendBroadcast(synchronize);
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d(TAG, "Playback error received: " + errorType.name());
    }

    @Override
    public void onInitialized(Player player) {
        player.addConnectionStateCallback(this);
        player.addPlayerNotificationCallback(this);
    }

    @Override
    public void onError(Throwable throwable) {
        Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
    }
}
