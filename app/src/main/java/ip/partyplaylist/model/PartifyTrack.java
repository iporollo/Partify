package ip.partyplaylist.model;

import java.io.Serializable;

/**
 * Created by az on 29/05/16.
 */
public class PartifyTrack implements Serializable {
    public String mName;
    public String mArtistName;
    public String mId;

    public PartifyTrack() {}

    public PartifyTrack(String name, String artistName, String id) {
        mName = name;
        mArtistName = artistName;
        mId = id;
    }
}
