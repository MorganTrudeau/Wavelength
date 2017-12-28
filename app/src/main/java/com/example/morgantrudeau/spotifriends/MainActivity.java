package com.example.morgantrudeau.spotifriends;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.UserRequest;
import com.wrapper.spotify.models.AuthorizationCodeCredentials;
import com.wrapper.spotify.models.User;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{

    private static final String CURRENT_USER_ID = "current_user_id";
    private static final String CURRENT_USER_DISPLAY_NAME = "current_user_display_name";

    private TextView m_userNameTextView;
    private ImageView m_userImageView;
    private EditText m_userNameEditText;
    private LinearLayout m_userCell;

    private ApiManager m_apiManager = new ApiManager();
    private final Api m_api = m_apiManager.getApi();

    private User m_currentUser;
    private String m_currentUserDisplayName = null;

    private String m_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpUI();

         /* Set the necessary scopes that the application will need from the user */
        final List<String> scopes = Arrays.asList("playlist-modify-private", "playlist-modify-public", "playlist-read-private", "playlist-read-collaborative", "user-read-currently-playing", "user-read-recently-played");

        /* Set a state. This is used to prevent cross site request forgeries. */
        final String state = "";

        String authorizeURL = m_api.createAuthorizeURL(scopes, state);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(authorizeURL));
        startActivity(i);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null) {
            AuthenticationResponse response = AuthenticationResponse.fromUri(uri);
            switch (response.getType()) {
                case CODE:
                    // Handle successful response
                    m_code = response.getCode();
                    new GetAccessTokens().execute();
                    break;
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    break;
                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;
                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    private class GetAccessTokens extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            /* Make a token request. Asynchronous requests are made with the .getAsync method and synchronous requests
            * are made with the .get method. This holds for all type of requests. */
            final SettableFuture<AuthorizationCodeCredentials> authorizationCodeCredentialsFuture = m_api.authorizationCodeGrant(m_code).build().getAsync();

                /* Add callbacks to handle success and failure */
                Futures.addCallback(authorizationCodeCredentialsFuture, new FutureCallback<AuthorizationCodeCredentials>() {
                @Override
                public void onSuccess(AuthorizationCodeCredentials authorizationCodeCredentials) {
                    /* The tokens were retrieved successfully! */
                    System.out.println("Successfully retrieved an access token! " + authorizationCodeCredentials.getAccessToken());
                    System.out.println("The access token expires in " + authorizationCodeCredentials.getExpiresIn() + " seconds");
                    System.out.println("Luckily, I can refresh it using this refresh token! " +     authorizationCodeCredentials.getRefreshToken());

                    /* Set the access token and refresh token so that they are used whenever needed */
                    m_api.setAccessToken(authorizationCodeCredentials.getAccessToken());
                    m_api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
                    m_apiManager.setApi(m_api);
                }

                @Override
                public void onFailure(Throwable throwable) {
                /* Let's say that the client id is invalid, or the code has been used more than once,
                 * the request will fail. Why it fails is written in the throwable's message. */
                    System.out.println("Error throwable");
                }
            });

            return null;
        }
    }

    private class GetUser extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean success = false;
            String userName = m_userNameEditText.getText().toString();

            final UserRequest request = m_api.getUser(userName).build();

            try {
                final User user = request.get();
                m_currentUser = user;
                success = true;
            } catch (Exception e) {
                System.out.println("Something went wrong!" + e.getMessage());
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                displayUser();
            } else {
                removeUser();
            }
        }
    }

    private void setUpUI() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        m_userNameTextView = (TextView) findViewById(R.id.userNameText);
        m_userImageView = (ImageView) findViewById(R.id.userImage);
        m_userNameEditText = (EditText) findViewById(R.id.editText);
        m_userCell = (LinearLayout) findViewById(R.id.userCell);
        setSupportActionBar(myToolbar);

        final Button button = findViewById(R.id.search);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new GetUser().execute();
            }
        });

    }

    public void onUserSelected(View view) {
        Intent intent =  new Intent(this, UserActivity.class);
        intent.putExtra(CURRENT_USER_ID, m_currentUser.getId());
        if (m_currentUserDisplayName != null) {
            intent.putExtra(CURRENT_USER_DISPLAY_NAME, m_currentUserDisplayName);
        }
        startActivity(intent);
    }

    public void displayUser() {
        m_userCell.setVisibility(View.VISIBLE);
        m_currentUserDisplayName = m_currentUser.getDisplayName();
        if (m_currentUserDisplayName != null) {
            m_userNameTextView.setText(m_currentUserDisplayName);
        } else {
            m_userNameTextView.setText(m_currentUser.getId());
        }
        m_userImageView.setImageResource(R.mipmap.ic_launcher);
    }

    public void removeUser() {
        m_userCell.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "UserManager logged in");

        //mPlayer.playUri(null, "spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "UserManager logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        super.onActivityResult(requestCode, resultCode, intent);
//
//        AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
//        // Check if result comes from the correct activity
//        if (requestCode == LOGIN_REQUEST_CODE) {
//            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
//                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
//                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
//                    @Override
//                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
//                        mPlayer = spotifyPlayer;
//                        mPlayer.addConnectionStateCallback(MainActivity.this);
//                        mPlayer.addNotificationCallback(MainActivity.this);
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
//                    }
//                });
//            }
//        }
//    }
}
