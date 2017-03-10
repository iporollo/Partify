package ip.partyplaylist.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import ip.partyplaylist.R;
import ip.partyplaylist.model.Song;


public class JoinedPartyTracksAdapter extends BaseAdapter {

    private final Context mContext;
    private boolean mIsCurrentPartyOwnedByCurrentUser;
    private List<Song> mTracks;
    private Map<Song, Boolean> mTrackMap;

    public JoinedPartyTracksAdapter(List<Song> tracks, Context context,
                                    boolean isCurrentPartyOwnedByCurrentUser) {
        mContext = context;
        mTracks = tracks;
        mIsCurrentPartyOwnedByCurrentUser = isCurrentPartyOwnedByCurrentUser;
    }

    public JoinedPartyTracksAdapter(List<Song> tracks, Context context) {
        mContext = context;
        mTracks = tracks;
    }

    public JoinedPartyTracksAdapter(Map<Song,Boolean> tracksMap, List<Song> tracksList, Context context) {
        mContext = context;
        mTrackMap = tracksMap;
        mTracks = tracksList;

    }


    @Override
    public int getCount() {
        return mTracks.size();
    }

    @Override
    public Song getItem(int position) {
        return mTracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if(convertView==null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.track_list_item_layout, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.trackNameTV = (TextView) convertView.findViewById(R.id.track_name);
            viewHolder.trackArtistTV = (TextView) convertView.findViewById(R.id.track_artist);
            viewHolder.upvoteButton = (ImageButton) convertView.findViewById(R.id.upvoteButton);
            viewHolder.downvoteButton = (ImageButton) convertView.findViewById(R.id.downvoteButton);
            viewHolder.votingButtons = (LinearLayout) convertView.findViewById(R.id.linearVotingButtonLayout);

            convertView.setTag(viewHolder);

        }else{

            viewHolder = (ViewHolderItem) convertView.getTag();
        }


        viewHolder.votingButtons.setVisibility(View.VISIBLE);
        Song track = mTracks.get(position);

        if(track != null) {
            viewHolder.trackNameTV.setText(track.songName);
            viewHolder.trackArtistTV.setText(track.songArtistName);

        }

        return convertView;
    }


    static class ViewHolderItem {
        TextView trackNameTV;
        TextView trackArtistTV;
        ImageButton upvoteButton;
        ImageButton downvoteButton;
        LinearLayout votingButtons;

    }
}
