package com.imagecroper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

public class ImageCropedAdapter extends RecyclerView.Adapter<ImageCropedAdapter.MyViewHolder> {

    private List<ImageUriList> moviesList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView title;

        public MyViewHolder(View view) {
            super(view);
            title = (ImageView) view.findViewById(R.id.title);
        }
    }


    public ImageCropedAdapter(List<ImageUriList> moviesList) {
        this.moviesList = moviesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ImageUriList movie = moviesList.get(position);
        holder.title.setImageURI(movie.getImageUri());

    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}