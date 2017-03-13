package ip.partyplaylist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;

import ip.partyplaylist.R;
import ip.partyplaylist.controllers.LoginActivityController;
import ip.partyplaylist.controllers.PartySavingLogicController;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.screen_actions.LoginScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;

public class LoginActivity extends AppCompatActivity implements
        ConnectionStateCallback, LoginScreenActions{

    private LoginActivityController mLoginActivityController;
    private PartySavingLogicController mSavePartyContoller;
    private SharedPreferenceHelper mSharedPreferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //idk if i need this
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mLoginActivityController = new LoginActivityController(this);
        mSavePartyContoller = new PartySavingLogicController(this);
        mSharedPreferenceHelper = new SharedPreferenceHelper(this);


        mLoginActivityController.onLoginUserToSpotify();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == LoginActivityController.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.CODE) {
                mLoginActivityController.onUserDataReceipt(response.getCode(), response.getState());
                Party party = (Party) getIntent().getSerializableExtra("CREATED_PARTY");
                mSavePartyContoller.saveParty(party);
                //showHostPlayerScreen();
            }
        }
    }

    @Override
    public void showHostPlayerScreen() {
        Toast.makeText(this, "Your party ID: " + mSharedPreferenceHelper.getCurrentPartyId(), Toast.LENGTH_LONG).show();

        Intent hostPlayer = new Intent(this, HostPlayerActivity.class);
        startActivity(hostPlayer);
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
}
