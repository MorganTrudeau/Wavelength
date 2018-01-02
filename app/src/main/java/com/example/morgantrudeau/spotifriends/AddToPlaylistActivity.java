package com.example.morgantrudeau.spotifriends;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.AddTrackToPlaylistRequest;
import com.wrapper.spotify.methods.PlaylistTracksRequest;
import com.wrapper.spotify.methods.UserPlaylistsRequest;
import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.PlaylistTrack;
import com.wrapper.spotify.models.SimplePlaylist;
import com.wrapper.spotify.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddToPlaylistActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private String m_selectedSongId;
    private String m_selectedPlaylist;
    private String m_loggedInUserId = new UserManager().getId();

    private ArrayList<PlaylistItem> m_playlists = new ArrayList<PlaylistItem>();

    private ApiManager m_apiManager = new ApiManager();
    private Api m_api = m_apiManager.getApi();

    private final String SELECTED_SONG_ID = "selected_song_id";

    private ListView m_addToPlaylistListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_playlist);
        m_selectedSongId = getIntent().getStringExtra(SELECTED_SONG_ID);
        m_addToPlaylistListView = (ListView) findViewById(R.id.addToPlaylistListView);

        new GetPlaylists().execute();
    }

    private class GetPlaylists extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean success = false;
            final UserPlaylistsRequest request = m_api.getPlaylistsForUser(m_loggedInUserId).build();

            try {
                final Page<SimplePlaylist> playlistsPage = request.get();
                String name;
                String id;

                for (SimplePlaylist playlist : playlistsPage.getItems()) {
                    name = playlist.getName();
                    id = playlist.getId();

                    m_playlists.add(new PlaylistItem(id, name));
                    success = true;
                }

            } catch (Exception e) {
                System.out.println("Something went wrong!" + e.getMessage());
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            updatePlaylists();
        }
    }

    private void updatePlaylists() {
        ArrayAdapter<PlaylistItem> m_playlistsAdapter =
                new ArrayAdapter<PlaylistItem>(this, android.R.layout.simple_list_item_1, m_playlists);
        m_addToPlaylistListView.setAdapter(m_playlistsAdapter);
        m_addToPlaylistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                m_selectedPlaylist = m_playlists.get(position).getId();
                showPopup(view);
            }
        });
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(AddToPlaylistActivity.this, v);
        popup.setOnMenuItemClickListener(AddToPlaylistActivity.this);
        popup.inflate(R.menu.add_to_playlist_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case R.id.add_to_playlist:
                new AddToPlaylist().execute();
                return true;
            default:
                return false;
        }
    }

    private class AddToPlaylist extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            final List<String> tracksToAdd = Arrays.asList("spotify:track:" + m_selectedSongId);

            // Index starts at 0
            final int insertIndex = 0;

            final AddTrackToPlaylistRequest request = m_api.addTracksToPlaylist(m_loggedInUserId, m_selectedPlaylist, tracksToAdd)
                    .position(insertIndex)
                    .build();

            try {
                request.get();
            } catch (Exception e) {
                System.out.println("Something went wrong!" + e.getMessage());
            }
            return null;
        }
    }

}
