package saiboten.no.synclistener.spotifysonginfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import saiboten.no.synclistener.spotifysonginfo.model.SpotifySongInfoModel;

/**
 * Created by Tobias on 24.04.2016.
 */
public interface SpotifyRestServices {

    @GET("v1/tracks/{trackId}")
    Call<SpotifySongInfoModel> getSpotifySongInfo(@Path("trackId") String trackId);

}
