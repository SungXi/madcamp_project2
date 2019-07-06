package com.example.serverapp.Fragment2;

import android.graphics.Bitmap;

public class ImageItem {
    private Bitmap image;
    private String image_name;
    private float[] feature;
    private float[] feature2;

    public ImageItem(Bitmap image, String image_name, float[] feature, float[] feature2) {
        this.image = image;
        this.image_name = image_name;
        this.feature = feature;
        this.feature2 = feature2;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getNmae() {
        return image_name;
    }

    public void setName(String image_name) {
        this.image_name = image_name;
    }

    public  float[] getFeature() { return feature; }

    public void setFeature(float[] feature) { this.feature = feature; }

    public  float[] getFeature2() { return feature2; }

    public void setFeature2(float[] feature) { this.feature2 = feature; }
}
