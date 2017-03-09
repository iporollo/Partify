package ip.partyplaylist.controllers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ip.partyplaylist.screen_actions.LoginScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;
import ip.partyplaylist.util.SpotifyScope;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginActivityController {

    private static final String TAG = LoginActivityController.class.getSimpleName();

    private static final String CLIENT_ID = "ea09225ef2974242a1549f3812a15496";
    private static final String REDIRECT_URI = "https://aqueous-taiga-60305.herokuapp.com/spotify/callback";
    public static final int REQUEST_CODE = 1;
    private OkHttpClient client = new OkHttpClient();

    private final SharedPreferenceHelper mSharedPreferenceHelper;
    private Context mContext;


    public LoginActivityController(Context context) {
        mContext = context;
        mSharedPreferenceHelper = new SharedPreferenceHelper(context);
    }

    public void onLoginUserToSpotify() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.CODE,
                REDIRECT_URI);

        builder.setScopes(new String[]{
                SpotifyScope.PLAYLIST_MODIFY_PRIVATE,
                SpotifyScope.PLAYLIST_MODIFY_PUBLIC,
                SpotifyScope.STREAMING});

        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity((Activity) mContext, REQUEST_CODE, request);
    }

    public void onUserDataReceipt(String responseCode, String responseState){

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://aqueous-taiga-60305.herokuapp.com/spotify/callback").newBuilder();
        urlBuilder.addQueryParameter("code", responseCode);
        urlBuilder.addQueryParameter("state", responseState);
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
            public void onResponse(Call call, final okhttp3.Response noSpecificResponse) throws IOException {
                if (!noSpecificResponse.isSuccessful()) {
                    throw new IOException("Unexpected code " + noSpecificResponse);
                } else {
                    getRailsUserID();
                }
            }
        });

    }

    public void getRailsUserID(){

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://aqueous-taiga-60305.herokuapp.com/user/spotifyuserid").newBuilder();
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
            public void onResponse(Call call, final okhttp3.Response userID) throws IOException {
                if (!userID.isSuccessful()) {
                    throw new IOException("Unexpected code " + userID);
                } else {
                    String jsonData = userID.body().string();
                    try {
                        JSONObject Jobject = new JSONObject(jsonData);
                        String id = Jobject.getString("id");
                        getUserAccessToken(id);

                    }
                    catch (JSONException e) {

                    }

                }
            }
        });

    }

    public void getUserAccessToken(String id){

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://aqueous-taiga-60305.herokuapp.com/user/givetoken").newBuilder();
        urlBuilder.addQueryParameter("user", id);
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
                        String token = Jobject.getString("token");
                        onUserLoggedInSuccessfully(token);
                    }
                    catch (JSONException e) {

                    }
                }
            }
        });
    }

    public void onUserLoggedInSuccessfully(final String accessToken) {
        mSharedPreferenceHelper.saveSpotifyToken(accessToken);

        SpotifyApi mSpotifyApi = new SpotifyApi();
        mSpotifyApi.setAccessToken(accessToken);
        SpotifyService spotifyService = mSpotifyApi.getService();

        spotifyService.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                Log.d(TAG, "Obtained User Information.");

                mSharedPreferenceHelper.saveCurrentUserId(userPrivate.id);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "FAILED to Obtain User Information.");
            }
        });

        ((LoginScreenActions) mContext).showCreatePartyScreen();
    }

}
