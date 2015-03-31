package saiboten.no.synclistener;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.spotify.sdk.android.playback.ConnectionStateCallback;
import com.spotify.sdk.android.playback.Player;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;

/**
 * Created by Tobias on 15.03.2015.
 */
public class SpotifyPlayerNotificationListener implements
        PlayerNotificationCallback, ConnectionStateCallback, Player.InitializationObserver {

    MusicService musicService;

   public SpotifyPlayerNotificationListener(MusicService musicService) {
       this.musicService = musicService;
   }

    @Override
    public void onLoggedIn() {
        Log.d("SpotifyIntegrationComp", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("SpotifyIntegrationComp", "User logged out");
    }

    @Override
    public void onTemporaryError() {
        Log.d("SpotifyIntegrationComp", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("SpotifyIntegrationComp", "Received connection message: " + message);
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("SpotifyIntegrationComp", "Login failed");
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("SpotifyIntegrationComp", "Playback event received: " + eventType);

        if (eventType.equals(EventType.END_OF_CONTEXT)) {
            Log.d("SpotifyIntegrationComp", "We are allowed to get a new song. Updating last time played time");
            // TODO send intent musicService.getMainActivity().musicPlayerFragment().synchronizeViewWithPlaylist();

            Intent synchronize = new Intent(MainActivity.SYNCHRONIZE);
            synchronize.setAction("no.saiboten.synclistener.SYNCHRONIZE");
            LocalBroadcastManager.getInstance(musicService).sendBroadcast(synchronize);
        }
        else if (eventType.equals(EventType.AUDIO_FLUSH)) {
            Log.d("SpotifyIntegrationComp", "AUDIO_FLUSH: Try Seek and destroy");
            // TODO send intent musicService.getMainActivity().musicPlayerFragment().seek();

            Intent seek = new Intent(MainActivity.SEEK);
            seek.setAction("no.saiboten.synclistener.SEEK");
            LocalBroadcastManager.getInstance(musicService).sendBroadcast(seek);

        }
        Log.d("SpotifyIntegrationComp", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("SpotifyIntegrationComp", "Playback error received: " + errorType.name());
    }

    @Override
    public void onInitialized(Player player) {
        player.addConnectionStateCallback(this);
        player.addPlayerNotificationCallback(this);
    }

    @Override
    public void onError(Throwable throwable) {
        Log.e("SpotifyPlayerInitObs", "Could not initialize player: " + throwable.getMessage());
    }
}
