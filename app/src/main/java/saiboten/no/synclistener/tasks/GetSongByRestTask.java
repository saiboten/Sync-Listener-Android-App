package saiboten.no.synclistener.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import saiboten.no.synclistener.callbacks.NewSongFromSyncListenerCallback;

/**
 * Created by Tobias on 15.03.2015.
 */
public class GetSongByRestTask extends AsyncTask<String, Void, String> {

        private NewSongFromSyncListenerCallback newSongFromSyncListenerCallback;

        public GetSongByRestTask(NewSongFromSyncListenerCallback newSongFromSyncListenerCallback) {
            this.newSongFromSyncListenerCallback = newSongFromSyncListenerCallback;
        }

        private Exception exception;

        protected String doInBackground(String... playlists) {
            String playlist = playlists[0];
            String url = "http://178.62.133.37:3000/get_song/" + playlist;
            String returnedData = null;

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();

                InputStream inputStream = httpEntity.getContent();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();

                String ligneLue = bufferedReader.readLine();
                while (ligneLue != null) {
                    stringBuilder.append(ligneLue + " \n");
                    ligneLue = bufferedReader.readLine();
                }
                bufferedReader.close();

                returnedData = stringBuilder.toString();


            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("MainActivity", "Returned json: " + returnedData);
            return returnedData;
        }

        protected void onPostExecute(String result) {
            Log.d("MainActivity", "onPostExecute: result: " + result);
            newSongFromSyncListenerCallback.newSongCallback(result);
        }

}
