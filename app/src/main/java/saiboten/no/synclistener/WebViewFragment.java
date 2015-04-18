package saiboten.no.synclistener;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Tobias on 26.03.2015.
 */
public class WebViewFragment extends Fragment {

    private View rootView;

    private String selectedPlaylist = "";

    public WebViewFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.

        Log.d("DemoObjectFragmetn", "Fragment CREATED!");

        rootView = inflater.inflate(
                R.layout.webview_layout, container, false);
        WebView webView = (WebView) rootView.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;
            }
        });

        webView.loadUrl("http://spotocracy.net/");
        return rootView;
    }

    public void changeUrl(String playlist) {
        if(playlist.equals(this.selectedPlaylist)) {
            Log.d("WebViewFragment", "Playlist not changed. Not loading url");
        }
        else {
            Log.d("WebViewFragment", "Playlist changed. Loading new url");
            WebView webView = (WebView) rootView.findViewById(R.id.webView);
            webView.loadUrl("http://spotocracy.net/p/"+playlist);
        }
    }

    public static Fragment newInstance() {
        WebViewFragment webViewFragment = new WebViewFragment();
        return webViewFragment;
    }
}
