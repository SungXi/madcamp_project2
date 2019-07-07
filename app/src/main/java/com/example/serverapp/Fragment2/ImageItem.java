package com.example.serverapp.Fragment2;

import android.graphics.Bitmap;

public class ImageItem {
    private String url;
    private String image_name;

    public ImageItem(String url, String image_name) {
        this.url = url;
        this.image_name = image_name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return image_name;
    }

    public void setName(String image_name) {
        this.image_name = image_name;
    }

}
