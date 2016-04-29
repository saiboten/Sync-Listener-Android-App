package saiboten.no.synclistener.mainscreen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import butterknife.OnTouch;
import saiboten.no.synclistener.R;
import saiboten.no.synclistener.activity.MainActivity;
import saiboten.no.synclistener.setup.SetupActivity;
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

    @Bind(R.id.MusicPlayerFragment_ProgressBar_progressBar)
    public ProgressBar progressBar;

    @Bind(R.id.MusicPlayerFragment_ImageButton_play_or_pause)
    public ImageButton pauseOrPlayButton;

    @Bind(R.id.MusicPlayerFragment_TextView_timePlayed)
    public TextView timePlayed;

    @Bind(R.id.MusicPlayerFragment_TextView_songLength)
    public TextView songLength;

    @Bind(R.id.MusicPlayerFragment_EditText_playlist)
    public EditText playlist;

    @Bind(R.id.MusicPlayerFragment_TextView_songinfo)
    public TextView songinfo;

    @Bind(R.id.MusicPlayerFragment_ImageView_imageView)
    public ImageView imageView;

    public View rootView;

    public int songDurationSeconds = 100;

    MainActivity mainActivity;

    private Handler handler = new Handler();

    private String playingSong = null;

    private boolean timeToSeek = false;

    private int seekTime = 0;

    private boolean paused = true;

    private boolean handlerInitialized = false;

    UpdateTime updateTime = new UpdateTime();

    Thread updateTimeThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        mainActivity = (MainActivity) getActivity();
        if(!handlerInitialized) {
            handlerInitialized = true;
            updateTimeThread = new Thread(updateTime);
            updateTimeThread.run();
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

        playlist.setText(previousPlaylist);

        if(mainActivity.musicServiceCommunicator.isMusicServiceRunning()) {
            Log.d(TAG, "Music service is running, lets just set info?");
            mainActivity.musicServiceCommunicator.getPlayOrPauseStatus();
            setInfo();
        }

        if(!mainActivity.musicServiceCommunicator.isMusicServiceRunning() && !mainActivity.accessTokenHelper.accessTokenHasExpired(mainActivity)) {
            mainActivity.musicServiceCommunicator.startMusicService(sharedPreferences.getString(ACCESS_TOKEN, null));
        }

        return rootView;
    }

    public void pause() {
        if(mainActivity.getMusicServiceCommunicator().isMusicServiceRunning()) {
            pauseOrPlayButton.setImageResource(R.drawable.playoptional);
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

    @OnTouch(R.id.MusicPlayerFragment_ImageButton_play_or_pause)
    public boolean touchButton(View v, MotionEvent event) {
        Log.d(TAG, "Touch!" + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                //TODO add some cool stuff here
                v.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                //ImageView imageView = (ImageView) v;
               // imageView.setImageResource(R.drawable.synchronize);
                v.invalidate();
                break;
            }
        }
        return false;
    }

    public void synchronize() {
        //Ensure that the music service is running
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);

        if(mainActivity.accessTokenHelper.accessTokenHasExpired(mainActivity)) {
            Intent intent = new Intent(this.getActivity(), SetupActivity.class);
            intent.putExtra("loginFailed", true);
            startActivity(intent);
        }
        else {
            if(!mainActivity.musicServiceCommunicator.isMusicServiceRunning()) {
                Log.d(TAG, "Music service is not running. Have to start it");
                mainActivity.musicServiceCommunicator.startMusicService(sharedPreferences.getString(ACCESS_TOKEN, null));
            }
            else {
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
                retrieveAndPlaySong();
                pauseOrPlayButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @OnClick(R.id.MusicPlayerFragment_FloatingActionButton)
    public void fabOnClick() {
        synchronize();
    }

    @OnClick(R.id.MusicPlayerFragment_ImageButton_play_or_pause)
    public void playOrPauseClick() {
        mainActivity.getMusicServiceCommunicator().playOrResumePlayer();
    }

    public void seek() {
        if(timeToSeek) {
            timeToSeek = false;
            MainActivity mainActivity = (MainActivity) getActivity();
            ((MainActivity) getActivity()).getMusicServiceCommunicator().seekToPosition(seekTime * 1000);
            //secondsPlayedTotal = seekTime;
        }
    }

    public void setInfo() {
        Log.d(TAG, "Updating screen info. This should not play or pause or anything, just update the info on screen");

        String playlistText = playlist.getText().toString();

        if(playlistText != null && !playlistText.equals("")) {
            Log.d(TAG, "Retrieving song info from playlist: " + playlistText);
            new NextSongService().getNextSong(this, playlistText);
        }
    }

    public void retrieveAndPlaySong() {
        String playlistText = playlist.getText().toString();

        if(playlistText != null && !playlistText.equals("")) {
            Log.d(TAG, "Complete url: " + playlistText);
            new NextSongService().getNextSongAndPlay(this, playlistText);
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        updateTimeThread.interrupt();
    }

    @Override
    public void getNextSongAndPlaySuccess(SyncListenerSongInfo syncListenerSongInfo) {
        String nextSong = syncListenerSongInfo.getSongTop().getSongAgain().getUri();
        mainActivity.getMusicServiceCommunicator().play(nextSong, playlist.getText().toString());
        resume();
        getNextSongSuccess(syncListenerSongInfo);
    }

    public void getNextSongSuccess(SyncListenerSongInfo syncListenerSongInfo) {
        Log.d(TAG, "Result: " + syncListenerSongInfo);

        String nextSong = syncListenerSongInfo.getSongTop().getSongAgain().getUri();
        int songDurationS = syncListenerSongInfo.getSongTop().getSongAgain().getSongDurationMs();
        this.playingSong = nextSong;

        Log.d(TAG, "Song: " + nextSong);

        //secondsPlayedTotal = 0;

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

        songLength.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        timePlayed.setVisibility(View.VISIBLE);

        songinfo.setText(spotifySongInfoModel.getArtist() + " - " + spotifySongInfoModel.getSong());
        new DownloadImageTask(imageView).execute(spotifySongInfoModel.getUrlToImage());
    }

    @Override
    public void songFromSpotifyFailedCallback() {
        Log.e(TAG, "Could not get song data from spotify :-(");
    }

    public void playStatusCallback(boolean isPlaying) {
        Log.d(TAG, "Player status callback recieved, are we playing? " + isPlaying);
        paused = !isPlaying;
    }

    private class UpdateTime implements Runnable {

        @Override
        public void run() {

            if(progressBar != null) {
                if(!paused) {
                    //secondsPlayedTotal++;
                    seekTime++;

                    if(songDurationSeconds != 0) {
                        int progress = (seekTime * 100 / songDurationSeconds);
                        //int progress = (secondsPlayedTotal * 100 / songDurationSeconds);
                        //Log.d("MusicPlayerFragment", "Updating progress status: " + progress);
                        if(progressBar == null) {
                            Log.d(TAG, "ProgressBar is null - assuming fragment is dead");
                        }
                        else {
                            progressBar.setProgress(progress);
                        }
                    }

                    timePlayed.setText(seekTime / 60 + ":" + String.format("%02d", seekTime % 60));
                }
            }
            new android.os.Handler().postDelayed(updateTime, 1000);
        }
    }

}