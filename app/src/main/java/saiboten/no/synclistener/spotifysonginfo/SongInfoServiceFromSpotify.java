package saiboten.no.synclistener.spotifysonginfo;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import saiboten.no.synclistener.spotifysonginfo.callback.SongInfoFromSpotifyCallback;
import saiboten.no.synclistener.spotifysonginfo.model.SpotifySongInfoModel;
import saiboten.no.synclistener.synclistenerrest.SyncListenerRestServices;
import saiboten.no.synclistener.synclistenerrest.model.SyncListenerSongInfo;

/**
 * Created by Tobias on 15.03.2015.
 */
public class SongInfoServiceFromSpotify {

    private static final String TAG = "SongInfoSpotifyService";

    public void getSongInfoFromSpotify(final SongInfoFromSpotifyCallback callback, String trackId) {
        String returnedData = null;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        SpotifyRestServices service = retrofit.create(SpotifyRestServices.class);

        Call<SpotifySongInfoModel> songInfo = service.getSpotifySongInfo(trackId);

        songInfo.enqueue(new Callback<SpotifySongInfoModel>() {
            @Override
            public void onResponse(Call<SpotifySongInfoModel> call, Response<SpotifySongInfoModel> response) {
                callback.songFromSpotifySuccessCallback(SpotifyModelToUsableModelConverter.convert(response.body()));
            }

            @Override
            public void onFailure(Call<SpotifySongInfoModel> call, Throwable t) {
                Log.d(TAG, "Could not song data from Spotify");
                callback.songFromSpotifyFailedCallback();
            }
        });

        //Retrofit retrofit =

        //"https://api.spotify.com/v1/tracks/" + trackId

//        JSONObject jsonObject = new JSONObject(jsonFromWebApi);
//        String artist = jsonObject.getJSONArray("artists").getJSONObject(0).getString("name");
//        String song = jsonObject.getString("name");
//        songinfo.setText(artist + " - " + song);
//
//        String urlString = jsonObject.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
//        Log.d(TAG, "Url of image: " + urlString);
//
//        new DownloadImageTask(imageView).execute(urlString);
    }
}
