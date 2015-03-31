package saiboten.no.synclistener;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Intent;
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
import android.widget.Toast;

import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.playback.PlayerState;
import com.spotify.sdk.android.playback.PlayerStateCallback;

import org.json.JSONException;
import org.json.JSONObject;

import saiboten.no.synclistener.tasks.DownloadImageTask;
import saiboten.no.synclistener.tasks.GetSongByRestTask;
import saiboten.no.synclistener.tasks.GetSongInfoByRestTask;

public class MusicPlayerFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    private ProgressBar songProgress;

    private ImageButton playpausebutton;

    private TextView timePlayed;

    public TextView songLength;

    private EditText playlist;

    private TextView songinfo;

    private ImageView imageView;

    public View rootView;

    private static final String REDIRECT_URI = "spotocracy://callback";

    private Spotify spotify = null;

    private Handler handler = new Handler();

    public int songDurationSeconds = 100;

    private int secondsPlayedTotal = 0;

    private SpotifyPlayerNotificationListener spotifyPlayerNotificationListener;

    private MusicServiceCommunicator musicServiceCommunicator;

    private static final String CLIENT_ID = "b60120e0052b4973b2a89fab00925019";

    private boolean timeToSeek = false;

    private int seekTime = 0;

    private boolean paused = true;

    private boolean handlerInitialized = false;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d("MusicPlayerFragment","Fragment CREATED!");

        rootView = inflater.inflate(
                R.layout.swipeviewfragment, container, false);
        Bundle args = getArguments();

        songProgress = (ProgressBar) rootView.findViewById(R.id.progressBar);
        timePlayed = (TextView) rootView.findViewById(R.id.timePlayed);
        playlist = (EditText) rootView.findViewById(R.id.playlist);
        playpausebutton = (ImageButton) rootView.findViewById(R.id.button);
        songLength = (TextView) rootView.findViewById(R.id.song_length);

        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        songinfo = (TextView) rootView.findViewById(R.id.songinfo);

        setupClickListeners();
        setupOrConnectToService();

        if(musicServiceCommunicator.isMusicServiceRunning()) {
            synchronizeViewWithPlaylist();
        }

        return rootView;
    }

    private void setupOrConnectToService() {
        musicServiceCommunicator = new MusicServiceCommunicator((MainActivity) getActivity());

        if(musicServiceCommunicator.isMusicServiceRunning()) {
            Log.d("MusicPlayerFragment","Music Service already running.");
        }
        else {
            Log.d("MusicPlayerFragment","Authenticating and starting music service");
            AuthenticationRequest.Builder builder =
                    new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

            builder.setScopes(new String[]{"streaming"});
            AuthenticationRequest request = builder.build();

            MainActivity mainActivity = (MainActivity) getActivity();
            AuthenticationClient.openLoginActivity(mainActivity, mainActivity.REQUEST_CODE, request);
        }
    }

    public void seek() {
        if(timeToSeek) {
            timeToSeek = false;
            musicServiceCommunicator.seekToPosition(seekTime * 1000);
            secondsPlayedTotal = seekTime;
        }
    }

    public void setupClickListeners() {
        ImageButton newTrack = (ImageButton) rootView.findViewById(R.id.button);
        newTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(paused) {
                    synchronizeViewWithPlaylist();
                    if(!handlerInitialized) {
                        handlerInitialized = true;
                        new android.os.Handler().postDelayed(new UpdateTime(), 1000);
                    }
                }
                else {
                    pause();
                }
                paused = !paused;
            }
        });
    }

    private void pause() {
        Log.d("MusicPlayerFragment", "Pausing");
        musicServiceCommunicator.pausePlayer();
        playpausebutton.setImageResource(R.drawable.play_button);
    }

    public void startMusicService(AuthenticationResponse response) {

        Log.d("MusicPlayerFragment", "Enabling player");

        // Handle successful response
        Log.d("MusicPlayerFragment", "SpotifyPlayer: " + musicServiceCommunicator);
        musicServiceCommunicator.startMusicService(response);
    }

    public void synchronizeViewWithPlaylist() {
        playpausebutton.setImageResource(R.drawable.pause);
        songDurationSeconds = 0;
        String url = "http://178.62.133.37:3000/get_song/";

        String playlistText = playlist.getText().toString();

        if (playlistText != null) {
            url += playlistText;
            Log.d("MusicPlayerFragment", "Complete url: " + url);
            new GetSongByRestTask(this).execute(url);
        }
    }

    public void updateView(String result) {
        try {
            Log.d("MusicPlayerFragment", result);
            JSONObject jsonObject = new JSONObject(result);
            JSONObject song = jsonObject.getJSONObject("song");
            String nextSong = song.getString("uri");
            int songDurationS = song.getInt("songDurationMs");

            Log.d("MusicPlayerFragment", "Song: " + nextSong);

            secondsPlayedTotal = 0;

            musicServiceCommunicator.play(nextSong);
            this.timeToSeek = true;
            this.seekTime = song.getInt("secondsPlayed");

            songDurationSeconds = songDurationS;
            int minutes = (songDurationSeconds) / 60;
            int seconds = (songDurationSeconds) % 60;

            songLength.setText(minutes + ":" + String.format("%02d", seconds));

            String trackId = nextSong.substring(14, nextSong.length());
            Log.d("MainActivity", "Track uri: " + trackId);

            new GetSongInfoByRestTask((MainActivity) getActivity()).execute("https://api.spotify.com/v1/tracks/" + trackId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateSongInfo(String jsonFromWebApi) {
        Log.d("MusicPlayerFragment", "Json: " + jsonFromWebApi);

        try {
            JSONObject jsonObject = new JSONObject(jsonFromWebApi);
            String artist = jsonObject.getJSONArray("artists").getJSONObject(0).getString("name");
            String song = jsonObject.getString("name");
            songinfo.setText(artist + " - " + song);

            String urlString = jsonObject.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
            Log.d("MusicPlayerFragment", "Url of image: " + urlString);

            new DownloadImageTask(imageView).execute(urlString);
        }
        catch(JSONException jsonException) {
            Log.d("MusicPlayerFragment", "This aint json, maybe? " + jsonException.getMessage());
        }
    }

    private class UpdateTime implements Runnable {

        @Override
        public void run() {

            if(!paused) {
                secondsPlayedTotal++;

                if(songDurationSeconds != 0) {
                    int progress = (secondsPlayedTotal * 100 / songDurationSeconds);
                    Log.d("MusicPlayerFragment", "Updating progress status: " + progress);
                    songProgress.setProgress(progress);
                }

                timePlayed.setText(secondsPlayedTotal/60 + ":" + String.format("%02d", secondsPlayedTotal%60));
            }
            new android.os.Handler().postDelayed(new UpdateTime(), 1000);
        }
    }
}