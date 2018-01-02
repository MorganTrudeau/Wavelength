package com.example.morgantrudeau.spotifriends;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.UserPlaylistsRequest;
import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.SimplePlaylist;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {

    private TextView m_TextMessage;
    private ListView m_playlistsListView;

    private ApiManager m_apiManager = new ApiManager();
    private Api m_api = m_apiManager.getApi();

    //private String m_currentUserDisplayName = null;
    private String m_currentUserId = "";
    private ArrayList<PlaylistItem> m_playlistItems = new ArrayList<>();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    m_TextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    m_TextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    m_TextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        m_TextMessage = (TextView) findViewById(R.id.message);
        m_playlistsListView = (ListView) findViewById(R.id.playlistsListView);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        new GetPlaylists().execute();
    }

    private class GetPlaylists extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean success = false;
            m_currentUserId = getIntent().getStringExtra("current_user_id");
            final UserPlaylistsRequest request = m_api.getPlaylistsForUser(m_currentUserId).build();

            try {
                final Page<SimplePlaylist> playlistsPage = request.get();
                String name;
                String id;

                for (SimplePlaylist playlist : playlistsPage.getItems()) {
                    name = playlist.getName();
                    id = playlist.getId();

                    m_playlistItems.add(new PlaylistItem(id, name));
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
                new ArrayAdapter<PlaylistItem>(this, android.R.layout.simple_list_item_1, m_playlistItems);
        m_playlistsListView.setAdapter(m_playlistsAdapter);
        final Intent intent = new Intent(this, SongsActivity.class);
        m_playlistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {

                PlaylistItem selectedPlaylist =  m_playlistItems.get(position);
                intent.putExtra("playlistId", selectedPlaylist.getId());
                intent.putExtra("currentUser", m_currentUserId);
                startActivity(intent);
            }
        });
    }

}
