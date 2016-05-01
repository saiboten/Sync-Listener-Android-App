package saiboten.no.synclistener.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import saiboten.no.synclistener.activity.BaseActivity;
import saiboten.no.synclistener.setup.SetupActivity;
import saiboten.no.synclistener.activity.MainActivity;
import saiboten.no.synclistener.mainscreen.MusicPlayerFragment;
import saiboten.no.synclistener.musicservicecommunicator.MusicServiceCommunicator;
import saiboten.no.synclistener.preferences.AccessTokenHelper;
import saiboten.no.synclistener.spotifytokenservice.SpotifyTokenSaveService;
import saiboten.no.synclistener.webview.WebViewFragment;

/**
 * Created by Tobias on 21.02.2016.
 */
@Module(library = true, injects = {BaseActivity.class, SetupActivity.class, MusicPlayerFragment.class, MainActivity.class})
public class DaggerModule {

    private final BaseApplication application;

    public DaggerModule(BaseApplication baseApplication) {
        this.application = baseApplication;
    }

    @Provides
    public MusicServiceCommunicator getMusicServiceCommunicator() {
        return new MusicServiceCommunicator();
    }

    @Provides
    @Singleton
    public SpotifyTokenSaveService getSpotifyTokenSaveService() {
        return new SpotifyTokenSaveService();
    }

    @Provides
    @Singleton
    public MusicPlayerFragment getMusicPlayerFragment() {
        return new MusicPlayerFragment();
    }

    @Singleton
    @Provides
    public WebViewFragment getWebViewFragment() {
        return new WebViewFragment();
    }

    @Provides
    @Singleton
    public AccessTokenHelper accessTokenHelper() {
        return new AccessTokenHelper();
    }

}
