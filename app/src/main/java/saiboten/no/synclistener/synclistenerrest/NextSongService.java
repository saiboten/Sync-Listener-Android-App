package saiboten.no.synclistener.synclistenerrest;

import android.util.Log;

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

    private static final String TAG = "NextSongService";

    public void getNextSongAndPlay(final NextSongFromSynclistenerCallback nextSongFromSynclistenerCallback, String playlist) {
        getNextSong(nextSongFromSynclistenerCallback, playlist, true);
    }

    public void getNextSong(final NextSongFromSynclistenerCallback nextSongFromSynclistenerCallback, String playlist) {
        getNextSong(nextSongFromSynclistenerCallback, playlist, false);
    }

    public void getNextSong(final NextSongFromSynclistenerCallback nextSongFromSynclistenerCallback, String playlist, final boolean playCallback) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://spotocracy.net")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        SyncListenerRestServices service = retrofit.create(SyncListenerRestServices.class);

        Call<SyncListenerSongInfo> songInfo = service.getPlaylistInfo(playlist);

        songInfo.enqueue(new Callback<SyncListenerSongInfo>() {
            @Override
            public void onResponse(Call<SyncListenerSongInfo> call, Response<SyncListenerSongInfo> response) {
                if(playCallback) {
                    Log.d(TAG, "Got a response - this is the song to play: " + response.body());
                    nextSongFromSynclistenerCallback.getNextSongAndPlaySuccess(response.body());
                } else {
                    nextSongFromSynclistenerCallback.getNextSongSuccess(response.body());

                }
            }

            @Override
            public void onFailure(Call<SyncListenerSongInfo> call, Throwable t) {
                nextSongFromSynclistenerCallback.getNextSongFailed();
            }
        });
    }
}
