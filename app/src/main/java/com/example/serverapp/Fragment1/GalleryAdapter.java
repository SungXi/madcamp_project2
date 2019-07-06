package com.example.serverapp.Fragment1;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.serverapp.R;

import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

    private ArrayList<String> images;
    private RequestOptions reqOpt;
    private Context context;
    private final RequestManager glide;

    public GalleryAdapter(Context localContext,
                          RequestManager mGlideRequestManager,
                          ArrayList<String> localData) {
        context = localContext;
        glide = mGlideRequestManager;
        images = localData;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public RequestManager getGlide() { return glide; }

    public int getCount() {
        return images.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        ImageView picturesView;
        if (convertView == null) {
            picturesView = new ImageView(context);
            picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            picturesView
                    .setLayoutParams(new ExpandableGridView.LayoutParams(420, 420));

        } else {
            picturesView = (ImageView) convertView;
        }

        reqOpt = RequestOptions
                .fitCenterTransform()
                .transform(new RoundedCorners(5))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(picturesView.getWidth(), picturesView.getHeight());

        glide.load(images.get(position))
                .thumbnail(0.25f)
                .apply(reqOpt)
                .placeholder(R.mipmap.ic_launcher).centerCrop()
                .into(picturesView);

        return picturesView;
    }
}
