package ip.partyplaylist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ip.partyplaylist.R;
import ip.partyplaylist.adapter.PartifyTracksAdapter;
import ip.partyplaylist.controllers.CreatePartyController;
import ip.partyplaylist.model.PartifyTrack;
import ip.partyplaylist.screen_actions.CreatePartyScreenActions;

public class CreatePartyActivity extends AppCompatActivity implements
        CreatePartyScreenActions
         {

    public static final int SEARCH_SONG_REQUEST_CODE = 1;
    private CreatePartyController mCreatePartyController;
    private EditText mPartyNameEditText;
    private ListView mPartyTrackList;
    private PartifyTracksAdapter mTracksAdapter;
    private ArrayList<PartifyTrack> mCurrentTrackList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_party);

        mCreatePartyController = new CreatePartyController(this);

        mTracksAdapter = new PartifyTracksAdapter(mCurrentTrackList, this);

        mPartyNameEditText = (EditText) findViewById(R.id.party_name_edit_text);
        mPartyTrackList = (ListView) findViewById(R.id.party_track_list);

        Button mCreatePartyButton = (Button) findViewById(R.id.create_party);
        mCreatePartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreatePartyController.onSaveParty(mPartyNameEditText.getText().toString(), mCurrentTrackList);
            }
        });

        Button mAddTrackButton = (Button) findViewById(R.id.add_track_button);
        mAddTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreatePartyController.onAddTrackButtonPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPartyTrackList.setAdapter(mTracksAdapter);
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }

    @Override
    public void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPartyCreatedScreen() {
        Toast.makeText(this, "Party Created!", Toast.LENGTH_SHORT).show();
        //super.onBackPressed();
        // todo redirect to new gui
        Intent i = new Intent(CreatePartyActivity.this, PlaylistGUIActivity.class);
        startActivity(i);
    }

    @Override
    public void showSearchTrackScreen() {
        Intent startSearchSongActivity = new Intent(this, SearchTrackActivity.class);
        startActivityForResult(startSearchSongActivity, SEARCH_SONG_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_SONG_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                PartifyTrack trackToAdd = (PartifyTrack) data.getExtras().get(SearchTrackActivity.TRACK);
                mCurrentTrackList.add(trackToAdd);
                mTracksAdapter = new PartifyTracksAdapter(mCurrentTrackList, CreatePartyActivity.this);
                mPartyTrackList.setAdapter(mTracksAdapter);
                mPartyTrackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mCurrentTrackList.remove(position);
                        mTracksAdapter = new PartifyTracksAdapter(mCurrentTrackList, CreatePartyActivity.this);
                        mPartyTrackList.setAdapter(mTracksAdapter);
                    }

                });

            }
        }
    }
}