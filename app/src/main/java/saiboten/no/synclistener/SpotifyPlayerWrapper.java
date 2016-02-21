package saiboten.no.synclistener;

import android.util.Log;

import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerStateCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tobias on 31.03.2015.
 */
public class SpotifyPlayerWrapper {

    private final static String TAG = "SpotifyPlayerWrapper";

    private Player player;

    /* private List<PlayerStateCallback> callbacks; Gotta wait for spotify to provide a way to delete player state callbacks */

    /*public void clearCallbacks() {
        for(PlayerStateCallback playerStateCallback : callbacks) {
            this.player.removePlayerNotificationCallback(playerStateCallback);
        }
    }*/

    public SpotifyPlayerWrapper() {
        /* callbacks = new ArrayList<PlayerStateCallback>(); */
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void resume() {
        this.player.resume();
    }

    public void pause() {
        this.player.pause();
    }

    public void seekToPosition(int i) {
        if(i>3000) {
            this.player.seekToPosition(i);
        }
        else {
            Log.d(TAG, "Position " + i + " is less than three seconds. Let's just skip the seek. Probably a new track");
        }
    }

    public boolean isPlayerInitialized() {
        return this.player != null && this.player.isInitialized() && this.player.isLoggedIn();
    }

    public void play(String nextSong) {
        this.player.play(nextSong);
    }

    public void getPlayerState(PlayerStateCallback callback) {
        this.player.getPlayerState(callback);
    }
}
