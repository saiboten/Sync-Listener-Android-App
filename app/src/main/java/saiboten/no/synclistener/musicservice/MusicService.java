package saiboten.no.synclistener.musicservice;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONException;
import org.json.JSONObject;

import saiboten.no.synclistener.mainscreen.MainActivity;
import saiboten.no.synclistener.R;
import saiboten.no.synclistener.callbacks.NewSongFromSyncListenerCallback;
import saiboten.no.synclistener.tasks.GetNextSongByRestTask;

/**
 * Created by Tobias on 27.03.2015.
 */
public class MusicService extends IntentService implements NewSongFromSyncListenerCallback {

    private static final String CLIENT_ID = "b60120e0052b4973b2a89fab00925019";

    private static final int SOME_ID = 123456;

    private String currentPlaylist = null;

    public SpotifyPlayerWrapper getSpotifyPlayerWrapper() {
        return spotifyPlayerWrapper;
    }

    SpotifyPlayerWrapper spotifyPlayerWrapper;

    private HeadSetDisconnectedBroadcastReceiver receiver;

    private final static String TAG = "MusicService";

    private boolean headsetConnected = false;

    public MusicService() {
        super("no.saiboten.MusicService");
   }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG,"This is never called. Right?");
    }

    public String getCurrentPlaylist() {
        return this.currentPlaylist;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Received start id " + startId + ": " + intent);

        if(intent.getAction() != null && intent.getAction().equals("STOP_SERVICE")) {
            Log.d(TAG, "Stopping service");
            spotifyPlayerWrapper.getPlayer().shutdown();
            stopSelf();
        }
        else if(intent.getAction().equals("PLAY")) {
            Log.d(TAG, "Playing song");
            currentPlaylist = intent.getStringExtra("playlist");
            getSpotifyPlayerWrapper().play(intent.getStringExtra("song"));
        }
        else if(intent.getAction().equals("PAUSE")) {
            Log.d(TAG, "Pausing song");

            Intent pause = new Intent(MainActivity.PAUSE);
            pause.setAction("no.saiboten.synclistener.PAUSE");
            LocalBroadcastManager.getInstance(this).sendBroadcast(pause);

            getSpotifyPlayerWrapper().pause();
        }
        else if(intent.getAction().equals("RESUME")) {
            Intent resume = new Intent(MainActivity.RESUME);
            resume.setAction("no.saiboten.synclistener.RESUME");
            LocalBroadcastManager.getInstance(this).sendBroadcast(resume);

            Log.d(TAG, "Resuming song");
            getSpotifyPlayerWrapper().resume();
        }
        else if(intent.getAction().equals("SEEK_POSITION")) {
            Log.d(TAG, "Seeking to another position");
            getSpotifyPlayerWrapper().seekToPosition(intent.getIntExtra("position", 0));
        }
        else {
            IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            receiver = new HeadSetDisconnectedBroadcastReceiver();
            registerReceiver( receiver, receiverFilter );

            Log.d(TAG, "Access token: " + intent.getStringExtra("accessToken"));
            Intent stopServiceIntent = new Intent(this,MusicService.class);
            stopServiceIntent.setAction("STOP_SERVICE");

            Intent pauseServiceIntent = new Intent(this,MusicService.class);
            pauseServiceIntent.setAction("PAUSE");

            Intent resumeServiceIntent = new Intent(this,MusicService.class);
            resumeServiceIntent.setAction("RESUME");

            Intent openSyncListenerIntent = new Intent(this,MainActivity.class);
            openSyncListenerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent openSyncListener = PendingIntent.getActivity(getApplicationContext(), 12345, openSyncListenerIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            PendingIntent stopServicePendingIntent = PendingIntent.getService(getApplicationContext(),12345, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pauseServicePendingIntent = PendingIntent.getService(getApplicationContext(),12345, pauseServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent resumeServicePendingIntent = PendingIntent.getService(getApplicationContext(),12345, resumeServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.spotocracylogo)
                            .setContentTitle("Sync Listener")
                            .setContentText("Swipe ned for avspillingsvalg")
                            .setContentIntent(openSyncListener)
                            .addAction(R.drawable.stop, "", stopServicePendingIntent)
                              .addAction(R.drawable.pause, "", pauseServicePendingIntent)
                             .addAction(R.drawable.play, "", resumeServicePendingIntent);

            Notification notification = mBuilder.build();
            startForeground(SOME_ID, notification );

            Log.d(TAG, "MusicService created successfully. Notifications ready");

            Player player = null;
            spotifyPlayerWrapper = new SpotifyPlayerWrapper();
            SpotifyPlayerNotificationListener spotifyPlayerNotificationListener = new SpotifyPlayerNotificationListener(this);

            Log.d(TAG, "Spotify player wrapper created. Notification listener created");

            Config playerConfig = new Config(getApplicationContext(), intent.getStringExtra("accessToken"), CLIENT_ID);

            Log.d(TAG, "Config created");
            spotifyPlayerWrapper.setPlayer(Spotify.getPlayer(playerConfig, this, spotifyPlayerNotificationListener));
            Log.d(TAG, "Spotify player ready");
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        Spotify.destroyPlayer(this);
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void playNewSong() {
        new GetNextSongByRestTask(this).execute(currentPlaylist);
    }

    @Override
    public void newSongCallback(String result) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            JSONObject song = jsonObject.getJSONObject("song");
            String nextSong = song.getString("uri");
            getSpotifyPlayerWrapper().play(nextSong);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class HeadSetDisconnectedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                if (state == 0 && headsetConnected) {
                    Log.d(TAG, "Headset unplugged");
                    headsetConnected = false;
                    getSpotifyPlayerWrapper().pause();
                }
                else if(state == 1 && !headsetConnected) {
                    Log.d(TAG, "Headset plugged");
                    headsetConnected = true;
                }
            }
        }
    }

}

