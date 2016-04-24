package saiboten.no.synclistener.mainscreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import saiboten.no.synclistener.R;
import saiboten.no.synclistener.spotifysonginfo.SongInfoServiceFromSpotify;
import saiboten.no.synclistener.spotifysonginfo.callback.SongInfoFromSpotifyCallback;
import saiboten.no.synclistener.spotifysonginfo.model.SpotifySyncNiceSongInfoModel;
import saiboten.no.synclistener.synclistenerrest.NextSongService;
import saiboten.no.synclistener.synclistenerrest.callback.NextSongFromSynclistenerCallback;
import saiboten.no.synclistener.synclistenerrest.model.SyncListenerSongInfo;
import saiboten.no.synclistener.tasks.DownloadImageTask;

public class MusicPlayerFragment extends Fragment implements NextSongFromSynclistenerCallback, SongInfoFromSpotifyCallback {

    public static final String ACCESS_TOKEN = "access_token";

    private static final String PREFS_NAME = "SyncListenerAccessToken";

    private final static String TAG = "MusicPlayerFragment";

    @Bind(R.id.MusicPlayFragment_ProgressBar_progressBar)
    public ProgressBar songProgress;

    @Bind(R.id.play_or_pause)
    public ImageButton pauseOrPlayButton;

    @Bind(R.id.timePlayed)
    public TextView timePlayed;

    @Bind(R.id.song_length)
    public TextView songLength;

    @Bind(R.id.playlist)
    public EditText playlist;

    @Bind(R.id.songinfo)
    public TextView songinfo;

    @Bind(R.id.imageView)
    public ImageView imageView;

    public View rootView;

    public int songDurationSeconds = 100;

    MainActivity mainActivity;

    private Handler handler = new Handler();

    private int secondsPlayedTotal = 0;

    private String playingSong = null;

    private boolean timeToSeek = false;

    private int seekTime = 0;

    private boolean paused = true;

    private boolean handlerInitialized = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        mainActivity = (MainActivity) getActivity();

        if(!handlerInitialized) {
            handlerInitialized = true;
            new android.os.Handler().postDelayed(new UpdateTime(), 1000);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        rootView = inflater.inflate(
                R.layout.swipeviewfragment, container, false);
        ButterKnife.bind(this, rootView);

        SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        String previousPlaylist = sharedPref.getString(getString(R.string.playlist), "");

        Log.d(TAG, "The stored playlist: " + previousPlaylist);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);

        MainActivity mainActivity = (MainActivity) getActivity();
        if(!mainActivity.musicServiceCommunicator.isMusicServiceRunning()) {
            mainActivity.musicServiceCommunicator.startMusicService(sharedPreferences.getString(ACCESS_TOKEN, null));
        }

        playlist.setText(previousPlaylist);
        return rootView;
    }

    public void pause() {
        if(mainActivity.getMusicServiceCommunicator().isMusicServiceRunning()) {
            pauseOrPlayButton.setImageResource(R.drawable.play);
            paused = true;
        }
    }

    public void resume() {
        if(mainActivity.getMusicServiceCommunicator().isMusicServiceRunning()) {
            pauseOrPlayButton.setImageResource(R.drawable.pause);
            paused = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //TODO save some state here maybe?
    }

    @OnClick(R.id.play_or_pause)
    public void playOrPauseClick() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if(paused) {
            String playlistText = playlist.getText().toString();

            if(playlistText != null && !playlistText.equals("")) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                Set<String> previousPlaylists = sharedPref.getStringSet(getString(R.string.playlistSet), new HashSet<String>());

                SharedPreferences.Editor editor = sharedPref.edit();
                Log.d(TAG, "Playlist being saved: " + playlistText);
                editor.putString(getString(R.string.playlist), playlistText);

                previousPlaylists.add(playlistText);
                editor.putStringSet(getString(R.string.playlistSet), previousPlaylists);
                editor.commit();
            }

            synchronizeViewWithPlaylist();
        } else {
            mainActivity.getMusicServiceCommunicator().pausePlayer();
        }
    }

    public void seek() {
        if(timeToSeek) {
            timeToSeek = false;
            MainActivity mainActivity = (MainActivity) getActivity();
            ((MainActivity) getActivity()).getMusicServiceCommunicator().seekToPosition(seekTime * 1000);
            secondsPlayedTotal = seekTime;
        }
    }

    public void setInfo() {
        MainActivity mainActivity = (MainActivity) this.getActivity();
        if(mainActivity.getMusicServiceCommunicator().isMusicServiceRunning()) {
            String playlistText = playlist.getText().toString();

            if(playlistText != null && !playlistText.equals("")) {
                Log.d(TAG, "Complete url: " + playlistText);
                new NextSongService().getNextSong(this, playlistText);
            }
        }
    }

    public void synchronizeViewWithPlaylist() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if(mainActivity.getMusicServiceCommunicator().isMusicServiceRunning()) {
            resume();
            songDurationSeconds = 0;
            setInfo();
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void getNextSongSuccess(SyncListenerSongInfo syncListenerSongInfo) {
        Log.d(TAG, "Result: " + syncListenerSongInfo);

        String nextSong = syncListenerSongInfo.getSongTop().getSongAgain().getUri();
        int songDurationS = syncListenerSongInfo.getSongTop().getSongAgain().getSongDurationMs();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getMusicServiceCommunicator().play(nextSong, playlist.getText().toString());
        this.playingSong = nextSong;

        Log.d(TAG, "Song: " + nextSong);

        secondsPlayedTotal = 0;

        this.timeToSeek = true;
        this.seekTime = syncListenerSongInfo.getSongTop().getSongAgain().getSecondsPlayed();

        songDurationSeconds = songDurationS;
        int minutes = (songDurationSeconds) / 60;
        int seconds = (songDurationSeconds) % 60;

        songLength.setText(minutes + ":" + String.format("%02d", seconds));

        String trackId = nextSong.substring(14, nextSong.length());
        Log.d(TAG, "Track uri: " + trackId);

        new SongInfoServiceFromSpotify().getSongInfoFromSpotify(this, trackId);
    }

    @Override
    public void getNextSongFailed() {
        Log.e(TAG, "Could not get the next song to play :-(");
    }

    @Override
    public void songFromSpotifySuccessCallback(SpotifySyncNiceSongInfoModel spotifySongInfoModel) {
        Log.d(TAG, "Spotify song info: " + spotifySongInfoModel);
        songinfo.setText(spotifySongInfoModel.getArtist() + " - " + spotifySongInfoModel.getSong());
        new DownloadImageTask(imageView).execute(spotifySongInfoModel.getUrlToImage());
    }

    @Override
    public void songFromSpotifyFailedCallback() {
        Log.e(TAG, "Could not get song data from spotify :-(");
    }

    private class UpdateTime implements Runnable {

        @Override
        public void run() {

            if(!paused) {
                secondsPlayedTotal++;

                if(songDurationSeconds != 0) {
                    int progress = (secondsPlayedTotal * 100 / songDurationSeconds);
                    //Log.d("MusicPlayerFragment", "Updating progress status: " + progress);
                    songProgress.setProgress(progress);
                }

                timePlayed.setText(secondsPlayedTotal / 60 + ":" + String.format("%02d", secondsPlayedTotal % 60));
            }
            new android.os.Handler().postDelayed(new UpdateTime(), 1000);
        }
    }

}