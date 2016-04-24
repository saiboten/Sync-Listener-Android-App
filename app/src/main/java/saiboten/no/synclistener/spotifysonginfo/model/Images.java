package saiboten.no.synclistener.spotifysonginfo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Tobias on 24.04.2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Images {
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Images{" +
                "url='" + url + '\'' +
                '}';
    }
}
