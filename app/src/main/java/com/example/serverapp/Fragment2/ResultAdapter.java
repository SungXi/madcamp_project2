package com.example.serverapp.Fragment2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.serverapp.R;

import java.util.ArrayList;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.CustomViewHolder> {
    private ArrayList<ImageItem> imageList;

    public ResultAdapter(ArrayList<ImageItem> list) {
        imageList = list;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView result_image;
        public CustomViewHolder(View view) {
            super(view);
            result_image = view.findViewById(R.id.result_image);
        }
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tab3_image, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder viewHolder, final int position) {
        viewHolder.result_image.setImageBitmap(imageList.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }
}
