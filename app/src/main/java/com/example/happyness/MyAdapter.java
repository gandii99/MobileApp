package com.example.happyness;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<Integer> id_plus = new ArrayList<Integer>();
    private List<String> images = new ArrayList();
    private List<String> tytul = new ArrayList();
    private List<Integer> plusy = new ArrayList();
    Context context;
    private OnNoteListener mOnNoteListener;

    public MyAdapter(Context context, List<String> tytul, List<String> images, List<Integer> plusy, OnNoteListener onNoteListener){
        this.context = context;
        this.tytul = tytul;
        this.images = images;
        this.plusy = plusy;
        this.mOnNoteListener = onNoteListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view, mOnNoteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.mem_tytul.setText(tytul.get(position));
        holder.mem_plusy.setText('+' + plusy.get(position).toString());
        Picasso.get().load(images.get(position)).into(holder.mem_obraz);
            /*
            //MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            //retriever.setDataSource("https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4");
            //int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            holder.mem_video.getLayoutParams().height = 500;
            holder.mem_video.setVideoURI(Uri.parse("https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4"));
            MediaController mediaController = new MediaController(context);
            holder.mem_video.setMediaController(mediaController);
            mediaController.setAnchorView(holder.mem_video);
            holder.mem_video.start();
            */

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mem_tytul;
        ImageView mem_obraz;
        VideoView mem_video;
        Button mem_plusy;
        ImageView mem_messenger;
        Button mem_udostepnij;
        OnNoteListener onNoteListener;

        public MyViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);

            mem_tytul = itemView.findViewById(R.id.mem_tytul);
            mem_obraz = itemView.findViewById(R.id.mem_obraz);
            mem_video = itemView.findViewById(R.id.mem_video);
            mem_plusy = itemView.findViewById(R.id.mem_plusy);
            mem_messenger = itemView.findViewById(R.id.mem_messenger);
            mem_udostepnij = itemView.findViewById(R.id.mem_udostepnij);

            this.onNoteListener = onNoteListener;

            itemView.setOnClickListener(this);
            mem_plusy.setOnClickListener(this);
            mem_messenger.setOnClickListener(this);
            mem_udostepnij.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == mem_plusy.getId()){
                if(!id_plus.contains(mem_plusy.getId())){
                    mem_plusy.setText('+'+String.valueOf(Integer.parseInt(mem_plusy.getText().toString())+1));
                    mem_plusy.setBackgroundColor(0x7F00FF00);
                    id_plus.add(mem_plusy.getId());
                }else{
                    mem_plusy.setText('+'+String.valueOf(Integer.parseInt(mem_plusy.getText().toString())-1));
                    mem_plusy.setBackgroundColor(0x77990F02);
                    id_plus.remove(id_plus.indexOf(mem_plusy.getId()));
                }

            }else if(view.getId() == mem_messenger.getId()){

                BitmapDrawable bitmapDrawable = ((BitmapDrawable) mem_obraz.getDrawable());
                Bitmap bitmap = bitmapDrawable .getBitmap();

                //String bitmapPath = MediaStore.Images.Media.insertImage(view.getContext().getContentResolver(), bitmap,"some title", null);
                //Uri bitmapUri = Uri.parse(bitmapPath);

                Uri bitmapUri = getImageUri(view.getContext(), bitmap);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                sendIntent.setType("image/jpeg");
                sendIntent.setPackage("com.facebook.orca");
                try {
                    view.getContext().startActivity(sendIntent);
                }
                catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(context,"Please Install Facebook Messenger", Toast.LENGTH_LONG).show();
                }
            }else if(view.getId() == mem_udostepnij.getId()){

                BitmapDrawable bitmapDrawable = ((BitmapDrawable) mem_obraz.getDrawable());
                Bitmap bitmap = bitmapDrawable .getBitmap();

                //String bitmapPath = MediaStore.Images.Media.insertImage(view.getContext().getContentResolver(), bitmap,"some title", null);
                //Uri bitmapUri = Uri.parse(bitmapPath);
                Uri bitmapUri = getImageUri(view.getContext(), bitmap);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpeg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                view.getContext().startActivity(Intent.createChooser(shareIntent,"Share Image"));

            }else{
                onNoteListener.onNoteClick(getAdapterPosition());
            }
        }
    }

    public interface OnNoteListener{
        void onNoteClick(int position);
    }
/*
    private boolean haveNetwork(){
        boolean have_WIFI = false;
        boolean have_MobileData = false;

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos=connectivityManager.getAllNetworkInfo();

        for(NetworkInfo info:networkInfos){
            if(info.getTypeName().equalsIgnoreCase("WIFI")){
                if(info.isConnected()) {
                    have_WIFI = true;
                }
            }
            if(info.getTypeName().equalsIgnoreCase("MOBILE")){
                if(info.isConnected()) {
                    have_WIFI = true;
                }
            }
        }

        if(have_WIFI || have_MobileData){
            network_lost.setVisibility(View.INVISIBLE);
        }else{
            network_lost.setVisibility(View.VISIBLE);
        }

        return have_MobileData || have_WIFI;
    }

 */
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
