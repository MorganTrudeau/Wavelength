package com.example.morgantrudeau.spotifriends;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.AddToMySavedTracksRequest;
import com.wrapper.spotify.methods.PlaylistTracksRequest;
import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.PlaylistTrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SongsActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private ApiManager m_apiManager = new ApiManager();
    private Api m_api = m_apiManager.getApi();

    private Player m_player;

    private ListView m_songsListView;

    private ArrayList<Song> m_songsList = new ArrayList<Song>();
    private String m_selectedSongId;

    private final String SELECTED_SONG_ID = "selected_song_id";

    private final Player.OperationCallback m_operationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Log.e("Success", "Success");
        }

        @Override
        public void onError(Error error) {
            Log.e("Error", "Error" + error);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);

        m_songsListView = (ListView) findViewById(R.id.songsListView);

        new GetSongs().execute();

        Config playerConfig = new Config(this, m_apiManager.getAccessToken(), m_apiManager.getClientId());
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                m_player = spotifyPlayer;
//                m_player.addConnectionStateCallback(SongsActivity.this);
//                m_player.addNotificationCallback(SongsActivity.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_player.pause(m_operationCallback);
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
                String name;
                String id;
                for (PlaylistTrack playlistTrack : playlistTracks) {
                    name = playlistTrack.getTrack().getName();
                    id = playlistTrack.getTrack().getId();
                    m_songsList.add(new Song(id, name));
                }
                success = true;

            } catch (Exception e) {
                System.out.println("Something went wrong!" + e.getMessage());
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            updateSongs();
        }
    }

    private void updateSongs() {
        ArrayAdapter<Song> m_playlistsAdapter =
                new ArrayAdapter<Song>(this, android.R.layout.simple_list_item_1, m_songsList);
        m_songsListView.setAdapter(m_playlistsAdapter);
        m_songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                m_selectedSongId = m_songsList.get(position).getId();
                showPopup(view);
            }
        });
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(SongsActivity.this, v);
        popup.setOnMenuItemClickListener(SongsActivity.this);
        popup.inflate(R.menu.song_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case R.id.play:
                m_player.playUri(null, "spotify:track:" + m_selectedSongId, 0, 0);
                return true;
            case R.id.add_to_playlist:
                final Intent intent = new Intent(this, AddToPlaylistActivity.class);
                intent.putExtra(SELECTED_SONG_ID, m_selectedSongId);
                startActivity(intent);
                return true;
            case R.id.save:
                new Save().execute();
            default:
                return false;
        }
    }

    private class Save extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Api api = Api.builder().accessToken(m_apiManager.getAccessToken()).build();

            List<String> tracksToAdd = Arrays.asList(m_selectedSongId);

            AddToMySavedTracksRequest request = api.addToMySavedTracks(tracksToAdd).build();

            try {
                // Add tracks synchronously
                request.get();
                System.out.println("Added tracks to the user's Your Music library!");
            } catch (Exception e) {
                System.out.println("Something went wrong!" + e.getMessage());
            }
            return null;
        }
    }
}
