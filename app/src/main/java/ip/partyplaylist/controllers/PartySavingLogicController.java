package ip.partyplaylist.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import ip.partyplaylist.model.Party;
import ip.partyplaylist.screen_actions.LoginScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Playlist;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ivan on 3/13/2017.
 */

public class PartySavingLogicController {
    private static final String TAG = CreatePartyController.class.getSimpleName();
    private final LoginScreenActions mLoginScreenActions;
    private final SharedPreferenceHelper mSharedPreferenceHelper;
    private final SpotifyService mSpotifyService;
    private Party mCurrentParty;
    private String partyID;
    private OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");



    public PartySavingLogicController(LoginScreenActions loginScreenActions) {
        mLoginScreenActions = loginScreenActions;
        mSharedPreferenceHelper = new SharedPreferenceHelper((Context) loginScreenActions);


        SpotifyApi mSpotifyApi = new SpotifyApi();
        mSpotifyApi.setAccessToken(mSharedPreferenceHelper.getCurrentSpotifyToken());
        mSpotifyService = mSpotifyApi.getService();
    }

    public void saveParty(Party party){
        mCurrentParty = party;
        mSharedPreferenceHelper.saveCurrentSpotifyPlayListName(mCurrentParty.name);
        mCurrentParty.setHostId(mSharedPreferenceHelper.getCurrentSpotifyUserId());
        mCurrentParty.setPartyAccessToken(mSharedPreferenceHelper.getCurrentSpotifyToken());

        createUniquePartyID();
        createSpotifyPlaylist();
    }

    private void createUniquePartyID() {
        String partyID = "";
        double d;
        for (int i = 1; i <= 5; i++) {
            d = Math.random() * 10;
            partyID = partyID + ((int) d);
        }

        this.partyID = partyID;

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://aqueous-taiga-60305.herokuapp.com/party/checkcode").newBuilder();
        urlBuilder.addQueryParameter("partyid", partyID);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    String jsonData = response.body().string();
                    try {
                        JSONObject Jobject = new JSONObject(jsonData);
                        boolean exists = Jobject.getBoolean("id_exists");
                        //Log.i("creatingPartyLogic", "HEEEEEERRRREEEEEEE");
                        if(exists){
                            createUniquePartyID();
                        }
                    }
                    catch (JSONException e) {

                    }
                }
            }
        });

        mSharedPreferenceHelper.saveCurrentPartyId(this.partyID);
        mCurrentParty.setPartyId(this.partyID);
    }

    private void createSpotifyPlaylist() {
        HashMap<String, Object> playlistParams = new HashMap<>();
        playlistParams.put("name", mCurrentParty.name);
        playlistParams.put("public", true);

        mSpotifyService.createPlaylist(mCurrentParty.hostId, playlistParams, new Callback<Playlist>() {
            @Override
            public void success(Playlist playlist, Response response) {
                Log.d(TAG, "Playlist Created successfully: " + playlist.name);

                mSharedPreferenceHelper.saveCurrentSpotifyPlayListId(playlist.id);

                mCurrentParty.setPlaylistId(playlist.id);

                savePartyToDatabase();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "Playlist Creation Failed.");
            }
        });
    }

    private void savePartyToDatabase() {

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://aqueous-taiga-60305.herokuapp.com/party/create").newBuilder();
        String url = urlBuilder.build().toString();

        JsonObject json = new JsonObject();
        json.addProperty("name", mCurrentParty.name);
        json.addProperty("playlist_id", mCurrentParty.playlistId);
        json.addProperty("host_spotify_id", mCurrentParty.hostId);
        json.addProperty("party_id", mCurrentParty.partyId);
        json.addProperty("access_token", mCurrentParty.spotifyAccessToken);
        json.addProperty("guest_song_add_state", mCurrentParty.guestSongAddState);
        json.addProperty("down_vote_state", mCurrentParty.downVoteState);

        String jsonString = json.toString();
        RequestBody body = RequestBody.create(JSON, jsonString);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLoginScreenActions.showHostPlayerScreen();
                        }
                    });
                }
            }
        });
    }

    private void runOnUiThread(Runnable task) {
        new Handler(Looper.getMainLooper()).post(task);
    }





//    public void onSaveParty(String partyName, ArrayList<Song> trackList) {
//        if (partyName.trim().length() == 0) {
//            //mLoginScreenActions.showError("Party Name Invalid");
//        } else {
//            mCurrentParty = new Party(partyName, trackList);
//
//            createUniquePartyID();
//            mCurrentParty.setPartyId(partyID);
//
//            createSpotifyPlaylist();
//        }
//    }



//    private void getHostId() {
//        mSpotifyService.getMe(new Callback<UserPrivate>() {
//            @Override
//            public void success(UserPrivate userPrivate, Response response) {
//                Log.d(TAG, "Obtained User Information.");
//
//                mCurrentParty.setHostId(userPrivate.id);
//
//                createSpotifyPlaylist();
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.d(TAG, "FAILED to Obtain User Information.");
//            }
//        });
//    }


//
//    private void addTracksToSpotifyPlaylist() {
//
//        StringBuffer tracksParams = new StringBuffer();
//
//        for (Song track : mCurrentParty.trackList) {
//            tracksParams.append(track.songID).append(",");
//        }
//
//        HashMap parametersMap = new HashMap();
//        parametersMap.put("uris", tracksParams.toString());
//
//
//        mSpotifyService.addTracksToPlaylist(
//                mCurrentParty.hostId,
//                mCurrentParty.playlistId,
//                parametersMap,
//                new HashMap<String, Object>(),
//                new Callback<Pager<PlaylistTrack>>() {
//                    @Override
//                    public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
//                        savePartyToDatabase(mCurrentParty);
//                    }
//
//                    @Override
//                    public void failure(RetrofitError error) {
//                        Log.d(TAG, "Adding Tracks to Playlist Failed.");
//                    }
//                });
//    }

//    public void onAddTrackButtonPressed() {
//        // mLoginScreenActions.showSearchTrackScreen();
//    }
}
