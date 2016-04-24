package saiboten.no.synclistener.synclistenerrest;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import saiboten.no.synclistener.synclistenerrest.model.SyncListenerSongInfo;

/**
 * Created by Tobias on 22.02.2016.
 */
public interface SyncListenerRestServices {

    @GET("get_song/{playlist}")
    Call<SyncListenerSongInfo> getPlaylistInfo(@Path("playlist") String playlist);

}
