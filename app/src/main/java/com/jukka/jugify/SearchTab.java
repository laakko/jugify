package com.jukka.jugify;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.rw.keyboardlistener.KeyboardUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.SEARCH_SERVICE;
import static com.jukka.jugify.MainActivity.mSpotifyAppRemote;
import static com.jukka.jugify.MainActivity.spotify;

public class SearchTab extends Fragment {

    String chosen_tab;
    ArrayList<String> names;
    ArrayList<String> urls;
    ArrayList<String> ids;
    ArrayList<Artist> artists;
    ArrayList<AlbumSimple> albums;
    ArrayList<PlaylistSimple> playlists;
    Common cm = new Common();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_search_tab, container, false);
        final SearchView search = (SearchView) view.findViewById(R.id.searchView);
        final ListView listResults = (ListView) view.findViewById(R.id.listResults);
        final NavigationTabStrip datatimeline = view.findViewById(R.id.searchFilter);
        datatimeline.setTitles("Artists", "Albums", "Tracks", "Playlists");
        datatimeline.setAnimationDuration(50);
        datatimeline.setTabIndex(0);
        chosen_tab = "artists";

        search.setIconifiedByDefault(false);
        search.setQueryHint("Search for " + chosen_tab);
        search.setFocusable(true);

        names = new ArrayList<String>();
        urls = new ArrayList<String>();
        ids = new ArrayList<String>();
        artists = new ArrayList<Artist>();
        albums = new ArrayList<AlbumSimple>();
        playlists = new ArrayList<PlaylistSimple>();
        final SearchListAdapter adapter = new SearchListAdapter(getContext(), names, urls);

        // Handle keyboard
        KeyboardUtils.addKeyboardToggleListener(getActivity(), new KeyboardUtils.SoftKeyboardToggleListener()
        {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible)
            {
                if(isVisible){
                    // NOTE: This is for 1440p screen, may need to be changed for other sizes
                    listResults.getLayoutParams().height = 800;
                    listResults.requestLayout();
                } else {
                    // NOTE: This is for 1440p screen, may need to be changed for other sizes
                    listResults.getLayoutParams().height = 1600;
                    listResults.requestLayout();
                }
            }
        });

        datatimeline.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
                if(index == 0) {
                    chosen_tab = "artists";
                    search.setQueryHint("Search for " + chosen_tab);
                } else if(index == 1) {
                    chosen_tab = "albums";
                    search.setQueryHint("Search for " + chosen_tab);
                } else if(index == 2) {
                    chosen_tab = "tracks";
                    search.setQueryHint("Search for " + chosen_tab);
                } else if(index == 3) {
                    chosen_tab = "playlists";
                    search.setQueryHint("Search for " + chosen_tab);
                }
            }

            @Override
            public void onEndTabSelected(String title, int index) {
            }
        });


        listResults.setAdapter(adapter);

        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    showInputMethod(view.findFocus());
                }
            }
        });


        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                names.clear();
                urls.clear();
                ids.clear();
                artists.clear();
                albums.clear();
                adapter.notifyDataSetChanged();
                spotifySearch(s, chosen_tab, adapter);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                names.clear();
                urls.clear();
                ids.clear();
                artists.clear();
                albums.clear();
                adapter.notifyDataSetChanged();
                spotifySearch(s, chosen_tab, adapter);


                return false;
            }
        });

        listResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                if(chosen_tab == "tracks") {
                    mSpotifyAppRemote.getPlayerApi().play(ids.get(i));
                    toast("Now playing: "+ adapter.getItem(i), R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK, getContext());
                }
                else if(chosen_tab == "albums") {

                    spotify.getAlbum(albums.get(i).id, new Callback<Album>() {
                        @Override
                        public void success(Album album, Response response) {
                            search.clearFocus();
                            cm.AlbumPopup(album, getContext(), view, true, false, true, 0);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                        }
                    });
                } else if(chosen_tab == "artists") {
                    search.clearFocus();
                    cm.ArtistPopup(artists.get(i), view, true, getContext());
                }
                else if(chosen_tab == "playlists") {
                    search.clearFocus();
                    cm.PlaylistPopup(getContext(), view, playlists.get(i).owner.toString(), playlists.get(i), true);
                   // mSpotifyAppRemote.getPlayerApi().play(playlists.get(i).uri);
                   // toast("Now playing: "+ adapter.getItem(i), R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK, getContext());
                }
            }
        });

        return view;
    }


    public void spotifySearch(String query, String type, final SearchListAdapter adapter) {


        final Map<String, Object> options = new HashMap<>();
        options.put("limit", 10);

        final Map<String, Object> options2 = new HashMap<>();
        options2.put("limit", 25);


        if(type == "artists") {

            spotify.searchArtists(query, options, new Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    names.clear();
                    urls.clear();
                    artists.clear();
                    for(Artist a : artistsPager.artists.items) {
                        names.add(a.name);
                        try{
                            urls.add(a.images.get(0).url);
                        } catch(IndexOutOfBoundsException ioobe) {

                        }
                        artists.add(a);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });

        } else if(type == "albums") {


            spotify.searchAlbums(query, options, new Callback<AlbumsPager>() {
                @Override
                public void success(AlbumsPager albumsPager, Response response) {
                    names.clear();
                    urls.clear();
                    albums.clear();
                    for(AlbumSimple a : albumsPager.albums.items) {
                        names.add(a.name);
                        try{
                            urls.add(a.images.get(0).url);
                        } catch(IndexOutOfBoundsException ioobe) {

                        }
                        albums.add(a);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });

        } else if(type == "tracks") {

            spotify.searchTracks(query, options, new Callback<TracksPager>() {
                @Override
                public void success(TracksPager tracksPager, Response response) {
                    names.clear();
                    urls.clear();
                    ids.clear();
                    for(Track t : tracksPager.tracks.items) {
                        names.add(t.name + " - " + t.artists.get(0).name);
                        try{
                            urls.add(t.album.images.get(0).url);
                        } catch (IndexOutOfBoundsException ioobe) {

                        }
                        ids.add(t.uri);
                    }
                    adapter.notifyDataSetChanged();

                }

                @Override
                public void failure(RetrofitError error) {

                }
            });

        } else if(type == "playlists") {
            spotify.searchPlaylists(query, options2, new Callback<PlaylistsPager>() {
                @Override
                public void success(PlaylistsPager playlistsPager, Response response) {
                    names.clear();
                    urls.clear();
                    playlists.clear();
                    for(PlaylistSimple p : playlistsPager.playlists.items) {
                        names.add(p.name + " - " + p.owner.display_name);
                        try {
                            urls.add(p.images.get(0).url);
                        } catch (IndexOutOfBoundsException ioobe) {

                        }
                        playlists.add(p);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }

    }


    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    public void toast(String message, int drawable, int tintcolor, Context ctx) {
        Toasty.custom(ctx, message, drawable, tintcolor, 700, true, true).show();
    }

}
