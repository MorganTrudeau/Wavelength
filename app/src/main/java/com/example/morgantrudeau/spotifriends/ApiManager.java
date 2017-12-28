package com.example.morgantrudeau.spotifriends;

import com.wrapper.spotify.Api;
import com.wrapper.spotify.models.User;

/**
 * Created by morgantrudeau on 2017-12-18.
 */

public class ApiManager {

    private static final String CLIENT_ID = "429dbba126154c6a87c6730ffa296044";
    private static final String REDIRECT_URI = "testschema://callback";
    private static final  String CLIENT_SECRET = "c727dbba1a95455caa36886714b85dad";

    private static Api api = Api.builder()
            .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectURI(REDIRECT_URI)
                .build();

    private String code;

    public Api getApi() {
         return api;
    }

    public void setApi(Api newApi) {
        api = newApi;
    }
}
