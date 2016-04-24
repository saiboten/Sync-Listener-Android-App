package saiboten.no.synclistener.synclistenerrest.model;

/**
 * Created by Tobias on 22.02.2016.
 */
public class SyncListenerSongAgain {
    private String artist;

    private String name;

    private String uri;

    private int score;

    private int songDurationMs;

    private int secondsPlayed;

    private int position;

    private String album;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getSongDurationMs() {
        return songDurationMs;
    }

    public void setSongDurationMs(int songDurationMs) {
        this.songDurationMs = songDurationMs;
    }

    public int getSecondsPlayed() {
        return secondsPlayed;
    }

    public void setSecondsPlayed(int secondsPlayed) {
        this.secondsPlayed = secondsPlayed;
    }

    @Override
    public String toString() {
        return "SyncListenerSongAgain{" +
                "artist='" + artist + '\'' +
                ", name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                ", score=" + score +
                ", songDurationMs=" + songDurationMs +
                ", secondsPlayed=" + secondsPlayed +
                ", position=" + position +
                '}';
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
