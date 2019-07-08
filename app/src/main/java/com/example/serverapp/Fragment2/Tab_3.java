package com.example.serverapp.Fragment2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.serverapp.R;
import com.example.serverapp.Retrofit.IAppService;
import com.example.serverapp.Retrofit.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Tab_3 extends Fragment {
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    private Button result_image;
    private ImageView original_image;
    private Bitmap bitmap_image;
    private IAppService apiService;

    final int PIXEL_THRESHOLD = 500;
    final int GET_IMAGE = 10;
    final int GET_BACK = 11;
    final int UPLOAD_IMAGE = 12;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab3_layout, container, false);

        result_image = view.findViewById(R.id.result_image);
        original_image = view.findViewById(R.id.original_image);
        Retrofit retrofitClient = RetrofitClient.getInstance();
        apiService = retrofitClient.create(IAppService.class);

        result_image.setVisibility(View.GONE);
        result_image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent result_intent = new Intent(getActivity(), ShowResult.class);
                Drawable d = original_image.getDrawable();
                bitmap_image = ((BitmapDrawable) d).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap_image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();
                result_intent.putExtra("bitmap_image", bytes);
                startActivityForResult(result_intent, GET_BACK);
            }
        });

        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        fab = view.findViewById(R.id.tab3Fab);
        fab1 = view.findViewById(R.id.tab3Fab1);
        fab2 = view.findViewById(R.id.tab3Fab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                switch (id) {
                    case R.id.tab3Fab:
                        anim();
                        break;
                    case R.id.tab3Fab1:
                        anim();
                        break;
                    case R.id.tab3Fab2:
                        anim();
                        break;
                    default:
                        break;
                }
            }
        });

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture to Compare"), GET_IMAGE);
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture to Upload"), UPLOAD_IMAGE);
            }
        });

        return view;
    }

    public void anim() {
        if (isFabOpen) {
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
        } else {
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check which request we're responding to
        if (requestCode == GET_IMAGE) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
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
        } else if (requestCode == UPLOAD_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
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

                    HistogramGenerator histogramGenerator = new HistogramGenerator();
                    float[] featureVector = histogramGenerator.getHueDistribution(img);
                    float[] featureVector2 = histogramGenerator.getSatDistribution(img);
                    multipartImageUpload(img, featureVector, featureVector2);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void multipartImageUpload(Bitmap mBitmap, float[] featureVector1, float[] featureVector2) {
        try {
            File filesDir = getContext().getFilesDir();
            File file = new File(filesDir, "image" + ".png");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), reqFile);
            RequestBody name = RequestBody.create("upload", MediaType.parse("text/plain"));
            RequestBody feature1 = RequestBody.create(Arrays.toString(featureVector1), MediaType.parse("text/plain"));
            RequestBody feature2 = RequestBody.create(Arrays.toString(featureVector2), MediaType.parse("text/plain"));

            Call<ResponseBody> req = apiService.addDB(body, name, feature1, feature2);
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        Toast.makeText(getContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error : " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getContext(), "Request failed.", Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
