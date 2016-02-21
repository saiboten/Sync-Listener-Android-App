package saiboten.no.synclistener.spotifytokenservice;

import com.spotify.sdk.android.authentication.AuthenticationResponse;

/**
 * Created by Tobias on 21.02.2016.
 */
public class SpotifyTokenSaveService {

    private AuthenticationResponse authenticationResponse;

    public void storeAuthenticationResult(AuthenticationResponse authenticationResponse) {
        this.authenticationResponse = authenticationResponse;
    }

    public AuthenticationResponse getAuthenticationResponse() {
        return authenticationResponse;
    }
}
