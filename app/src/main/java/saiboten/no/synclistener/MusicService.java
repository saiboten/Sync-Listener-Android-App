package saiboten.no.synclistener;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.playback.Config;
import com.spotify.sdk.android.playback.Player;

/**
 * Created by Tobias on 27.03.2015.
 */
public class MusicService extends IntentService {

    private static final String CLIENT_ID = "b60120e0052b4973b2a89fab00925019";

    private static final int SOME_ID = 123456;

    MainActivity mainActivity;

    private String currentPlaylist = null;

    public SpotifyPlayerWrapper getSpotifyPlayerWrapper() {
        return spotifyPlayerWrapper;
    }

    SpotifyPlayerWrapper spotifyPlayerWrapper;

    public MusicService() {
        super("no.saiboten.MusicService");
   }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("MusicService","This is never called. Right?");
    }

    public String getCurrentPlaylist() {
        return this.currentPlaylist;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("MusicService", "Received start id " + startId + ": " + intent);

        if(intent.getAction() != null && intent.getAction().equals("STOP_SERVICE")) {
            Log.d("MusicService", "Stopping service");
            stopSelf();
        }
        else if(intent.getAction().equals("STOP_SERVICE")) {
            Log.d("MusicService", "Stopping service");
            stopSelf();
        }
        else if(intent.getAction().equals("PLAY")) {
            Log.d("MusicService", "Playing song");
            getSpotifyPlayerWrapper().play(intent.getStringExtra("song"));
        }
        else if(intent.getAction().equals("PAUSE")) {
            Log.d("MusicService", "Pausing song");
            getSpotifyPlayerWrapper().pause();
        }
        else if(intent.getAction().equals("RESUME")) {
            Log.d("MusicService", "Resuming song");
            getSpotifyPlayerWrapper().resume();
        }
        else if(intent.getAction().equals("SEEK_POSITION")) {
            Log.d("MusicService", "Seeking to another position");
            getSpotifyPlayerWrapper().seekToPosition(intent.getIntExtra("position",0));
        }
        else {
            Log.d("MusicService", "Access token: " + intent.getStringExtra("accessToken"));
            Intent stopServiceIntent = new Intent(this,MusicService.class);
            stopServiceIntent.setAction("STOP_SERVICE");

            PendingIntent stopServicePendingIntent = PendingIntent.getService(getApplicationContext(),12345, stopServiceIntent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.spotocracylogo)
                            .setContentTitle("Stop music")
                            .setContentText("Stop the music please")
                            .addAction(R.drawable.spotocracylogo, "Stop service", stopServicePendingIntent);

            Notification notification = mBuilder.build();
            startForeground(SOME_ID, notification );

            Player player = null;
            spotifyPlayerWrapper = new SpotifyPlayerWrapper();
            SpotifyPlayerNotificationListener spotifyPlayerNotificationListener = new SpotifyPlayerNotificationListener(this);


            Config playerConfig = new Config(getApplicationContext(), intent.getStringExtra("accessToken"), CLIENT_ID);
            spotifyPlayerWrapper.setPlayer(Spotify.getPlayer(playerConfig, this, spotifyPlayerNotificationListener));
        }

        return START_NOT_STICKY;
    }
}
