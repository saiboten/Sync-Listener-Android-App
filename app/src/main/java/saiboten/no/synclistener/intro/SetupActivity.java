package saiboten.no.synclistener.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

import saiboten.no.synclistener.activity.BaseActivity;
import saiboten.no.synclistener.mainscreen.MainActivity;
import saiboten.no.synclistener.musicservicecommunicator.MusicServiceCommunicator;
import saiboten.no.synclistener.R;
import saiboten.no.synclistener.spotifytokenservice.SpotifyTokenSaveService;

public class SetupActivity extends BaseActivity {

    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
    public static final int REQUEST_CODE = 1337;

    private final static String TAG = "SetupActivity";

    private static final String CLIENT_ID = "b60120e0052b4973b2a89fab00925019";

    private static final String REDIRECT_URI = "spotocracy://callback";
    private static final String PREFS_NAME = "SyncListenerAccessToken";
    public static final String ACCESS_TOKEN = "access_token";

    @Inject
    public MusicServiceCommunicator musicServiceCommunicator;

    @Inject
    public SpotifyTokenSaveService spotifyTokenSaveService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        musicServiceCommunicator.setActivity(this);

        Button button = (Button) findViewById(R.id.setup_activity_setup_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupSpotifyAuthentication();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME,0);
        String accessToken = sharedPreferences.getString(ACCESS_TOKEN,null);

        Log.d(TAG, "Current access token : " + accessToken);

        //Should also validate that it works, in some way ?

        if(StringUtils.isNotEmpty(accessToken)) {
            Log.d(TAG, "User already has access token. Lets just start main activity then?");
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.d(TAG, "ActivityResult: resultCode" + resultCode);
        Log.d(TAG, "ActivityResult: request code" + requestCode);
        Log.d(TAG, "ActivityResult: Intent: " + intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            Log.d("MainActivity", "Response type:" + response.getType());

            switch (response.getType()) {
                case TOKEN:
                    Log.d("MainActivity", "Token granted: " + response);
                    spotifyTokenSaveService.storeAuthenticationResult(response);

                    SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME,0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(ACCESS_TOKEN, response.getAccessToken());
                    editor.commit();

                    Intent mainActivityIntent = new Intent(this, MainActivity.class);
                    startActivity(mainActivityIntent);
                    break;
                case ERROR:
                    Log.d("MainActivity", "Some error has occured: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.d("MainActivity", "Default, user cancelled? " + response.getError());
                    // Handle other cases
            }
        }
    }

    public void setupSpotifyAuthentication() {
            Log.d(TAG,"Authenticating and starting music service");
            AuthenticationRequest.Builder builder =
                    new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

            builder.setScopes(new String[]{"streaming"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(this, this.REQUEST_CODE, request);
    }

}
