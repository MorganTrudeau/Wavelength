package com.example.morgantrudeau.spotifriends;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.UserPlaylistsRequest;
import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.SimplePlaylist;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity {

    private TextView m_TextMessage;

    private ApiManager m_apiManager = new ApiManager();
    private Api m_api = m_apiManager.getApi();

    //private String m_currentUserDisplayName = null;
    private String m_currentUserId = "";
    private ArrayList<String> m_playlists = new ArrayList<>();

//    private ArrayAdapter<String> m_playlistsAdapter =
//            new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, m_playlists);


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
        setContentView(R.layout.activity_user);

        m_TextMessage = (TextView) findViewById(R.id.message);
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

                for (SimplePlaylist playlist : playlistsPage.getItems()) {
                    m_playlists.add(playlist.getName());
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
        ArrayAdapter<String> m_playlistsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, m_playlists);
        ListView playlistsListView = findViewById(R.id.playlistsListView);
        playlistsListView.setAdapter(m_playlistsAdapter);
    }

}
