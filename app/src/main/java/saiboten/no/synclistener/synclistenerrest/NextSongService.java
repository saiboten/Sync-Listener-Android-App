package saiboten.no.synclistener.synclistenerrest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import saiboten.no.synclistener.synclistenerrest.callback.NextSongFromSynclistenerCallback;
import saiboten.no.synclistener.synclistenerrest.model.SyncListenerSongInfo;

/**
 * Created by Tobias on 15.03.2015.
 */
public class NextSongService {

    public void getNextSong(final NextSongFromSynclistenerCallback nextSongFromSynclistenerCallback, String playlist) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://178.62.133.37:3000")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        SyncListenerRestServices service = retrofit.create(SyncListenerRestServices.class);

        Call<SyncListenerSongInfo> songInfo = service.getPlaylistInfo(playlist);

        songInfo.enqueue(new Callback<SyncListenerSongInfo>() {
            @Override
            public void onResponse(Call<SyncListenerSongInfo> call, Response<SyncListenerSongInfo> response) {
                nextSongFromSynclistenerCallback.getNextSongSuccess(response.body());
            }

            @Override
            public void onFailure(Call<SyncListenerSongInfo> call, Throwable t) {
                nextSongFromSynclistenerCallback.getNextSongFailed();
            }
        });
    }
}
