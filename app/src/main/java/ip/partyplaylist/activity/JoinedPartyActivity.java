package ip.partyplaylist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.StringTokenizer;

import ip.partyplaylist.R;
import ip.partyplaylist.adapter.JoinedPartyTracksAdapter;
import ip.partyplaylist.adapter.PartifyTracksAdapter;
import ip.partyplaylist.controllers.JoinedPartyController;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.model.Song;
import ip.partyplaylist.screen_actions.JoinedPartyScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;

/**
 * Created by Ivan on 1/28/2017.
 */

public class JoinedPartyActivity extends AppCompatActivity implements JoinedPartyScreenActions {

    public static final int SEARCH_SONG_REQUEST_CODE = 1;
    private JoinedPartyController mJoinedPartyController;
    private ArrayList<Song> mTrackList;
    private ListView mTrackListView;


    private JoinedPartyTracksAdapter mJoinedPartyTracksAdapter;


    private Button mAddButton;
    private TextView songTitle, songArtist, mPlaylistName;
    private ImageView albumCover, mSwipeUpBarSongStateIcon;

    private SlidingUpPanelLayout mSwipeUpPanel;
    private ImageView mSwipeUpBarSongArtImage;
    private TextView mSwipeUpBarTitle, mSwipeUpBarDetail, mPlayerTimeForward, tmpPartyIdHolder;
    private ProgressBar mProgressBar;

    private String mCurrentPartyID;
    private Party mCurrentParty;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_details);
        mCurrentPartyID = getIntent().getStringExtra("ENTERED_PARTYID");

        mJoinedPartyController = new JoinedPartyController(this);
        getCurrentPartyFirebase(mCurrentPartyID);

        //intialize vars

        mTrackListView=(ListView)findViewById(R.id.trackListJoinedParty);

        mPlaylistName = (TextView) findViewById(R.id.playlistNameJoinedParty);
        mAddButton = (Button) findViewById(R.id.btnAddSongJoinedParty);
        songTitle = (TextView) findViewById(R.id.txtSongTitleJoinedParty);
        songArtist = (TextView) findViewById(R.id.txtSongArtistJoinedParty);
        tmpPartyIdHolder = (TextView) findViewById(R.id.tmpPartyIdTxtJoinedParty);

        mSwipeUpPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout_joined_party);
        mSwipeUpBarSongStateIcon = (ImageView) findViewById(R.id.swipeUpBarSongStateImageJoinedParty);
        mSwipeUpBarSongArtImage = (ImageView) findViewById(R.id.swipeUpBarSongArtImageJoinedParty);
        mSwipeUpBarTitle = (TextView) findViewById(R.id.txtSwipeUpBarTitleJoinedParty);
        mSwipeUpBarDetail = (TextView) findViewById(R.id.txtSwipeUpBarDetailJoinedParty);
        //mPlayerTimeForward = (TextView) findViewById(R.id.);


        //hide the swipe up bar at first
        mSwipeUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        //add song button clicked action
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mJoinedPartyController.onAddTrackButtonPressed();
            }
        });

    }

    public void getCurrentPartyFirebase(final String currentPartyID) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("parties");

        myRef.child(currentPartyID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCurrentParty = dataSnapshot.getValue(Party.class);
                mJoinedPartyController.populateSharedPreferences(mCurrentParty);
                setUpView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("joinedPartyController", "failed to pull from database");

            }
        });

    }

    public void setUpView(){
        mPlaylistName.setText(mCurrentParty.name);
        tmpPartyIdHolder.setText(mCurrentPartyID);
        mJoinedPartyTracksAdapter = new JoinedPartyTracksAdapter(mCurrentParty.trackList, this);
        mTrackListView.setAdapter(mJoinedPartyTracksAdapter);
    }

    //Add song functionality
    @Override
    public void showSearchTrackScreen() {
        Intent startSearchSongActivity = new Intent(this, SearchTrackActivity.class);
        startActivityForResult(startSearchSongActivity, JoinedPartyActivity.SEARCH_SONG_REQUEST_CODE); // activates the method below
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JoinedPartyActivity.SEARCH_SONG_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Song trackToAdd = (Song) data.getExtras().get(SearchTrackActivity.TRACK);
                boolean isAlreadyInList = false;

                for(int i = 0; i <mTrackList.size(); i++){
                    if(mTrackList.get(i).songID.equals(trackToAdd.songID)){
                        isAlreadyInList = true;
                    }
                }

                if(!isAlreadyInList){
                    mTrackList.add(trackToAdd);
                    mJoinedPartyTracksAdapter.notifyDataSetChanged();

                    mJoinedPartyController.updateCurrentFirebaseParty(mCurrentParty);
                    Toast.makeText(this, "Song Added!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Already in playlist!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}