package com.jukka.jugify;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.ohoussein.playpause.PlayPauseView;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.media.audiofx.AudioEffect.CONTENT_TYPE_MUSIC;
import static android.media.audiofx.AudioEffect.EXTRA_PACKAGE_NAME;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.jukka.jugify.MainActivity.atoken;
import static com.jukka.jugify.MainActivity.audioSessionId;
import static com.jukka.jugify.MainActivity.displayheight;
import static com.jukka.jugify.MainActivity.mSpotifyAppRemote;
import static com.jukka.jugify.MainActivity.spotify;
import static com.jukka.jugify.MainActivity.trackArtist;
import static com.jukka.jugify.MainActivity.trackName;
import static com.jukka.jugify.MainActivity.userAuthd;
import static com.jukka.jugify.MainActivity.viewPager;

public class ListenTab extends Fragment {

    TextView txtTitle;
    TextView txtNowPlaying;
    TextView txtNowArtist;
    TextView lyrics;
    PlayPauseView playpause;
    ImageButton skip;
    ImageButton prev;
    ImageButton shuffle;
    ImageButton playlistAdd;
    ImageButton devices;
    Boolean isplaying = false;
    Boolean shuffling = false;
    Float instrumental;
    ImageView imgnowplaying;
    SeekBar seekbar;
    static String imguri;
    Boolean image_gotten = false;
    TextView key, tempo, loudness, timesignature;
    static String keystr;
    TextView songduration, songposition;
    TextView songinformation;
    TrackProgressBar mTrackProgressBar;
    LinearLayout bottomlayout, songcard;
    ProgressBar popularitybar, valencebar, dancebar, energybar, acousticbar;
    ImageView imageNowPlayingBig;
    TextView txtpopularity,txtvalence,txtdance, txtenergy;
    private PopupWindow EQpopup;
    Album nowPlayingAlbum;
    int lyricerrors = 0;
    Common cm = new Common();
    Boolean dontupdate;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_listen_tab, container, false);

        txtNowPlaying = (TextView) view.findViewById(R.id.txtNowPlaying);
        txtNowArtist = (TextView) view.findViewById(R.id.txtNowArtist);
        playpause = (PlayPauseView) view.findViewById(R.id.btnPlay);
        skip = (ImageButton) view.findViewById(R.id.btnNext);
        prev = (ImageButton) view.findViewById(R.id.btnPrev);
        shuffle = (ImageButton) view.findViewById(R.id.btnShuffle);
        //imgnowplaying = (ImageView) view.findViewById(R.id.imgNowPlaying);
        key = (TextView) view.findViewById(R.id.txtKey);
        tempo = (TextView) view.findViewById(R.id.txtBPM);
        loudness = (TextView) view.findViewById(R.id.txtLoudness);
        timesignature = (TextView) view.findViewById(R.id.txtTimeSignature);
        lyrics = (TextView) view.findViewById(R.id.txtLyrics);
        songduration = (TextView) view.findViewById(R.id.txtSongDuration);
        songposition = (TextView) view.findViewById(R.id.txtSongPosition);
        seekbar = (SeekBar) view.findViewById(R.id.seekBar);
        bottomlayout = (LinearLayout) view.findViewById(R.id.bottomlayout);
        songinformation = (TextView) view.findViewById(R.id.txtSongInformation);
        popularitybar = (ProgressBar) view.findViewById(R.id.popularityBar);
        valencebar = (ProgressBar) view.findViewById(R.id.valenceBar);
        dancebar = (ProgressBar) view.findViewById(R.id.danceBar);
        energybar = (ProgressBar) view.findViewById(R.id.energyBar);
       // acousticbar = (ProgressBar) view.findViewById(R.id.acousticBar);
        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        imageNowPlayingBig = (ImageView) view.findViewById(R.id.imageArtistBig);
        songcard = (LinearLayout) view.findViewById(R.id.songcard);
        txtpopularity = (TextView) view.findViewById(R.id.txtPopularity);
        txtvalence = (TextView) view.findViewById(R.id.txtValence);
        txtenergy = (TextView) view.findViewById(R.id.txtEnergy);
        txtdance = (TextView) view.findViewById(R.id.txtDanceability);
        playlistAdd = (ImageButton) view.findViewById(R.id.btnPlaylistAdd);
        devices = (ImageButton) view.findViewById(R.id.btnDevices);
        final UserTab userTab = new UserTab();

        final ScrollView scrollview = (ScrollView) view.findViewById(R.id.scrollview);

        if(displayheight == 1794) {
            scrollview.getLayoutParams().height = 1298;
            scrollview.requestLayout();
        }

        mTrackProgressBar = new TrackProgressBar(seekbar);




        if(userAuthd){


            txtNowPlaying.setText(trackName);

            mSpotifyAppRemote.getPlayerApi()
                    .subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>() {
                public void onEvent(PlayerState playerState) {

                    lyricerrors = 0;
                    final Track track = playerState.track;
                    if(playerState.isPaused){
                        isplaying = false;
                        playpause.change(true);
                    } else {
                        isplaying = true;
                        playpause.change(false);

                    }
                    if(playerState.playbackOptions.isShuffling){
                        shuffling = true;
                        shuffle.setColorFilter(Color.parseColor("#427DD1"));
                    } else {
                        shuffling = false;
                        shuffle.setColorFilter(Color.parseColor("#232E6E"));

                    }



                    if(playerState.playbackSpeed > 0) {
                        mTrackProgressBar.unpause();
                    } else {
                        mTrackProgressBar.pause();
                    }


                    if (track != null) {

                        // Get basic information
                        trackName = track.name;
                        trackArtist = track.artist.name;
                        txtNowPlaying.setText(trackName);
                        txtNowArtist.setText(trackArtist);
                        txtTitle.setText(track.name + "\n by " + track.artist.name);
                        String duration = String.format("%d:%d",
                                TimeUnit.MILLISECONDS.toMinutes(track.duration),
                                TimeUnit.MILLISECONDS.toSeconds(track.duration) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(track.duration))
                        );
                        songduration.setText(duration);


                        // Get progress bar
                        mTrackProgressBar.setDuration(track.duration);
                        mTrackProgressBar.update(playerState.playbackPosition);
                        seekbar.setEnabled(true);

                        // Get track information
                        getNowPlayingInformation(track.album.uri.substring(14));


                        // Get Album Image
                        imguri = track.imageUri.raw;
                        mSpotifyAppRemote.getImagesApi().getImage(track.imageUri)
                                .setResultCallback(new CallResult.ResultCallback<Bitmap>() {
                                    @Override
                                    public void onResult(Bitmap bitmap) {
                                        imageNowPlayingBig.setImageBitmap(bitmap);
                                    }
                                });



                        // Get audio features
                        getAudioFeatures(track.uri.substring(14));
                        String requestURL = "https://api.lyrics.ovh/v1/" + track.artist.name + "/" + track.name;
                        lyricsApi(requestURL);



                        txtTitle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                spotify.getArtist(track.artist.uri.substring(15), new Callback<Artist>() {
                                    @Override
                                    public void success(Artist artist, Response response) {
                                        cm.ArtistPopup(artist, getView(), true, getView().getContext());
                                    }
                                    @Override
                                    public void failure(RetrofitError error) {
                                    }
                                });
                            }
                        });

                        imageNowPlayingBig.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(displayheight == 2560) {
                                    // 1440p
                                    cm.AlbumPopup(nowPlayingAlbum , view.getContext(), getView(), false, true, false, (960+scrollview.getScrollY()));
                                } else {
                                    // 1080p
                                    cm.AlbumPopup(nowPlayingAlbum , view.getContext(), getView(), false, true, false, (850+scrollview.getScrollY()));
                                }
                            }
                        });

                        playlistAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                addToPlaylist(track, view, atoken);
                            }
                        });


                    } else {
                        seekbar.setEnabled(false);
                    }
                }

            });


            // Play/Pause, Skip, Prev and shuffle buttons
            playpause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isplaying){
                        mSpotifyAppRemote.getPlayerApi().pause();
                    } else {
                        mSpotifyAppRemote.getPlayerApi().resume();
                    }
                }
            });

            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lyrics.setText("Loading ...");
                    mSpotifyAppRemote.getPlayerApi().skipNext();
                }
            });

            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSpotifyAppRemote.getPlayerApi().skipPrevious();
                }
            });

            shuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(shuffling) {
                        mSpotifyAppRemote.getPlayerApi().setShuffle(false);
                    } else {
                        mSpotifyAppRemote.getPlayerApi().setShuffle(true);
                    }

                }
            });

            devices.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   getAvailableDevices(atoken);
                }
            });



        }
        return view;
    }


    public void getNowPlayingInformation(final String uri) {

        spotify.getAlbum(uri, new Callback<Album>() {
            @Override
            public void success(Album a, Response response) {
                songinformation.setText("From " + '"' + a.name + '"' + "\nreleased " + a.release_date);
                popularitybar.setProgress(a.popularity);
                nowPlayingAlbum = a;
            }
            @Override
            public void failure(RetrofitError error){
                Log.d("Album failure", error.toString());
            }
        });
    }


    public void getAudioFeatures(String uri) {
        spotify.getTrackAudioFeatures(uri, new Callback<AudioFeaturesTrack>() {

            @Override
            public void success(AudioFeaturesTrack aft, Response response){

                if(aft.key == 0){
                    keystr = "C";
                } else if(aft.key == 1) {
                    keystr = "C#";
                } else if(aft.key == 2) {
                    keystr = "D";
                } else if(aft.key == 3) {
                    keystr = "D#";
                } else if(aft.key == 4) {
                    keystr = "E";
                } else if(aft.key == 5) {
                    keystr = "F";
                } else if(aft.key == 6) {
                    keystr = "F#";
                } else if(aft.key == 7) {
                    keystr = "G";
                } else if(aft.key == 8) {
                    keystr = "G#";
                } else if(aft.key == 9) {
                    keystr = "A";
                } else if(aft.key == 10) {
                    keystr = "A#";
                } else if(aft.key == 11) {
                    keystr = "B";
                }


                if(aft.mode == 1) {
                    keystr += " Minor";
                } else {
                    keystr += " Major";
                }

                String tempoRounded = Float.toString((int)Math.round(aft.tempo));


                key.setText("Key: "+keystr);
                tempo.setText("Tempo: "+ tempoRounded + " BPM");
                loudness.setText("Loudness: "+Float.toString(aft.loudness) + "dB");
                timesignature.setText("Time signature: " + Integer.toString(aft.time_signature));

                energybar.setProgress((int)Math.round(aft.energy * 100));
                dancebar.setProgress((int)Math.round(aft.danceability * 100));
                valencebar.setProgress((int)Math.round(aft.valence * 100));

               // acousticbar.setProgress(Math.round(aft.acousticness * 100));
                instrumental = aft.instrumentalness;

            }

            @Override
            public void failure(RetrofitError error){
                Log.d("Audio features failure", error.toString());
            }
        });
    }

    public void lyricsApi(final String url) {

        // API: https://lyricsovh.docs.apiary.io
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                            try{
                                String txtlyrics = response.getString("lyrics");
                                lyrics.setText("\n" + txtlyrics + "\n");
                            } catch(JSONException je) {
                                je.printStackTrace();
                            }
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            lyricerrors += 1;
                            if(lyricerrors < 8) {
                                lyricsApi(url);
                            } else {
                                lyrics.setText("Lyrics not found  \n (or the track is instrumental) \n Click to try again \n");
                            }
                        }
                    });

            queue.add(jsonObjectRequest);


            lyrics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lyricsApi(url);
                }
            });
    }


    public void addToPlaylist(final Track song, View view, final String token) {
        final PopupWindow playlistpopup;
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_addtoplaylist,
                (ViewGroup) view.findViewById(R.id.tab_layout_2));

        playlistpopup = new PopupWindow(layout, MATCH_PARENT, MATCH_PARENT, true);
        playlistpopup.showAtLocation(layout, Gravity.TOP, 0, 0);

        TextView txtPlaylistPopup = (TextView) layout.findViewById(R.id.txtPlaylistPopup);
        txtPlaylistPopup.setText("Add " + song.name + " to playlist:");

        final MyPlaylistsGridAdapter pladapter;
        final ArrayList<PlaylistSimple> myplaylistslist = new ArrayList<>();

        final GridView gridPopupPlaylists = (GridView) layout.findViewById(R.id.gridPopupPlaylists);

        pladapter = new MyPlaylistsGridAdapter(getContext().getApplicationContext(), myplaylistslist);

        spotify.getMyPlaylists(new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> pager, Response response) {

                pladapter.clear();
                for(PlaylistSimple p : pager.items){
                    pladapter.add(p);
                }

                gridPopupPlaylists.setAdapter(pladapter);

                // Add songs to playlist
                gridPopupPlaylists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                        String url = "https://api.spotify.com/v1/playlists/" + pladapter.getItem(i).id + "/tracks?uris=" + song.uri;
                        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                (Request.Method.POST, url, null, new com.android.volley.Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {


                                    }
                                }, new com.android.volley.Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                    }
                                })
                        {
                            @Override
                            public Map getHeaders() throws AuthFailureError {
                                HashMap headers = new HashMap();
                                headers.put("Authorization", "Bearer " + token);
                                return headers;
                            }
                        };


                        queue.add(jsonObjectRequest);
                        cm.toast(song.name + " added to " + pladapter.getItem(i).name, R.drawable.ic_playlist_add_black_36dp, R.color.colorAccent, view.getContext());
                        playlistpopup.dismiss();

                    }
                });

                }
                @Override
                public void failure(RetrofitError error) {
                    Log.d("My playlists failure", error.toString());
                }
            });

    }

    public void getAvailableDevices(final String token) {
       // curl -X GET "https://api.spotify.com/v1/me/player/devices" -H "Authorization: Bearer {your access token}"

        String url = "https://api.spotify.com/v1/me/player/devices";

        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            final ArrayList<String> deviceNames = new ArrayList<String>();
                            final ArrayList<String> deviceIds = new ArrayList<String>();

                            deviceNames.add("This device");
                            deviceIds.add("dummyid");

                            JSONArray jarray = response.getJSONArray("devices");
                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject jo = jarray.getJSONObject(i);

                                deviceNames.add(jo.getString("name"));
                                deviceIds.add(jo.getString("id"));
                            }

                            // Open a popup containing device names
                            PopupWindow devicespopup;

                            LayoutInflater inflater = (LayoutInflater) getView().getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                            View layout = inflater.inflate(R.layout.popup_devices,
                                    (ViewGroup) getView().findViewById(R.id.tab_layout_2));

                            devicespopup = new PopupWindow(layout, MATCH_PARENT, 600, true);
                            devicespopup.showAtLocation(layout, Gravity.BOTTOM, -250, 570);

                            ListView listdevices = (ListView) layout.findViewById(R.id.listAvailableDevices);
                            ArrayAdapter<String> devicesadapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, deviceNames);
                            listdevices.setAdapter(devicesadapter);

                            listdevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                                    // Current device
                                    if(deviceNames.get(i) == "This device") {
                                        mSpotifyAppRemote.getConnectApi().connectSwitchToLocalDevice();
                                    } else {
                                        // Transfer playback to device at i
                                        // curl -X PUT "https://api.spotify.com/v1/me/player"
                                        // -H "Authorization: Bearer {your access token}" -H "Content-Type: application/json" --data "{device_ids:[\"74ASZWbe4lXaubB36ztrGX\"]}"
                                        String url = "https://api.spotify.com/v1/me/player";
                                        Log.d("TOKKENI", token);
                                        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
                                        JsonObjectRequest putRequest = new JsonObjectRequest
                                                (Request.Method.PUT, url, null, new com.android.volley.Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {
                                                    }
                                                }, new com.android.volley.Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                    }
                                                })
                                        {
                                            @Override
                                            public Map getHeaders() throws AuthFailureError {
                                                HashMap headers = new HashMap();
                                                headers.put("Authorization", "Bearer " + token);
                                                headers.put("Content-type", "application/json");
                                                return headers;
                                            }

                                            @Override
                                            public String getBodyContentType() {
                                                return "application/json; charset=utf-8";
                                            }

                                            @Override
                                            public byte[] getBody() {

                                                final String mRequestBody = "{\"device_ids\":[" + "\"" + deviceIds.get(i) + "\"]}";
                                                try {
                                                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                                                } catch (UnsupportedEncodingException uee) {
                                                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                                                    return null;
                                                }
                                            }
                                        };


                                        queue.add(putRequest);

                                    }

                                }
                            });

                        } catch(JSONException je) {

                        }


                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                })
        {
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };


        queue.add(jsonObjectRequest);
    }


    private class TrackProgressBar {

        private static final int LOOP_DURATION = 500;
        private final SeekBar mSeekBar;
        private final Handler mHandler;


        private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String position = String.format("%d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(seekBar.getProgress()),
                        TimeUnit.MILLISECONDS.toSeconds(seekBar.getProgress()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(seekBar.getProgress()))
                );
                songposition.setText(position);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSpotifyAppRemote.getPlayerApi().seekTo(seekBar.getProgress());
            }
        };

        private final Runnable mSeekRunnable = new Runnable() {
            @Override
            public void run() {
                int progress = mSeekBar.getProgress();
                mSeekBar.setProgress(progress + LOOP_DURATION);
                String position = String.format(Locale.US, "%d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(progress),
                        TimeUnit.MILLISECONDS.toSeconds(progress) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress))
                );
                songposition.setText(position);

                mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
            }
        };

        private TrackProgressBar(SeekBar seekBar) {
            mSeekBar = seekBar;
            mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
            mHandler = new Handler();
        }

        private void setDuration(long duration) {
            mSeekBar.setMax((int) duration);
        }

        private void update(long progress)   {
            mSeekBar.setProgress((int) progress);
        }

        private void pause() {
            mHandler.removeCallbacks(mSeekRunnable);
        }

        private void unpause() {
            mHandler.removeCallbacks(mSeekRunnable);
            mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
        }
    }


}