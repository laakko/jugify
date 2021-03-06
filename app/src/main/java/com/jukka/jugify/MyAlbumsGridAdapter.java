package com.jukka.jugify;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import kaaes.spotify.webapi.android.models.SavedAlbum;

public class MyAlbumsGridAdapter extends ArrayAdapter<SavedAlbum> {

    private Context mContext;
    private ArrayList<SavedAlbum> albumslist = new ArrayList<SavedAlbum>();

    public MyAlbumsGridAdapter(@NonNull Context context, ArrayList<SavedAlbum> list) {
        super(context, 0, list);
        mContext = context;
        albumslist = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.layout_my_albums, parent, false);


        SavedAlbum sa = albumslist.get(position);
        Collections.sort(albumslist, new Comparator<SavedAlbum>() {
            @Override
            public int compare(SavedAlbum savedAlbum, SavedAlbum t1) {
                return savedAlbum.album.name.compareTo(t1.album.name);
            }
        });

        ImageView albumImage = listItem.findViewById(R.id.imgArtist);
        TextView albumName = listItem.findViewById(R.id.txtArtistName);
        //String aname = sa.album.name + "\n" + sa.album.artists.get(0).name;
        String aname = sa.album.name;
        albumName.setText(aname);
        String albumImageUrl = sa.album.images.get(0).url;
        ImageLoader imgloader = ImageLoader.getInstance();
        //imgloader.displayImage(albumImageUrl, albumImage, new ImageSize(100,100));
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.baseline_album_24).cacheOnDisk(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).defaultDisplayImageOptions(defaultOptions).build();
        imgloader.displayImage(albumImageUrl, albumImage, defaultOptions);


        return listItem;

    }
}