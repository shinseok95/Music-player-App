package com.example.MusicPlayer;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import com.squareup.picasso.Picasso;

class Adapter extends BaseAdapter {

    private Context context = null;
    private Audio music = null;
    private ArrayList<Audio> musicList = null;
    private LayoutInflater layoutInflater= null;

    public Adapter(Context context, ArrayList<Audio> musicList) {
        this.context = context;
        this.musicList = musicList;
        layoutInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int i) {
        return musicList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final int position =i;

        View itemAdapter = view;
        ViewHolder viewHolder;

        if(itemAdapter == null){

            itemAdapter = layoutInflater.inflate(R.layout.adapter_view,null);
            viewHolder = new ViewHolder();

            viewHolder.albumArtID = (ImageView)itemAdapter.findViewById(R.id.albumArtID);
            viewHolder.titleID = (TextView) itemAdapter.findViewById(R.id.titleID);

            itemAdapter.setTag(viewHolder);

        }
        else{
            viewHolder = (ViewHolder)itemAdapter.getTag();
        }

        // Adapter click -> Go to PlayMusicActivity
        itemAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(),PlayMusicActivity.class);
                intent.putExtra("MUSIC_POSITION",position);
                intent.putExtra("MUSIC_LIST",musicList);
                intent.putExtra("IS_FIRST_PLAY",true);
                view.getContext().startActivity(intent);

            }
        });

        music = musicList.get(i);
        Uri uri = Uri.parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(uri, music.getAlbumId());
        Picasso.get().load(albumArtUri).error(R.drawable.tmpimage).into(viewHolder.albumArtID);
        viewHolder.titleID.setText(music.getTitle());

        return itemAdapter;
    }

}

class ViewHolder{

    ImageView albumArtID;
    TextView titleID;
}