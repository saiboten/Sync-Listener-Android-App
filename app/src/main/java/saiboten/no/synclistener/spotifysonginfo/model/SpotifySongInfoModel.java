package saiboten.no.synclistener.spotifysonginfo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Tobias on 24.04.2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifySongInfoModel {
   // JSONObject jsonObject = new JSONObject(jsonFromWebApi);
//        String artist = jsonObject.getJSONArray("artists").getJSONObject(0).getString("name");
//        String song = jsonObject.getString("name");
//        songinfo.setText(artist + " - " + song);
//
//        String urlString = jsonObject.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
//        Log.d(TAG, "Url of image: " + urlString);

    private List<Artists> artists;

    private String name;

    private Album album;

    public List<Artists> getArtists() {
        return artists;
    }

    public void setArtists(List<Artists> artists) {
        this.artists = artists;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    @Override
    public String toString() {
        return "SpotifySongInfoModel{" +
                "artists=" + artists +
                ", name='" + name + '\'' +
                ", album=" + album +
                '}';
    }
}
