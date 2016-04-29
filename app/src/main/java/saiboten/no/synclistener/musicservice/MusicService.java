package saiboten.no.synclistener.musicservice;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;

import saiboten.no.synclistener.R;
import saiboten.no.synclistener.activity.MainActivity;
import saiboten.no.synclistener.spotifysonginfo.SongInfoServiceFromSpotify;
import saiboten.no.synclistener.spotifysonginfo.callback.SongInfoFromSpotifyCallback;
import saiboten.no.synclistener.spotifysonginfo.model.SpotifySyncNiceSongInfoModel;
import saiboten.no.synclistener.synclistenerrest.NextSongService;
import saiboten.no.synclistener.synclistenerrest.callback.NextSongFromSynclistenerCallback;
import saiboten.no.synclistener.synclistenerrest.model.SyncListenerSongAgain;
import saiboten.no.synclistener.synclistenerrest.model.SyncListenerSongInfo;
import saiboten.no.synclistener.tasks.DownloadImageTask;

/**
 * Created by Tobias on 27.03.2015.
 */
public class MusicService extends IntentService implements NextSongFromSynclistenerCallback, SongInfoFromSpotifyCallback {

    private static final String CLIENT_ID = "b60120e0052b4973b2a89fab00925019";

    private static final int NOTIFICATION_ID = 13337;

    private final static String TAG = "MusicService";

    SpotifyPlayerWrapper spotifyPlayerWrapper;

    private String currentPlaylist = null;

    private HeadSetDisconnectedBroadcastReceiver receiver;

    private boolean headsetConnected = false;

    private NextSongService nextSongService;

    RemoteViews bigRemoteView;

    RemoteViews smallRemoteView;

    NotificationCompat.Builder builder;

    NotificationManager notificationManager;

    Notification notification;

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
        } else if(intent.getAction().equals("PAUSE_OR_RESUME")) {
            pauseOrResume();
        } else if(intent.getAction().equals("SEEK_POSITION")) {
            seekPosition(intent);
        } else if(intent.getAction().equals("SEEK_ACTIVITY")) {
            synchronizeThroughtActivity();
        } else if(intent.getAction().equals("PLAYPAUSESTATUS")) {
            playPauseStatus();
        } else {
            setup(intent);
        }

        return START_NOT_STICKY;
    }

    private void synchronizeThroughtActivity() {
        Log.d(TAG, "Launching synchronize and play intent");
        Intent synchronize = new Intent(MainActivity.SYNCHRONIZE_AND_PLAY);
        synchronize.setAction("no.saiboten.synclistener.SYNCHRONIZE_AND_PLAY");
        LocalBroadcastManager.getInstance(this).sendBroadcast(synchronize);
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

        setupNotificationBar();

        Log.d(TAG, "MusicService created successfully. Notifications ready");

        Player player = null;
        spotifyPlayerWrapper = new SpotifyPlayerWrapper();
        SpotifyPlayerNotificationListener spotifyPlayerNotificationListener = new SpotifyPlayerNotificationListener(this);

        Log.d(TAG, "Spotify player wrapper created. Notification listener created");
        Log.d(TAG, "Access token: " + intent.getStringExtra("accessToken"));

        Config playerConfig = new Config(getApplicationContext(), intent.getStringExtra("accessToken"), CLIENT_ID);

        Log.d(TAG, "Config created");
        spotifyPlayerWrapper.setPlayer(Spotify.getPlayer(playerConfig, this, spotifyPlayerNotificationListener));
        Log.d(TAG, "Spotify player ready");
    }

    private void setupNotificationBar() {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent synchronizeFromActivityServiceIntent = new Intent(this, MusicService.class);
        synchronizeFromActivityServiceIntent.setAction("SEEK_ACTIVITY");

        Intent pauseOrResumeServiceIntent = new Intent(this, MusicService.class);
        pauseOrResumeServiceIntent.setAction("PAUSE_OR_RESUME");

        Intent stopServiceIntent = new Intent(this, MusicService.class);
        stopServiceIntent.setAction("STOP_SERVICE");

        Intent openSyncListenerIntent = new Intent(this, MainActivity.class);
        openSyncListenerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        PendingIntent openSyncListener = PendingIntent.getActivity(getApplicationContext(), 12345, openSyncListenerIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent stopServicePendingIntent = PendingIntent.getService(getApplicationContext(), 12346, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pauseOrResumeServicePendingIntent = PendingIntent.getService(getApplicationContext(), 12347, pauseOrResumeServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent synchronizeServicePendingIntent = PendingIntent.getService(getApplicationContext(), 12348, synchronizeFromActivityServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        bigRemoteView = new RemoteViews(getPackageName(),
                R.layout.notification);

        bigRemoteView.setOnClickPendingIntent(R.id.notification_logo, openSyncListener);
        bigRemoteView.setOnClickPendingIntent(R.id.notification_stop, stopServicePendingIntent);
        bigRemoteView.setOnClickPendingIntent(R.id.notification_pause_or_resume, pauseOrResumeServicePendingIntent);
        bigRemoteView.setOnClickPendingIntent(R.id.notification_synchronize, synchronizeServicePendingIntent);

        smallRemoteView = new RemoteViews(getPackageName(),
                R.layout.notification_small);

        smallRemoteView.setOnClickPendingIntent(R.id.notification_logo, openSyncListener);
        smallRemoteView.setOnClickPendingIntent(R.id.notification_stop, stopServicePendingIntent);
        smallRemoteView.setOnClickPendingIntent(R.id.notification_pause_or_resume, pauseOrResumeServicePendingIntent);
        smallRemoteView.setOnClickPendingIntent(R.id.notification_synchronize, synchronizeServicePendingIntent);

        builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.spotocracylogo).setContent(smallRemoteView);

        notification = builder.build();
        notification.bigContentView = bigRemoteView;

        startForeground(NOTIFICATION_ID, notification);
    }

    private void seekPosition(Intent intent) {
        Log.d(TAG, "Seeking to another position");
        if(getSpotifyPlayerWrapper() != null && intent != null) {
            getSpotifyPlayerWrapper().seekToPosition(intent.getIntExtra("position", 0));
        }
    }

    private void pauseOrResume() {
        final MusicService musicService = this;

        getSpotifyPlayerWrapper().getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                if(playerState.playing) {
                    Log.d(TAG, "Pausing song");
                    spotifyPlayerWrapper.pause();
                    Intent pause = new Intent(MainActivity.PAUSE);
                    pause.setAction("no.saiboten.synclistener.PAUSE");
                    LocalBroadcastManager.getInstance(musicService).sendBroadcast(pause);
                    smallRemoteView.setImageViewResource(R.id.notification_pause_or_resume, R.drawable.playoptional_small);
                    bigRemoteView.setImageViewResource(R.id.notification_pause_or_resume, R.drawable.playoptional_small);
                    notificationManager.notify(NOTIFICATION_ID, notification);

                } else {
                    Log.d(TAG, "Resuming song");
                    spotifyPlayerWrapper.resume();
                    Intent resume = new Intent(MainActivity.RESUME);
                    resume.setAction("no.saiboten.synclistener.RESUME");
                    LocalBroadcastManager.getInstance(musicService).sendBroadcast(resume);
                    smallRemoteView.setImageViewResource(R.id.notification_pause_or_resume, R.drawable.pause_small);
                    bigRemoteView.setImageViewResource(R.id.notification_pause_or_resume, R.drawable.pause_small);
                    notificationManager.notify(NOTIFICATION_ID, notification);
                }
            }
        });
    }

    private void play(Intent intent) {
        currentPlaylist = intent.getStringExtra("playlist");
        playNewSong();
    }

    private void stopService() {
        Log.d(TAG, "Stopping service");
        spotifyPlayerWrapper.getPlayer().shutdown();
        stopSelf();
    }

    public void playNewSong() {
        final MusicService ms = this;
        nextSongService.getNextSong(ms, currentPlaylist);
    }

    @Override
    public void getNextSongSuccess(SyncListenerSongInfo syncListenerSongInfo) {
        String nextSong = syncListenerSongInfo.getSongTop().getSongAgain().getUri();
        Log.d(TAG, "Song to play (id/uri): " + nextSong);
        getSpotifyPlayerWrapper().play(nextSong);
        updateSongInfoOnNotifications(syncListenerSongInfo.getSongTop().getSongAgain());

        String trackId = nextSong.substring(14, nextSong.length());
        new SongInfoServiceFromSpotify().getSongInfoFromSpotify(this, trackId);
    }

    private void updateSongInfoOnNotifications(SyncListenerSongAgain songAgain) {
        Log.d(TAG, "Updating song info on notifications: " + songAgain);

        bigRemoteView.setTextViewText(R.id.MusicService_TextView_song, songAgain.getName());
        bigRemoteView.setTextViewText(R.id.MusicService_TextView_artist, songAgain.getArtist());
        bigRemoteView.setTextViewText(R.id.MusicService_TextView_album, songAgain.getAlbum());

        String songName = songAgain.getName().length() > 12 ? songAgain.getName().substring(0,12) + ".." : songAgain.getName();
        String artistName = songAgain.getArtist().length() > 12 ? songAgain.getArtist().substring(0,12) + ".." : songAgain.getArtist();
        String albumName = songAgain.getAlbum().length() > 12 ? songAgain.getAlbum().substring(0,12) + ".." : songAgain.getAlbum();

        smallRemoteView.setTextViewText(R.id.MusicService_TextView_song, songName);
        smallRemoteView.setTextViewText(R.id.MusicService_TextView_artist, artistName);
        smallRemoteView.setTextViewText(R.id.MusicService_TextView_album, albumName);

        notificationManager.notify(NOTIFICATION_ID, notification);
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

    @Override
    public void songFromSpotifySuccessCallback(SpotifySyncNiceSongInfoModel spotifySongInfoModel) {
        // TODO update image here
    }

    @Override
    public void songFromSpotifyFailedCallback() {

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

