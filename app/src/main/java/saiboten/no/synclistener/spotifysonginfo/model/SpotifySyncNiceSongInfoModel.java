package saiboten.no.synclistener.spotifysonginfo.model;

/**
 * Created by Tobias on 24.04.2016.
 */
public class SpotifySyncNiceSongInfoModel {
    private String artist;

    private String song;

    private String urlToImage;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    @Override
    public String toString() {
        return "SpotifySyncNiceSongInfoModel{" +
                "artist='" + artist + '\'' +
                ", song='" + song + '\'' +
                ", urlToImage='" + urlToImage + '\'' +
                '}';
    }
}
