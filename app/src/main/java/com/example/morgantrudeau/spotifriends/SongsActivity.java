package com.example.morgantrudeau.spotifriends;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.PlaylistTracksRequest;
import com.wrapper.spotify.methods.UserPlaylistsRequest;
import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.PlaylistTrack;
import com.wrapper.spotify.models.SimplePlaylist;

import java.util.ArrayList;
import java.util.List;

public class SongsActivity extends AppCompatActivity {

    private ApiManager m_apiManager = new ApiManager();
    private Api m_api = m_apiManager.getApi();

    private ListView m_songsListView;

    private ArrayList<String> m_songsList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);

        m_songsListView = (ListView) findViewById(R.id.songsListView);

        new GetSongs().execute();
    }

    private class GetSongs extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean success = false;
            Intent intent = getIntent();
            String playlistId = intent.getStringExtra("playlistId");
            String currentUser = intent.getStringExtra("currentUser");
            final PlaylistTracksRequest request = m_api.getPlaylistTracks(currentUser, playlistId).build();
            try {
                final Page<PlaylistTrack> page = request.get();

                final List<PlaylistTrack> playlistTracks = page.getItems();

                for (PlaylistTrack playlistTrack : playlistTracks) {
                    m_songsList.add(playlistTrack.getTrack().getName());
                }
                success = true;

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
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, m_songsList);
        m_songsListView.setAdapter(m_playlistsAdapter);
        final Intent intent = new Intent(this, SongsActivity.class);
        m_songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
            }
        });
    }

}
