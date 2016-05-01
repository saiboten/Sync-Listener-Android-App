package saiboten.no.synclistener.webview;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import butterknife.Bind;
import butterknife.ButterKnife;
import saiboten.no.synclistener.R;

/**
 * Created by Tobias on 26.03.2015.
 */
public class WebViewFragment extends Fragment {

    private final String TAG = "WebViewFragment";

    @Bind(R.id.webView)
    WebView webView;

    private String selectedPlaylist = "";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Fragment CREATED!");

        View view = inflater.inflate(
                R.layout.webview_layout, container, false);
        ButterKnife.bind(this, view);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        return view;
    }



    public void changeUrl(String playlist) {
        if(playlist.equals(this.selectedPlaylist)) {
            Log.d("WebViewFragment", "Playlist not changed. Not loading url");
        } else {
            Log.d("WebViewFragment", "Playlist changed. Loading new url");
            webView.loadUrl("http://spotocracy.net/p/" + playlist);
        }
        this.selectedPlaylist = playlist;
    }
}
