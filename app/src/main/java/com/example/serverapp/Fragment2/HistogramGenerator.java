package com.example.serverapp.Fragment2;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Arrays;

public class HistogramGenerator {
    public static Bitmap doGrayscale(Bitmap src) {
        final double GS_RED = 0.299;
        final double GS_GREEN = 0.587;
        final double GS_BLUE = 0.114;

        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        int A, R, G, B;
        int pixel;

        int width = src.getWidth();
        int height = src.getHeight();

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                pixel = src.getPixel(x, y);

                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                R = G = B = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);

                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return bmOut;
    }

    public static float[] getDistribution(Bitmap src) {
        float[] distOut  = new float[256];
        Bitmap graySrc = doGrayscale(src);

        int R;
        int pixel;

        int width = graySrc.getWidth();
        int height = graySrc.getHeight();

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = graySrc.getPixel(x, y);

                R = Color.red(pixel);

                distOut[R] ++;
            }
        }

        float length = 0;

        for (int i = 0; i < 256; ++i) {
            length = length + distOut[i];
        }

        System.out.println("histogram is " + distOut);
        System.out.println(Arrays.toString(distOut));

        return distOut;
    }

    public static float[] getHueDistribution(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();

        int R, G, B;
        int pixel;

        int step = 360;

        float[] histogram = new float[step];

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                pixel = src.getPixel(x, y);

                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                float[] hsv = new float[3];
                Color.RGBToHSV(R, G, B, hsv);

                histogram[(int) (hsv[0])] ++;
            }
        }

        float length = 0;

        for (int i = 0; i < step; ++i) {
            length = length + histogram[i];
        }

        return histogram;
    }

    public static float[] getSatDistribution(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();

        int R, G, B;
        int pixel;

        int step = 256;

        float[] histogram = new float[step];

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                pixel = src.getPixel(x, y);

                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                float[] hsv = new float[3];
                Color.RGBToHSV(R, G, B, hsv);

                histogram[(int) (hsv[1] * (step - 1))] ++;
            }
        }

        float length = 0;

        for (int i = 0; i < step; ++i) {
            length = length + histogram[i];
        }

        return histogram;
    }
}
