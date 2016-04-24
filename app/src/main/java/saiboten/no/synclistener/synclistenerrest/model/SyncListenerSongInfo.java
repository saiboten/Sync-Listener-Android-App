package saiboten.no.synclistener.synclistenerrest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Tobias on 22.02.2016.
 */
public class SyncListenerSongInfo {

    private boolean success;

    @JsonProperty("song")
    private SongTop songTop;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "SyncListenerSongInfo{" +
                "success=" + success +
                ", songTop=" + songTop +
                '}';
    }

    public SongTop getSongTop() {
        return songTop;
    }

    public void setSongTop(SongTop songTop) {
        this.songTop = songTop;
    }

}
