package com.example.serverapp.Fragment2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.serverapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Tab_3 extends Fragment {
    private Button select_image;
    private Button result_image;
    private ImageView original_image;
    private Bitmap bitmap_image;

    final int PIXEL_THRESHOLD = 500;
    final int GET_IMAGE = 10;
    final int GET_BACK = 11;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab3_layout, container, false);

//        select_image = view.findViewById(R.id.select_image);
        result_image = view.findViewById(R.id.result_image);
        original_image = view.findViewById(R.id.original_image);

        result_image.setVisibility(View.GONE);

//        select_image.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GET_IMAGE);
//            }
//        });

        result_image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent result_intent = new Intent(getActivity(), ShowResult.class);

                Drawable d = original_image.getDrawable();
                bitmap_image = ((BitmapDrawable)d).getBitmap();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap_image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();
                result_intent.putExtra("bitmap_image", bytes);

                // test
                System.out.println("bytes is " + bytes);

                startActivityForResult(result_intent, GET_BACK);
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.imageSyncFab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture to Upload"), GET_IMAGE);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check which request we're responding to
        if (requestCode == GET_IMAGE) {
            // Make sure the request was successful
            if (resultCode == getActivity().RESULT_OK) {
                try {
                    // make bitmap image
                    Uri fileUri = data.getData();
                    InputStream in = getActivity().getContentResolver().openInputStream(fileUri);
                    Bitmap img = BitmapFactory.decodeStream(in);
                    in.close();

                    // resizing image if too big
                    if (img.getHeight() > PIXEL_THRESHOLD | img.getWidth() > PIXEL_THRESHOLD){
                        int dstWidth = img.getWidth();
                        int dstHeight = img.getHeight();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4;
                        if (dstHeight > dstWidth){
                            dstWidth = dstWidth*PIXEL_THRESHOLD/dstHeight;
                            dstHeight = PIXEL_THRESHOLD;
                        }
                        else {
                            dstHeight = dstHeight*PIXEL_THRESHOLD/dstWidth;
                            dstWidth = PIXEL_THRESHOLD;
                        }
                        img = Bitmap.createScaledBitmap(img, dstWidth, dstHeight, true);
                    }

                    // show image
                    result_image.setVisibility(View.VISIBLE);
                    original_image.setImageBitmap(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        else if (requestCode == GET_BACK) {
            if (resultCode == getActivity().RESULT_OK) {
                System.out.println("nothing");
            }
        }
    }
}
