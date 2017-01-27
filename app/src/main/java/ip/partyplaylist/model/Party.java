package ip.partyplaylist.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by az on 21/05/16.
 */
@IgnoreExtraProperties
public class Party implements Serializable {

    public String name;
    public ArrayList<Song> trackList;
    public String playlistId;
    public String hostId;
    public String partyId;

    public Party() {

    }

    public Party(String name, ArrayList<Song> trackList) {
        this.name = name;
        this.trackList = trackList;
    }

    public void addTrack(Song trackToAdd) {
        trackList.add(trackToAdd);
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }
}
