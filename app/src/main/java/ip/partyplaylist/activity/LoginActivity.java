package ip.partyplaylist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;

import ip.partyplaylist.R;
import ip.partyplaylist.controllers.LoginActivityController;
import ip.partyplaylist.screen_actions.LoginScreenActions;

public class LoginActivity extends AppCompatActivity implements
        ConnectionStateCallback,
        LoginScreenActions {

    private LoginActivityController mLoginActivityController;

    private Button mHostButton;
    private Button mJoinButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mLoginActivityController = new LoginActivityController(this);

        mHostButton = (Button) findViewById(R.id.host_button);
        mJoinButton = (Button) findViewById(R.id.join_button);

        mHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginActivityController.onLoginUserToSpotify();

                //mHostButton.setVisibility(View.GONE);
            }
        });

        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //function when Join button is clicked
                //Join activity controller needs to be implented

                //mJoinButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

//        mHostButton.setVisibility(View.VISIBLE);
//        mJoinButton.setVisibility(View.VISIBLE);

        mLoginActivityController.onCheckIfUserIsLoggedIn();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == LoginActivityController.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {

                String accessToken = response.getAccessToken();
                mLoginActivityController.onUserLoggedInSuccessfully(accessToken);

                //idk if this goes in the controller or here - implementing player
               // Config playerConfig = new Config(this, accessToken, mLoginActivityController.CLIENT_ID);

            }
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("LoginActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("LoginActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error var1) {
        Log.d("LoginActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("LoginActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("LoginActivity", "Received connection message: " + message);
    }

    @Override
    public void showMainScreen() {
        Intent loadMainPartifyScreen = new Intent(this, CreatePartyActivity.class);
        startActivity(loadMainPartifyScreen);
        finish();
    }



    // shows join party screen
//    @Override
//    public void showJoinScreen() {
//        Intent loadMainPartifyScreen = new Intent(this, CreatePartyActivity.class);
//        startActivity(loadMainPartifyScreen);
//        finish();
//    }
}
