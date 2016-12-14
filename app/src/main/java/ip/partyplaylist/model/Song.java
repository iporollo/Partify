package ip.partyplaylist.model;

import java.io.Serializable;


public class Song implements Serializable {
    public String songName;
    public String songArtistName;
    public String songID;

    public Song() {}

    public Song(String name, String artistName, String id) {
        songName = name;
        songArtistName = artistName;
        songID = id;
    }
}
