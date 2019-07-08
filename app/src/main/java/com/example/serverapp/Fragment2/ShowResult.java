package com.example.serverapp.Fragment2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.serverapp.MainActivity;
import com.example.serverapp.R;
import com.example.serverapp.Retrofit.IAppService;
import com.example.serverapp.Retrofit.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class ShowResult extends AppCompatActivity {
    private final String SERVER = "http://143.248.36.156:3000";
    private Button back_button;

    RecyclerView mRecyclerView;
    ArrayList<ImageItem> imageList;
    LinearLayoutManager mLinearLayoutManager;
    ResultAdapter mResultAdaptor;
    RequestManager mGlideRequestManager;

    byte[] bytes;
    Bitmap bitmap_image;

    ImageView original;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IAppService iAppService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab3_result);

        mGlideRequestManager = Glide.with(this);
        Retrofit retrofitClient = RetrofitClient.getInstance();
        iAppService = retrofitClient.create(IAppService.class);
        bytes = getIntent().getByteArrayExtra("bitmap_image");
        bitmap_image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        original = findViewById(R.id.original_image);
        original.setImageBitmap(bitmap_image);

        imageList = new ArrayList<>();

        // Should send proper histogram distribution to the server
        HistogramGenerator histogramGenerator = new HistogramGenerator();
        float[] featureVector = histogramGenerator.getHueDistribution(bitmap_image);
        float[] featureVector2 = histogramGenerator.getSatDistribution(bitmap_image);

        compositeDisposable.add(iAppService.addHist(Arrays.toString(featureVector), Arrays.toString(featureVector2))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String data) throws Exception {
                        if (!data.isEmpty()) {
                            parseJson(data);
                        }
                    }
                }));

        // show images in image list
        mRecyclerView = findViewById(R.id.recyclerView);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mResultAdaptor = new ResultAdapter(imageList, mGlideRequestManager);
        mRecyclerView.setAdapter(mResultAdaptor);

        back_button = findViewById(R.id.back);
        back_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void parseJson(String data) {
        String jsonString = "{\"images\": " + data + " }";
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(jsonString);
        JsonArray memberArray = (JsonArray) jsonObject.get("images");
        Log.e("Find Image", memberArray.toString());

        for (int i = 0; i < memberArray.size() ; i++) {
            String tempString = memberArray.get(i).toString().replace("\"", "");
            ImageItem new_item = new ImageItem(SERVER + tempString, i + "");
            mResultAdaptor.getImage().add(new_item);
            mResultAdaptor.notifyItemInserted(mResultAdaptor.getItemCount() - 1);
            Log.e("Json Parser", SERVER + tempString);
        }
    }

    @Override
    public void onBackPressed() {
        Intent result_Intent = new Intent();
        setResult(Activity.RESULT_OK, result_Intent);
        super.onBackPressed();
    }
}
