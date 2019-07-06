package com.example.serverapp;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.example.serverapp.Fragment2.BitmapProcess;
import com.example.serverapp.Fragment2.ImageItem;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BitmapAddRequest extends AsyncTask<String, String, String> {
    ImageItem new_item;
    Bitmap new_image;
    float[] feature;
    float[] feature2;

    public BitmapAddRequest(ImageItem new_item) {
        this.new_item = new_item;
        this.new_image = new_item.getImage();
        this.feature = new_item.getFeature();
        this.feature2 = new_item.getFeature2();
    }

    @Override
    protected String doInBackground(String... urls)
    {
        try {
            BitmapProcess bitmapProcess = new BitmapProcess();
            String encodedImage = bitmapProcess.getStringFromBitmap(new_image);

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("image", encodedImage);
            jsonObject.accumulate("feature", this.feature);
            jsonObject.accumulate("feature2", this.feature2);

            HttpURLConnection con = null;
            BufferedReader reader = null;

            try
            {
                URL url = new URL(urls[0]);
                con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("POST");
                con.setRequestProperty("Cache-Control", "no-cache");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "text/html");
                con.setDoOutput(true);
                con.setDoInput(true);
                con.connect();

                System.out.println("before write");

                // things related to output stream
                OutputStream outStream = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();

                System.out.println("after write");

                // things related to input stream
                InputStream stream = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";

                while((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                return buffer.toString();
                // return "";
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if(con != null) {
                    con.disconnect();
                }
                try {
                    if(reader != null) {
                        reader.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result)
    {
        super.onPostExecute(result);

        System.out.println(result);
        System.out.println("Bitmap Add Request Completed");
    }
}
