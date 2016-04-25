package saiboten.no.synclistener.preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tobias on 24.04.2016.
 */
public class AccessTokenHelper {

    private static final String PREFS_NAME = "SyncListenerAccessToken";

    private static final String TAG = "AccessTokenHelper";

    public static final String TOKEN_EXPIRATION = "token_expiration";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-kk:mm:ss");

    public boolean accessTokenHasExpired(Activity activity) {
        boolean expired = true;
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, 0);
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
}
