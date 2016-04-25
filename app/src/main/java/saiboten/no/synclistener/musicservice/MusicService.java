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
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;

import saiboten.no.synclistener.R;
import saiboten.no.synclistener.intro.SetupActivity;
import saiboten.no.synclistener.mainscreen.MainActivity;
import saiboten.no.synclistener.synclistenerrest.NextSongService;
import saiboten.no.synclistener.synclistenerrest.callback.NextSongFromSynclistenerCallback;
import saiboten.no.synclistener.synclistenerrest.model.SyncListenerSongInfo;

/**
 * Created by Tobias on 27.03.2015.
 */
public class MusicService extends IntentService implements NextSongFromSynclistenerCallback {

    private static final String CLIENT_ID = "b60120e0052b4973b2a89fab00925019";

    private static final int SOME_ID = 123456;

    private final static String TAG = "MusicService";

    SpotifyPlayerWrapper spotifyPlayerWrapper;

    private String currentPlaylist = null;

    private HeadSetDisconnectedBroadcastReceiver receiver;

    private boolean headsetConnected = false;

    private NextSongService nextSongService;

    public MusicService() {
        super("no.saiboten.MusicService");
        nextSongService = new NextSongService();
    }

    public SpotifyPlayerWrapper getSpotifyPlayerWrapper() {
        return spotifyPlayerWrapper;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG, "This is never called. Right?");
    }

    public String getCurrentPlaylist() {
        return this.currentPlaylist;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Received start id " + startId + ": " + intent);

        if(intent.getAction() != null && intent.getAction().equals("STOP_SERVICE")) {
            stopService();
        } else if(intent.getAction().equals("PLAY")) {
            play(intent);
        } else if(intent.getAction().equals("PAUSE")) {
            pause();
        } else if(intent.getAction().equals("RESUME")) {
            resume();
        } else if(intent.getAction().equals("SEEK_POSITION")) {
            seekPosition(intent);
        } else if(intent.getAction().equals("PLAYPAUSESTATUS")) {
            playPauseStatus();
        } else {
            setup(intent);
        }

        return START_NOT_STICKY;
    }

    private void playPauseStatus() {
        Log.d(TAG, "Someone wants play status");
        final MusicService hm = this;
        getSpotifyPlayerWrapper().getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                Intent playstatus = new Intent(MainActivity.PLAYINGSTATUS);
                Log.d(TAG, "Broadcasing player state: " + playerState.playing);
                playstatus.setAction("no.saiboten.synclistener.PLAYINGSTATUS");
                playstatus.putExtra("status", playerState.playing);
                LocalBroadcastManager.getInstance(hm).sendBroadcast(playstatus);
            }
        });
    }

    private void setup(Intent intent) {
        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        receiver = new HeadSetDisconnectedBroadcastReceiver();
        registerReceiver(receiver, receiverFilter);

        Log.d(TAG, "Access token: " + intent.getStringExtra("accessToken"));
        Intent stopServiceIntent = new Intent(this, MusicService.class);
        stopServiceIntent.setAction("STOP_SERVICE");

        Intent pauseServiceIntent = new Intent(this, MusicService.class);
        pauseServiceIntent.setAction("PAUSE");

        Intent resumeServiceIntent = new Intent(this, MusicService.class);
        resumeServiceIntent.setAction("RESUME");

        Intent openSyncListenerIntent = new Intent(this, MainActivity.class);
        openSyncListenerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        PendingIntent openSyncListener = PendingIntent.getActivity(getApplicationContext(), 12345, openSyncListenerIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent stopServicePendingIntent = PendingIntent.getService(getApplicationContext(), 12346, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pauseServicePendingIntent = PendingIntent.getService(getApplicationContext(), 12347, pauseServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent resumeServicePendingIntent = PendingIntent.getService(getApplicationContext(), 12348, resumeServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
        startForeground(SOME_ID, notification);

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

    private void seekPosition(Intent intent) {
        Log.d(TAG, "Seeking to another position");
        if(getSpotifyPlayerWrapper() != null && intent != null) {
            getSpotifyPlayerWrapper().seekToPosition(intent.getIntExtra("position", 0));
        }
    }

    private void resume() {
        Intent resume = new Intent(MainActivity.RESUME);
        resume.setAction("no.saiboten.synclistener.RESUME");
        LocalBroadcastManager.getInstance(this).sendBroadcast(resume);

        Log.d(TAG, "Resuming song");
        getSpotifyPlayerWrapper().resume();
    }

    private void pause() {
        Log.d(TAG, "Pausing song");

        Intent pause = new Intent(MainActivity.PAUSE);
        pause.setAction("no.saiboten.synclistener.PAUSE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(pause);

        getSpotifyPlayerWrapper().pause();
    }

    private void play(Intent intent) {
        String songToPlay = intent.getStringExtra("song");
        currentPlaylist = intent.getStringExtra("playlist");

        Log.d(TAG, "Playing song: " + songToPlay + ", from playlist: " + currentPlaylist);

        getSpotifyPlayerWrapper().play(intent.getStringExtra("song"));
    }

    private void stopService() {
        Log.d(TAG, "Stopping service");
        spotifyPlayerWrapper.getPlayer().shutdown();
        stopSelf();
    }

    public void playNewSong() {
        nextSongService.getNextSong(this, currentPlaylist);
    }

    @Override
    public void getNextSongSuccess(SyncListenerSongInfo syncListenerSongInfo) {
        String nextSong = syncListenerSongInfo.getSongTop().getSongAgain().getUri();
        getSpotifyPlayerWrapper().play(nextSong);
    }

    @Override
    public void getNextSongAndPlaySuccess(SyncListenerSongInfo syncListenerSongInfo) {
        // Not in use here
    }

    @Override
    public void getNextSongFailed() {

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        Spotify.destroyPlayer(this);
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    class HeadSetDisconnectedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                if(state == 0 && headsetConnected) {
                    Log.d(TAG, "Headset unplugged");
                    headsetConnected = false;
                    getSpotifyPlayerWrapper().pause();
                } else if(state == 1 && !headsetConnected) {
                    Log.d(TAG, "Headset plugged");
                    headsetConnected = true;
                }
            }
        }
    }

}

