package ip.partyplaylist.util;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.concurrent.TimeUnit;


public class SharedPreferenceHelper {

    private static final String PREFERENCES = "PartifyPreferences";

    private static final String CURRENT_SPOTIFY_TOKEN_KEY = "CurrentSpotifyToken";
    private static final String CURRENT_USER_SPOTIFY_ID_KEY = "CurrentSpotifyID";
    private static final String CURRENT_PLAYLIST_ID_KEY = "CurrentPlaylistID";
    private static final String CURRENT_PARTY_ID_KEY = "CurrentPartyID";
    private static final String CURRENT_PLAYLIST_NAME = "CurrentPlaylistName";
    private static final String EXPIRES_AT = "expires_at";


    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public SharedPreferenceHelper(Context context) {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public void saveSpotifyToken(String token) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(CURRENT_SPOTIFY_TOKEN_KEY, token);
        editor.apply();
    }

    public void saveCurrentUserId(String id) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(CURRENT_USER_SPOTIFY_ID_KEY, id);
        editor.apply();
    }

    public void saveCurrentPlayListName(String name) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(CURRENT_PLAYLIST_NAME, name);
        editor.apply();
    }

    public void saveCurrentPlayListId(String id) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(CURRENT_PLAYLIST_ID_KEY, id);
        editor.apply();
    }

    public void saveCurrentPartyId(String id) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(CURRENT_PARTY_ID_KEY, id);
        editor.apply();
    }

    public String getCurrentSpotifyToken() {
        return mSharedPreferences.getString(CURRENT_SPOTIFY_TOKEN_KEY, "");
    }

    public String getCurrentUserId() {
        return mSharedPreferences.getString(CURRENT_USER_SPOTIFY_ID_KEY, "");
    }

    public String getCurrentPlaylistId() {
        return mSharedPreferences.getString(CURRENT_PLAYLIST_ID_KEY, "");
    }

    public String getCurrentPlaylistName() {
        return mSharedPreferences.getString(CURRENT_PLAYLIST_NAME, "");
    }

    public String getCurrentPartyId() {
        return mSharedPreferences.getString(CURRENT_PARTY_ID_KEY, "");
    }

}
