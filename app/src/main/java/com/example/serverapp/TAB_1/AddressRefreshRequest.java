package com.example.serverapp.TAB_1;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.ArrayList;

public class AddressRefresh extends AsyncTask<String, String, String> {
    private AddressAdapter addressAdapter;
    ArrayList<AddressItem> addressList;

    public AddressRefresh(AddressAdapter addressAdapter, ArrayList<AddressItem> addressList) {
        this.addressAdapter = addressAdapter;
        this.addressList = addressList;
    }

    @Override
    protected String doInBackground(String... urls)
    {
        try {
            JSONObject jsonObject = new JSONObject();
            //jsonObject.accumulate("user_id", "androidTest");
            //jsonObject.accumulate("name", "yun");

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

                // things related to output stream
                OutputStream outStream = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();

                // things related to input stream
                InputStream stream = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";

                while((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                return buffer.toString();
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

        System.out.println("okay");
        System.out.println(result);

        try {
            JSONArray jsonArray = new JSONArray(result);

            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObj = jsonArray.getJSONObject(i);

                System.out.println(jsonObj);
            }

            addressList.clear();

            for (int i = 0 ; i < jsonArray.length() ; i++) {
                try {
                    JSONObject person = jsonArray.getJSONObject(i);
                    AddressItem item = new AddressItem(person.get("name").toString(), person.get("number").toString(), person.get("email").toString());
                    addressList.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            addressAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.e("MYAPP", "unexpected JSON exception", e);
        }
    }
}

