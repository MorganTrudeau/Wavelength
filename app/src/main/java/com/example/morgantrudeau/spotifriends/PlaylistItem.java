package com.example.morgantrudeau.spotifriends;

/**
 * Created by morgantrudeau on 2018-01-02.
 */

public class PlaylistItem {
    private String id;
    private String  name;
    public PlaylistItem() {
        super();
    }

    public PlaylistItem(String id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
