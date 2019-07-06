package com.example.serverapp.Fragment2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serverapp.BitmapAddRequest;
import com.example.serverapp.R;

import java.util.ArrayList;

public class ShowResult extends AppCompatActivity {
    private Button back_button;

    RecyclerView mRecyclerView;
    ArrayList<ImageItem> imageList;
    LinearLayoutManager mLinearLayoutManager;
    ResultAdapter mResultAdaptor;

    byte[] bytes;
    Bitmap bitmap_image;

    ImageView original;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab3_result);

        bytes = getIntent().getByteArrayExtra("bitmap_image");
        bitmap_image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        original = findViewById(R.id.original_image);
        original.setImageBitmap(bitmap_image);

        imageList = new ArrayList<>();

        // Should send proper histogram distribution to the server
        HistogramGenerator histogramGenerator = new HistogramGenerator();
        float[] featureVector = histogramGenerator.getHueDistribution(bitmap_image);
        float[] featureVector2 = histogramGenerator.getSatDistribution(bitmap_image);

        // Should get proper images from the server
        ImageItem new_item = new ImageItem(bitmap_image, "original image", featureVector, featureVector2);

//        new BitmapAddRequest(new_item).execute("http://10.0.2.2:3000/sendImage");

        // Should add proper images to the imageList
        imageList.add(new_item);

//        BitmapProcess bitmapProcess = new BitmapProcess();
//        String encodedImage = bitmapProcess.getStringFromBitmap(bitmap_image);
//        Bitmap decodedImage = bitmapProcess.getBitmapFromString(encodedImage);
//
//        ImageItem decoded_item = new ImageItem(decodedImage, "decoded image", featureVector, featureVector2);
//        imageList.add(decoded_item);


        // show images in image list

        mRecyclerView = findViewById(R.id.recyclerView);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mResultAdaptor = new ResultAdapter(imageList);
        mRecyclerView.setAdapter(mResultAdaptor);

        back_button = findViewById(R.id.back);

        back_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent result_Intent = new Intent();
        setResult(Activity.RESULT_OK, result_Intent);
        super.onBackPressed();
    }
}
