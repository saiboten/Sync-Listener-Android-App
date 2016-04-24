package saiboten.no.synclistener.spotifysonginfo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Tobias on 24.04.2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Album {

    private List<Images> images;

    public List<Images> getImages() {
        return images;
    }

    public void setImages(List<Images> images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "Album{" +
                "images=" + images +
                '}';
    }
}
