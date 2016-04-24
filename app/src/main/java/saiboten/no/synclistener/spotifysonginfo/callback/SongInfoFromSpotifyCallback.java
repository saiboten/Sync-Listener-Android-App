package saiboten.no.synclistener.spotifysonginfo.callback;

import saiboten.no.synclistener.spotifysonginfo.model.SpotifySongInfoModel;
import saiboten.no.synclistener.spotifysonginfo.model.SpotifySyncNiceSongInfoModel;

/**
 * Created by Tobias on 24.04.2016.
 */
public interface SongInfoFromSpotifyCallback {
    public void songFromSpotifySuccessCallback(SpotifySyncNiceSongInfoModel spotifySongInfoModel);

    public void songFromSpotifyFailedCallback();
}
