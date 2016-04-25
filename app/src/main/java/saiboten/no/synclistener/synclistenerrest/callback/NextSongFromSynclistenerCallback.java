package saiboten.no.synclistener.synclistenerrest.callback;

import saiboten.no.synclistener.synclistenerrest.model.SyncListenerSongInfo;

/**
 * Created by Tobias on 24.04.2016.
 */
public interface NextSongFromSynclistenerCallback {
    public void getNextSongSuccess(SyncListenerSongInfo syncListenerSongInfo);

    public void getNextSongAndPlaySuccess(SyncListenerSongInfo syncListenerSongInfo);

    public void getNextSongFailed();
}
