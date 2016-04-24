package saiboten.no.synclistener.musicservicecommunicator;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.util.Log;

import saiboten.no.synclistener.musicservice.MusicService;

/**
 * Created by Tobias on 15.03.2015.
 */
public class MusicServiceCommunicator {

    private static final String CLIENT_ID = "b60120e0052b4973b2a89fab00925019";

    private final static String TAG = "MusicServiceComm";

    private Activity mainActivity;

    public MusicServiceCommunicator() {

    }

    public void setActivity(Activity activity) {
        this.mainActivity = activity;
    }

    public boolean isMusicServiceRunning() {
        ActivityManager manager = (ActivityManager) mainActivity.getApplicationContext().getSystemService(mainActivity.getApplicationContext().ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if("saiboten.no.synclistener.musicservice.MusicService".equals(service.service.getClassName())) {
                Log.d(TAG, "Music Service is running.");
                return true;
            }
        }
        return false;
    }

    public void pausePlayer() {
        Log.d(TAG, "Pausing player");

        Intent intent = new Intent(mainActivity.getApplicationContext(), MusicService.class);
        intent.setAction("PAUSE");

        mainActivity.startService(intent);
    }

    public void resumePlayer() {
        Log.d(TAG, "Resuming player");

        Intent intent = new Intent(mainActivity.getApplicationContext(), MusicService.class);
        intent.setAction("RESUME");

        mainActivity.startService(intent);
    }

    public void startMusicService(String token) {
        Log.d(TAG, "Player enabled");

        Intent intent = new Intent(mainActivity, MusicService.class);
        intent.setAction("START_MUSIC_SERVICE");
        intent.putExtra("accessToken", token);

        mainActivity.startService(intent);
    }

    public void seekToPosition(int i) {
        Log.d(TAG, "Seeking to position " + i);

        Intent intent = new Intent(mainActivity.getApplicationContext(), MusicService.class);
        intent.setAction("SEEK_POSITION");
        intent.putExtra("position", i);

        mainActivity.startService(intent);
    }

    public void play(String nextSong, String playlist) {
        Log.d(TAG, "Playing song: " + nextSong);

        Intent intent = new Intent(mainActivity.getApplicationContext(), MusicService.class);
        intent.setAction("PLAY");
        intent.putExtra("playlist", playlist);
        intent.putExtra("song", nextSong);

        mainActivity.startService(intent);
    }
}
