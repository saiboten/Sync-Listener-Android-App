package saiboten.no.synclistener;

import com.spotify.sdk.android.playback.Player;
import com.spotify.sdk.android.playback.PlayerStateCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tobias on 31.03.2015.
 */
public class SpotifyPlayerWrapper {

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
        this.player.seekToPosition(i);
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
