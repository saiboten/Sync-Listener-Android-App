package saiboten.no.synclistener;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import saiboten.no.synclistener.callbacks.NewSongFromSyncListenerCallback;
import saiboten.no.synclistener.tasks.DownloadImageTask;
import saiboten.no.synclistener.tasks.GetSongByRestTask;
import saiboten.no.synclistener.tasks.GetSongInfoByRestTask;

public class MusicPlayerFragment extends Fragment implements NewSongFromSyncListenerCallback {
    public static final String ARG_OBJECT = "object";

    private ProgressBar songProgress;

    private ImageButton playpausebutton;

    private TextView timePlayed;

    public TextView songLength;

    private EditText playlist;

    private TextView songinfo;

    private ImageView imageView;

    public View rootView;

    private Handler handler = new Handler();

    public int songDurationSeconds = 100;

    private int secondsPlayedTotal = 0;

    private boolean timeToSeek = false;

    private int seekTime = 0;

    private boolean paused = true;

    private boolean handlerInitialized = false;

    private final static String TAG = "MusicPlayerFragment";

    public MusicPlayerFragment() {

    }

    public void pause() {
        playpausebutton.setImageResource(R.drawable.synchronize);
        paused = true;
    }

    public void resume() {
        playpausebutton.setImageResource(R.drawable.synchronize);
        paused = false;
        //synchronizeViewWithPlaylist();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");

        rootView = inflater.inflate(
                R.layout.swipeviewfragment, container, false);

        songProgress = (ProgressBar) rootView.findViewById(R.id.progressBar);
        timePlayed = (TextView) rootView.findViewById(R.id.timePlayed);
        playlist = (EditText) rootView.findViewById(R.id.playlist);
        playpausebutton = (ImageButton) rootView.findViewById(R.id.button);
        songLength = (TextView) rootView.findViewById(R.id.song_length);

        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        songinfo = (TextView) rootView.findViewById(R.id.songinfo);

        setupClickListeners();

        Activity context = getActivity();
        SharedPreferences sharedPref = context.getPreferences(Context.MODE_PRIVATE);
        String previousPlaylist = sharedPref.getString(getString(R.string.playlist), "");

        Log.d(TAG,"The stored playlist: "+previousPlaylist);

        playlist.setText(previousPlaylist);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void seek() {
        if(timeToSeek) {
            timeToSeek = false;
            MainActivity mainActivity = (MainActivity) getActivity();
            ((MainActivity) getActivity()).getMusicServiceCommunicator().seekToPosition(seekTime * 1000);
            secondsPlayedTotal = seekTime;
        }
    }

    public void setupClickListeners() {
        ImageButton newTrack = (ImageButton) rootView.findViewById(R.id.button);
        newTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String playlistText = playlist.getText().toString();

                if (playlistText != null && !playlistText.equals("")) {
                    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    Log.d(TAG,"Playlist being saved: " + playlistText);
                    editor.putString(getString(R.string.playlist), playlistText);
                    editor.commit();
                }

                synchronizeViewWithPlaylist();

                if(!handlerInitialized) {
                    handlerInitialized = true;
                    new android.os.Handler().postDelayed(new UpdateTime(), 1000);
                }
            }
        });
    }

    public void synchronizeViewWithPlaylist() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if(mainActivity.getMusicServiceCommunicator().isMusicServiceRunning()) {
            resume();
            songDurationSeconds = 0;

            String playlistText = playlist.getText().toString();

            if (playlistText != null && !playlistText.equals("")) {
                Log.d(TAG, "Complete url: " + playlistText);
                new GetSongByRestTask(this).execute(playlistText);
            }
        }
        else {
            mainActivity.setupOrConnectToService();
        }
    }

    public void updateSongInfo(String jsonFromWebApi) {
        Log.d(TAG, "Json: " + jsonFromWebApi);

        try {
            JSONObject jsonObject = new JSONObject(jsonFromWebApi);
            String artist = jsonObject.getJSONArray("artists").getJSONObject(0).getString("name");
            String song = jsonObject.getString("name");
            songinfo.setText(artist + " - " + song);

            String urlString = jsonObject.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
            Log.d(TAG, "Url of image: " + urlString);

            new DownloadImageTask(imageView).execute(urlString);
        }
        catch(JSONException jsonException) {
            Log.d(TAG, "This aint json, maybe? " + jsonException.getMessage());
        }
    }

    @Override
    public void newSongCallback(String result) {
        try {
            Log.d(TAG, result);
            JSONObject jsonObject = new JSONObject(result);
            JSONObject song = jsonObject.getJSONObject("song");
            String nextSong = song.getString("uri");
            int songDurationS = song.getInt("songDurationMs");
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.getMusicServiceCommunicator().play(nextSong, playlist.getText().toString());

            Log.d(TAG, "Song: " + nextSong);

            secondsPlayedTotal = 0;

            this.timeToSeek = true;
            this.seekTime = song.getInt("secondsPlayed");

            songDurationSeconds = songDurationS;
            int minutes = (songDurationSeconds) / 60;
            int seconds = (songDurationSeconds) % 60;

            songLength.setText(minutes + ":" + String.format("%02d", seconds));

            String trackId = nextSong.substring(14, nextSong.length());
            Log.d(TAG, "Track uri: " + trackId);

            new GetSongInfoByRestTask((MainActivity) getActivity()).execute("https://api.spotify.com/v1/tracks/" + trackId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Fragment newInstance() {
        MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
        return musicPlayerFragment;
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

                timePlayed.setText(secondsPlayedTotal/60 + ":" + String.format("%02d", secondsPlayedTotal%60));
            }
            new android.os.Handler().postDelayed(new UpdateTime(), 1000);
        }
    }
}