package saiboten.no.synclistener.webview;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import butterknife.Bind;
import butterknife.ButterKnife;
import saiboten.no.synclistener.R;
import saiboten.no.synclistener.activity.MainActivity;

/**
 * Created by Tobias on 26.03.2015.
 */
public class WebViewFragment extends Fragment {

    private final String TAG = "WebViewFragment";

    @Bind(R.id.webView)
    WebView webView;

    private String selectedPlaylist = "";

    private boolean loading = false;

    private WebViewTypes webViewType;

    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Fragment CREATED!");

        View view = inflater.inflate(
                R.layout.webview_layout, container, false);
        ButterKnife.bind(this, view);
        mainActivity = (MainActivity) getActivity();

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                Log.d(TAG, "Progress changed: " + progress);

                if(!loading) {
                    loading = true;
                    mainActivity.setupHorizontalProgressDialog();
                }

                mainActivity.updateProgressDialogProgress(progress);

                if(progress == 100) {
                    mainActivity.popup.hide();
                    loading = false;
                }
            }
        });

        return view;
    }

    public static Fragment getInstance() {
        return new WebViewFragment();
    }

    public void loadPlaylist(String playlist) {
        if(playlist.equals(this.selectedPlaylist) && webViewType == WebViewTypes.PLAYLIST) {
            Log.d("WebViewFragment", "Playlist not changed. Not loading url");
        } else {
            Log.d("WebViewFragment", "Playlist changed. Loading new url");
            webView.loadUrl("http://spotocracy.net/p/" + playlist + "#/playlist");
        }
        this.selectedPlaylist = playlist;
        webViewType = WebViewTypes.PLAYLIST;
    }

    public void loadSearch(String playlist) {
        webView.loadUrl("http://spotocracy.net/p/" + playlist + "#/search");

//        if(playlist.equals(this.selectedPlaylist) && webViewType == WebViewTypes.SEARCH) {
//            Log.d("WebViewFragment", "Playlist not changed and we previously loaded the search. Nothing has changed. Not loading url");
//        } else {
//            Log.d("WebViewFragment", "Playlist changed. Loading new url");
//            webView.loadUrl("http://spotocracy.net/p/" + playlist + "#/search");
//        }
        this.selectedPlaylist = playlist;
        webViewType = WebViewTypes.SEARCH;
    }
}
