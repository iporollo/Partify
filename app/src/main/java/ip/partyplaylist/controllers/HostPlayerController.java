package ip.partyplaylist.controllers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import ip.partyplaylist.model.Party;
import ip.partyplaylist.model.Song;
import ip.partyplaylist.screen_actions.HostPlayerScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ivan on 12/13/2016.
 */

public class HostPlayerController {

    private HostPlayerScreenActions mHostPlayerScreenActions;
    private SharedPreferenceHelper mSharedPreferenceHelper;
    private SpotifyService mSpotifyService;
    private SpotifyApi mSpotifyApi;
    private Party mCurrentParty;
    private ArrayList<Song> mTrackList;
    private Playlist mCurrentPlaylist;


    public HostPlayerController(HostPlayerScreenActions hostPlayerScreenActions) {
        mHostPlayerScreenActions = hostPlayerScreenActions;

        mSharedPreferenceHelper = new SharedPreferenceHelper((Context) mHostPlayerScreenActions);

        mSpotifyApi = new SpotifyApi();
        mSpotifyApi.setAccessToken(mSharedPreferenceHelper.getCurrentSpotifyToken());
        mSpotifyService = mSpotifyApi.getService();

    }

    public void onAddTrackButtonPressed() {
        mHostPlayerScreenActions.showSearchTrackScreen();
    }


    public Party getInitialParty(ArrayList<Song> tracks){
        mCurrentParty = new Party(mSharedPreferenceHelper.getCurrentPlaylistName(), tracks);
        mCurrentParty.setPlaylistId(mSharedPreferenceHelper.getCurrentPlaylistId());
        mCurrentParty.setHostId(mSharedPreferenceHelper.getCurrentUserId());

        return mCurrentParty;
    }


    public void updateLocalCurrentPlaylist() {
        String playlistDesired = mSharedPreferenceHelper.getCurrentPlaylistId();

        mCurrentPlaylist = mSpotifyService.getPlaylist(mSharedPreferenceHelper.getCurrentUserId(), playlistDesired);
    }

    public ArrayList<Song> createSongModelArrayList() {

        updateLocalCurrentPlaylist();

        mTrackList = new ArrayList<>();

        for (int i = 0; i < mCurrentPlaylist.tracks.total; i++) {

            Song currentSong = new Song(mCurrentPlaylist.tracks.items.get(i).track.name,
                    mCurrentPlaylist.tracks.items.get(i).track.artists.get(0).name,
                    mCurrentPlaylist.tracks.items.get(i).track.uri,
                    mCurrentPlaylist.tracks.items.get(i).track.album.images.get(0).url);

            mTrackList.add(currentSong);
        }

        return mTrackList;

    }

    public void updateCurrentSpotifyPlaylist(Party currentParty) {

        StringBuffer tracksParams = new StringBuffer();


        for(int i =0; i < currentParty.trackList.size(); i++){
            boolean alreadyInPlaylist = false;
            Song track = currentParty.trackList.get(i);
            for(int j = 0; j < mCurrentPlaylist.tracks.items.size(); j++){
                if(mCurrentPlaylist.tracks.items.get(j).track.uri.equals(track.songID)){
                    alreadyInPlaylist = true;
                }
            }
            if(!alreadyInPlaylist){
                tracksParams.append(track.songID).append(",");
            }
        }


        mCurrentParty = currentParty;


        HashMap parametersMap = new HashMap();
        parametersMap.put("uris", tracksParams.toString());


        mSpotifyService.addTracksToPlaylist(
                currentParty.hostId,
                currentParty.playlistId,
                parametersMap,
                new HashMap<String, Object>(),
                new Callback<Pager<PlaylistTrack>>() {
                    @Override
                    public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                        updateLocalCurrentPlaylist();
                        updateCurrentFirebaseParty();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("HostPlayerActivity", "Adding Tracks to Playlist Failed.");
                    }
                });

    }

    public void updateCurrentFirebaseParty() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("parties");

        myRef.child(mSharedPreferenceHelper.getCurrentPartyId()).setValue(mCurrentParty, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            }
        });
    }


}


