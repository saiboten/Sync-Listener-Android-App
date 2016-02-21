package saiboten.no.synclistener.mainscreen;

import android.app.Activity;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import saiboten.no.synclistener.R;
import saiboten.no.synclistener.callbacks.NewSongFromSyncListenerCallback;
import saiboten.no.synclistener.musicservicecommunicator.MusicServiceCommunicator;
import saiboten.no.synclistener.tasks.DownloadImageTask;
import saiboten.no.synclistener.tasks.GetNextSongByRestTask;
import saiboten.no.synclistener.tasks.GetSongInfoByRestTask;

public class MusicPlayerFragment extends Fragment implements NewSongFromSyncListenerCallback {
    public static final String ARG_OBJECT = "object";

    private ProgressBar songProgress;

    private ImageButton pauseOrPlayButton;

    private TextView timePlayed;

    public TextView songLength;

    private EditText playlist;

    private TextView songinfo;

    private ImageView imageView;

    public View rootView;

    private Handler handler = new Handler();

    public int songDurationSeconds = 100;

    private int secondsPlayedTotal = 0;

    private String playingSong = null;

    private static final String PREFS_NAME = "SyncListenerAccessToken";

    public static final String ACCESS_TOKEN = "access_token";

    private boolean timeToSeek = false;

    private int seekTime = 0;

    private boolean paused = true;

    private boolean handlerInitialized = false;

    private final static String TAG = "MusicPlayerFragment";

    private MusicServiceCommunicator musicServiceCommunicator;

    @Inject
    public MusicPlayerFragment(MusicServiceCommunicator musicServiceCommunicator) {
        this.musicServiceCommunicator = musicServiceCommunicator;
    }

    public void pause() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if(mainActivity.getMusicServiceCommunicator().isMusicServiceRunning()) {
            pauseOrPlayButton.setImageResource(R.drawable.play);
            paused = true;
        }
        else {
            //mainActivity.setupOrConnectToService();
        }

    }

    public void resume() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if(mainActivity.getMusicServiceCommunicator().isMusicServiceRunning()) {
            pauseOrPlayButton.setImageResource(R.drawable.pause);
            paused = false;
        }
        else {
            //mainActivity.setupOrConnectToService();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if(!handlerInitialized) {
            handlerInitialized = true;
            new android.os.Handler().postDelayed(new UpdateTime(), 1000);
        }

        this.musicServiceCommunicator.setActivity(getActivity());
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
        pauseOrPlayButton = (ImageButton) rootView.findViewById(R.id.play_or_pause);

        songLength = (TextView) rootView.findViewById(R.id.song_length);

        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        songinfo = (TextView) rootView.findViewById(R.id.songinfo);

        setupClickListeners();

        Activity context = getActivity();
        SharedPreferences sharedPref = context.getPreferences(Context.MODE_PRIVATE);
        String previousPlaylist = sharedPref.getString(getString(R.string.playlist), "");
        Set<String> previousPlaylists = sharedPref.getStringSet(getString(R.string.playlistSet), new HashSet<String>());

        //Spinner spinner = (Spinner) rootView.findViewById(R.id.planets_spinner);
        //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, (String[]) previousPlaylists.toArray());
        //spinner.setAdapter(arrayAdapter);

        Log.d(TAG, "The stored playlist: " + previousPlaylist);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
        this.musicServiceCommunicator.startMusicService(sharedPreferences.getString(ACCESS_TOKEN, null));

        playlist.setText(previousPlaylist);
        setInfo();
        return rootView;
    }


    public void setupClickListeners() {

        pauseOrPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity) getActivity();
                if(paused) {
                    String playlistText = playlist.getText().toString();

                    if (playlistText != null && !playlistText.equals("")) {
                        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        Set<String> previousPlaylists = sharedPref.getStringSet(getString(R.string.playlistSet), new HashSet<String>());

                        SharedPreferences.Editor editor = sharedPref.edit();
                        Log.d(TAG,"Playlist being saved: " + playlistText);
                        editor.putString(getString(R.string.playlist), playlistText);

                        previousPlaylists.add(playlistText);
                        editor.putStringSet(getString(R.string.playlistSet), previousPlaylists);
                        editor.commit();
                    }

                    synchronizeViewWithPlaylist();
                }
                else {
                    mainActivity.getMusicServiceCommunicator().pausePlayer();
                }

            }
        });
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
        if (mainActivity.getMusicServiceCommunicator().isMusicServiceRunning()) {
            String playlistText = playlist.getText().toString();

            if (playlistText != null && !playlistText.equals("")) {
                Log.d(TAG, "Complete url: " + playlistText);
                new GetNextSongByRestTask(this).execute(playlistText);
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
        else {
            //mainActivity.setupOrConnectToService();
        }
    }

    @Override
    public void newSongCallback(String result) {
        try {
            Log.d(TAG, result);
            JSONObject jsonObject = new JSONObject(result);
            JSONObject song = jsonObject.getJSONObject("song");
            JSONObject songTwo = song.getJSONObject("song");

            String nextSong = songTwo.getString("uri");
            int songDurationS = songTwo.getInt("songDurationMs");
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.getMusicServiceCommunicator().play(nextSong, playlist.getText().toString());
            this.playingSong = nextSong;

            Log.d(TAG, "Song: " + nextSong);

            secondsPlayedTotal = 0;

            this.timeToSeek = true;
            this.seekTime = songTwo.getInt("secondsPlayed");

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