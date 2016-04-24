package saiboten.no.synclistener.spotifysonginfo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Tobias on 24.04.2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Artists {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Artists{" +
                "name='" + name + '\'' +
                '}';
    }
}
