package com.example.serverapp.Fragment2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.serverapp.R;

import java.util.ArrayList;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.CustomViewHolder> {
    private ArrayList<ImageItem> imageList;
    private RequestManager mGlideRequestManager;

    public ResultAdapter(ArrayList<ImageItem> list, RequestManager manager) {
        imageList = list;
        mGlideRequestManager = manager;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView result_image;
        public CustomViewHolder(View view) {
            super(view);
            result_image = view.findViewById(R.id.result_image);
        }
    }

    public ArrayList<ImageItem> getImage() {
        return imageList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tab3_image, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder viewHolder, final int position) {
        mGlideRequestManager.load(imageList.get(position).getUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(viewHolder.result_image);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }
}
