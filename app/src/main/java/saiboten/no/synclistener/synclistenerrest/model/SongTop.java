package saiboten.no.synclistener.synclistenerrest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Tobias on 22.02.2016.
 */
public class SongTop {

    private String playlistid;

    @JsonProperty("song")
    private SyncListenerSongAgain songAgain;

    private boolean playing;

    public String getPlaylistid() {
        return playlistid;
    }

    public void setPlaylistid(String playlistid) {
        this.playlistid = playlistid;
    }

    public SyncListenerSongAgain getSongAgain() {
        return songAgain;
    }

    public void setSongAgain(SyncListenerSongAgain songAgain) {
        this.songAgain = songAgain;
    }

    @Override
    public String toString() {
        return "SongTop{" +
                "playlistid='" + playlistid + '\'' +
                ", songAgain=" + songAgain +
                ", playing=" + playing +
                '}';
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

}
