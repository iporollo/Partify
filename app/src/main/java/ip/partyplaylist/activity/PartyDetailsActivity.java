package ip.partyplaylist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ip.partyplaylist.R;
import ip.partyplaylist.adapter.PartifyTracksAdapter;
import ip.partyplaylist.controllers.PartyDetailsController;
import ip.partyplaylist.model.PartifyTrack;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.screen_actions.PartyDetailsScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;

public class PartyDetailsActivity extends AppCompatActivity implements PartyDetailsScreenActions {

    private ArrayList<PartifyTrack> mPartyTrackList;
    private ListView mPartyTrackListView;
    private PartifyTracksAdapter mTracksAdapter;
    private PartyDetailsController mPartyDetailsController;
    private boolean mIsCurrentPartyOwnedByCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_details);

        SharedPreferenceHelper mSharedPreferenceHelper = new SharedPreferenceHelper(this);


        Bundle intentExtras = getIntent().getExtras();

        Party mCurrentParty = (Party) intentExtras.get(SearchPartyActivity.CURRENT_PARTY);

        mIsCurrentPartyOwnedByCurrentUser =
                mCurrentParty.hostId.equals(mSharedPreferenceHelper.getCurrentUserId());

        mPartyTrackList = mCurrentParty.trackList;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mCurrentParty.name);
        }

        mPartyDetailsController = new PartyDetailsController(this, mCurrentParty);

        Button mAddTrackButton = (Button) findViewById(R.id.add_track_button);
        mAddTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPartyDetailsController.onUserClicksAddTrack();
            }
        });

        mAddTrackButton.setVisibility(
                mIsCurrentPartyOwnedByCurrentUser ?
                        View.GONE :
                        View.VISIBLE);

        mPartyTrackListView = (ListView) findViewById(R.id.party_track_list);
        mTracksAdapter = new PartifyTracksAdapter(mPartyTrackList, this, mIsCurrentPartyOwnedByCurrentUser);
        mPartyTrackListView.setAdapter(mTracksAdapter);
        mPartyTrackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIsCurrentPartyOwnedByCurrentUser) {
                    mPartyDetailsController.onHostClicksTrack(mTracksAdapter.getItem(position));
                    removeElementFromTracklist(position);
                    Toast.makeText(PartyDetailsActivity.this, "Track Added!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void removeElementFromTracklist(int position) {
        mPartyTrackList.remove(position);
        mTracksAdapter = new PartifyTracksAdapter(mPartyTrackList, PartyDetailsActivity.this, mIsCurrentPartyOwnedByCurrentUser);
        mPartyTrackListView.setAdapter(mTracksAdapter);
    }

    @Override
    public void showSearchTrackScreen() {
        Intent startSearchSongActivity = new Intent(this, SearchTrackActivity.class);
        startActivityForResult(startSearchSongActivity, CreatePartyActivity.SEARCH_SONG_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CreatePartyActivity.SEARCH_SONG_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PartifyTrack trackToAdd = (PartifyTrack) data.getExtras().get(SearchTrackActivity.TRACK);
                mPartyDetailsController.onUserAddedTrack(trackToAdd);
            }
        }
    }

    @Override
    public void updateCurrentTrackList(Party mCurrentParty) {
        mTracksAdapter = new PartifyTracksAdapter(mCurrentParty.trackList, PartyDetailsActivity.this, mIsCurrentPartyOwnedByCurrentUser);
        mPartyTrackListView.setAdapter(mTracksAdapter);
    }
}
