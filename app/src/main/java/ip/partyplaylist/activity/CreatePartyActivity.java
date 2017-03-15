package ip.partyplaylist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import ip.partyplaylist.R;
import ip.partyplaylist.controllers.CreatePartyController;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.screen_actions.CreatePartyScreenActions;

public class CreatePartyActivity extends AppCompatActivity implements CreatePartyScreenActions {

    public static final int SEARCH_SONG_REQUEST_CODE = 1;
    private CreatePartyController mCreatePartyController;
    private EditText mPartyNameEditText;
    private TextView mTxtGuestSongAddSetting, mTxtDownVoteSetting;
    private Switch mSwitchGuestSongAddSetting, mSwitchDownVoteSetting;
    private boolean mCurrentSwitchGuestSongAddState, mCurrentSwitchDownVoteState;


//    private ListView mPartyTrackList;
//    private PartifyTracksAdapter mTracksAdapter;
//    private ArrayList<Song> mCurrentTrackList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_party);

        this.setTitle("Create Party");

        mCreatePartyController = new CreatePartyController(this);

        mPartyNameEditText = (EditText)  findViewById(R.id.party_name_edit_text);

        mTxtGuestSongAddSetting = (TextView) findViewById(R.id.txtGuestSongAddSetting);
        mSwitchGuestSongAddSetting = (Switch) findViewById(R.id.switchGuestSongAddSetting);

        mTxtDownVoteSetting = (TextView) findViewById(R.id.txtDownVoteSetting);
        mSwitchDownVoteSetting = (Switch) findViewById(R.id.switchDownVoteSetting);

        mSwitchGuestSongAddSetting.setChecked(true);
        mSwitchDownVoteSetting.setChecked(true);

        mSwitchGuestSongAddSetting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    mTxtGuestSongAddSetting.setText("Guests add songs enabled");
                    mCurrentSwitchGuestSongAddState = true;
                }else{
                    mTxtGuestSongAddSetting.setText("Guests add songs disabled");
                    mCurrentSwitchGuestSongAddState = false;
                }

            }
        });


        mSwitchDownVoteSetting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    mTxtDownVoteSetting.setText("Down-vote enabled");
                    mCurrentSwitchDownVoteState = true;
                }else{
                    mTxtDownVoteSetting.setText("Down-vote disabled");
                    mCurrentSwitchDownVoteState = false;
                }

            }
        });


        //check the current guest add song state before we display the screen
        if(mSwitchGuestSongAddSetting.isChecked()){
            mTxtGuestSongAddSetting.setText("Guests add songs enabled");
            mCurrentSwitchGuestSongAddState = true;
        }
        else {
            mTxtGuestSongAddSetting.setText("Guests add songs disabled");
            mCurrentSwitchGuestSongAddState = false;
        }

        //check the current downvote state before we display the screen
        if(mSwitchDownVoteSetting.isChecked()){
            mTxtDownVoteSetting.setText("Down-vote enabled");
            mCurrentSwitchDownVoteState = true;
        }
        else {
            mTxtDownVoteSetting.setText("Down-vote disabled");
            mCurrentSwitchDownVoteState = false;
        }

    //mTracksAdapter = new PartifyTracksAdapter(mCurrentTrackList, this);
//        mPartyTrackList = (ListView) findViewById(R.id.party_track_list); //playlist view main -- maybe needs changing
//
//
//        Button mAddTrackButton = (Button) findViewById(R.id.add_track_button);
//        mAddTrackButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCreatePartyController.onAddTrackButtonPressed();
//            }
//        });
    }

     //Action bar Create button
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.menu_create_party, menu);
         return true;
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
             case R.id.btnCreateParty:
                 mCreatePartyController.onCreateParty(mPartyNameEditText.getText().toString(),mCurrentSwitchGuestSongAddState, mCurrentSwitchDownVoteState);

                 //mCreatePartyController.onSaveParty(mPartyNameEditText.getText().toString(), mCurrentTrackList);
                 return(true);
         }
         return(super.onOptionsItemSelected(item));
     }


    @Override
    public void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPartyCreatedScreen(Party createdParty) {
        ///show unique code number
        //Toast.makeText(this, "Your party ID: " + mCreatePartyController.getPartyID(), Toast.LENGTH_LONG).show();
        //Toast.makeText(this, "Party Created!", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(CreatePartyActivity.this, LoginActivity.class);
        i.putExtra("CREATED_PARTY", createdParty);
        startActivity(i);
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mPartyTrackList.setAdapter(mTracksAdapter);
//    }
//
//    protected void onStart() {
//        super.onStart();
//    }
//
//    protected void onStop() {
//        super.onStop();
//    }
//
//    @Override
//    public void showSearchTrackScreen() {
//        Intent startSearchSongActivity = new Intent(this, SearchTrackActivity.class);
//        startActivityForResult(startSearchSongActivity, SEARCH_SONG_REQUEST_CODE);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == SEARCH_SONG_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//
//                Song trackToAdd = (Song) data.getExtras().get(SearchTrackActivity.TRACK);
//
//                boolean isAlreadyInList = false;
//
//                for(int i = 0; i <mCurrentTrackList.size(); i++){
//                    if(mCurrentTrackList.get(i).songID.equals(trackToAdd.songID)){
//                        isAlreadyInList = true;
//                    }
//                }
//                if(!isAlreadyInList) {
//                    mCurrentTrackList.add(trackToAdd);
//                }
//                else{
//                    Toast.makeText(this, "Already in playlist!", Toast.LENGTH_SHORT).show();
//                }
//                mTracksAdapter = new PartifyTracksAdapter(mCurrentTrackList, CreatePartyActivity.this);
//                mPartyTrackList.setAdapter(mTracksAdapter);
//                mPartyTrackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        mCurrentTrackList.remove(position);
//                        mTracksAdapter = new PartifyTracksAdapter(mCurrentTrackList, CreatePartyActivity.this);
//                        mPartyTrackList.setAdapter(mTracksAdapter);
//                    }
//
//                });
//
//            }
//        }
//    }
}
