package saiboten.no.synclistener.spotifysonginfo;

import saiboten.no.synclistener.spotifysonginfo.model.SpotifySongInfoModel;
import saiboten.no.synclistener.spotifysonginfo.model.SpotifySyncNiceSongInfoModel;

/**
 * Created by Tobias on 24.04.2016.
 */
public class SpotifyModelToUsableModelConverter {
    public static SpotifySyncNiceSongInfoModel convert(SpotifySongInfoModel spotifySongInfoModel) {
        SpotifySyncNiceSongInfoModel newModel = new SpotifySyncNiceSongInfoModel();
        newModel.setArtist(spotifySongInfoModel.getArtists().get(0).getName());
        newModel.setSong(spotifySongInfoModel.getName());
        newModel.setUrlToImage(spotifySongInfoModel.getAlbum().getImages().get(0).getUrl());
        return newModel;
    }
}
