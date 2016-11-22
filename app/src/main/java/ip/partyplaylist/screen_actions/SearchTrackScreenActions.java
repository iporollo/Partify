package ip.partyplaylist.screen_actions;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;


public interface SearchTrackScreenActions {
    void setTrackList(List<Track> tracks);
}
