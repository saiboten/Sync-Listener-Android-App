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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import saiboten.no.synclistener.R;
import saiboten.no.synclistener.activity.BaseActivity;
import saiboten.no.synclistener.mainscreen.MainActivity;
import saiboten.no.synclistener.spotifytokenservice.SpotifyTokenSaveService;

public class SetupActivity extends BaseActivity {

    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
    public static final int REQUEST_CODE = 1337;

    public static final String ACCESS_TOKEN = "access_token";

    public static final String TOKEN_EXPIRATION = "token_expiration";

    private final static String TAG = "SetupActivity";

    private static final String CLIENT_ID = "b60120e0052b4973b2a89fab00925019";

    private static final String REDIRECT_URI = "spotocracy://callback";

    private static final String PREFS_NAME = "SyncListenerAccessToken";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-kk:mm:ss");

    @Bind(R.id.setup_activity_setup_button)
    public Button button;

    @Inject
    public SpotifyTokenSaveService spotifyTokenSaveService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        ButterKnife.bind(this);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        String accessToken = sharedPreferences.getString(ACCESS_TOKEN, null);

        Log.d(TAG, "Current access token : " + accessToken);

        Intent inputIntent = getIntent();
        boolean loginFailed = inputIntent.getBooleanExtra("loginFailed", false);

        Log.d(TAG, "Login failed? " + loginFailed);

        if(loginFailed || accessTokenHasExpired()) {
            Log.d(TAG, "Login has failed. We have to get a new access token from spotify.");
        } else if(StringUtils.isNotEmpty(accessToken)) {
            Log.d(TAG, "User already has access token. Lets just start main activity then?");
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.setup_activity_setup_button)
    public void setupButtonClick(View view) {
        setupSpotifyAuthentication();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.d(TAG, "ActivityResult: resultCode" + resultCode);
        Log.d(TAG, "ActivityResult: request code" + requestCode);

        // Check if result comes from the correct activity
        if(requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            Log.d("MainActivity", "Response type:" + response.getType());

            switch(response.getType()) {
                case TOKEN:
                    Log.d("MainActivity", "Token granted: " + response);
                    Log.d("MainActivity", "Expires: " + response.getExpiresIn());

                    spotifyTokenSaveService.storeAuthenticationResult(response);

                    SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(ACCESS_TOKEN, response.getAccessToken());
                    editor.putString(TOKEN_EXPIRATION, getExpiresDateAsString(response.getExpiresIn()));
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

    public String getExpiresDateAsString(int expiresIn) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, expiresIn);
        String expiresDateAsString = sdf.format(calendar.getTime());

        Log.d(TAG, "Storing this expired string date: " + expiresDateAsString);
        Log.d(TAG, "For comparison, now is: " + sdf.format(new Date()));

        return expiresDateAsString;
    }

    public boolean accessTokenHasExpired() {
        boolean expired = true;
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        String tokenExpiration = sharedPreferences.getString(TOKEN_EXPIRATION, null);
        Log.d(TAG, "Raw stored expires string: " + tokenExpiration);


        if(tokenExpiration != null) {
            try {
                Date now = new Date();
                Date expiredDate = sdf.parse(tokenExpiration);
                Log.d(TAG, "Parsed expiration date: " + tokenExpiration);
                Log.d(TAG, "Now: " + now);

                if(now.before(expiredDate)) {
                    Log.d(TAG, "Token has not expired");
                    expired = false;
                }
            } catch(ParseException e) {
                Log.e(TAG, "Wups, could not format date?");
                e.printStackTrace();
            }
        }

        Log.d(TAG, "Has the access token expires ?" + expired);
        return expired;
    }

    public void setupSpotifyAuthentication() {
        Log.d(TAG, "Authenticating and starting music service");
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, this.REQUEST_CODE, request);
    }

}
