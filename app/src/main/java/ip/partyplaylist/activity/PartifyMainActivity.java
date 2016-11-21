package ip.partyplaylist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ip.partyplaylist.R;
import ip.partyplaylist.controllers.PartifyMainController;
import ip.partyplaylist.screen_actions.PartifyMainScreenActions;

public class PartifyMainActivity extends AppCompatActivity implements PartifyMainScreenActions {

    private Button mCreatePartyButton;
    private Button mSearchPartyButton;

    private PartifyMainController mPartifyMainController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partify_main);

        mPartifyMainController = new PartifyMainController(this);

        mCreatePartyButton = (Button) findViewById(R.id.create_party_button);
        mSearchPartyButton = (Button) findViewById(R.id.search_party_button);

        mCreatePartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPartifyMainController.onCreateParty();
            }
        });

        mSearchPartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPartifyMainController.onSearchParty();
            }
        });
    }

    @Override
    public void showCreatePartyScreen() {
        Intent showCreatePartyScreen = new Intent(this, CreatePartyActivity.class);
        startActivity(showCreatePartyScreen);
    }

    @Override
    public void showSearchPartyScreen() {
        Intent showSearchPartyScreen = new Intent(this, SearchPartyActivity.class);
        startActivity(showSearchPartyScreen);
    }
}
