package com.example.serverapp.Fragment1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.example.serverapp.MainActivity;
import com.example.serverapp.R;
import com.example.serverapp.Retrofit.IAppService;
import com.example.serverapp.Retrofit.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Fragment1 extends Fragment implements PopupMenu.OnMenuItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    BitmapProcess bitmapProcess;
    IAppService apiService;
    private static final int SEND_PICTURE = 1;
    private String ownerEmail;
    private GalleryAdapter gallery;
    private ImageView imageView;
    private ImageView imageViewTest;
    private ExpandableGridView gridView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    private String currentPhotoPath;
    private int pos = -1;
    private RequestManager mGlideRequestManager;
    private ArrayList<String> localData = new ArrayList<>();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IAppService iAppService;

    public Fragment1 () {
        // Empty Constructor
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGlideRequestManager = Glide.with(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_1, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh1);
        swipeRefreshLayout.setOnRefreshListener(this);
        Retrofit retrofitClient = RetrofitClient.getInstance();
        iAppService = retrofitClient.create(IAppService.class);
        getAllShownImagesPath();

        initRetrofitClient();
        bitmapProcess = new BitmapProcess();
        imageViewTest = view.findViewById(R.id.imageViewTest);
        imageView = view.findViewById(R.id.imageView);
        gridView = view.findViewById(R.id.gridView);

        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        fab = view.findViewById(R.id.imageSyncFab);
        fab1 = view.findViewById(R.id.imageSyncFab1);
        fab2 = view.findViewById(R.id.imageSyncFab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                switch (id) {
                    case R.id.contactFab:
                        anim();
                        break;
                    case R.id.contactFab1:
                        anim();
                        break;
                    case R.id.contactFab2:
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
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture to Upload"), SEND_PICTURE);
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(Intent.createChooser(galleryIntent, "Take Picture to Upload"), SEND_PICTURE);
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

    private void initRetrofitClient() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        apiService = new Retrofit.Builder()
                .baseUrl("http://143.248.36.156:3000/")
                .client(client)
                .build()
                .create(IAppService.class);
    }

    private void multipartImageUpload(Bitmap mBitmap) {
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
            RequestBody email = RequestBody.create(((MainActivity) getActivity()).getOwnerEmail(), MediaType.parse("text/plain"));

            Call<ResponseBody> req = apiService.postImage(body, name, email);
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        Toast.makeText(getContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error : " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                    if (gallery != null) {
                        gallery.notifyDataSetChanged();
                        gridView.setAdapter(gallery);
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment, new Fragment1());
                        transaction.commit();
                        swipeRefreshLayout.setRefreshing(false);
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

    public void showPopup(View v, int i) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        this.pos = i;
        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_image);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.delete:
                if (this.pos >= 0) {
                    String Url = gallery.getImages().get(pos);
                    if (Url.length() > 1 && Url.contains("/")) {
                        String[] entries = Url.split("/");
                        Url = "/uploads/" + entries[entries.length - 1];
                        compositeDisposable.add(iAppService.deleteImage(Url, ((MainActivity) getActivity()).getOwnerEmail())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<String>() {
                                    @Override
                                    public void accept(String data) throws Exception {
                                        Toast.makeText(getContext(), data.replace("\"", ""), Toast.LENGTH_SHORT).show();
                                    }
                                }));
                        gallery.getImages().remove(pos);
                        gallery.notifyDataSetChanged();
                    }
                    this.pos = -1;
                }
                return true;
//            case R.id.duplicate:
//                if (this.pos >= 0) {
//                    File imgFile = new File(gallery.getImages().get(this.pos));
//                    if (imgFile.exists()) {
//                        Uri photoUri = Uri.fromFile(imgFile);
//                        if (null != photoUri) {
//                            duplicateImage(photoUri);
//                        }
//                    }
//                    this.pos = -1;
//                }
//                return true;
//            case R.id.send:
//                if (this.pos >= 0) {
//                    File imgFile = new File(gallery.getImages().get(this.pos));
//                    if (imgFile.exists()) {
//                        Intent intent = new Intent(Intent.ACTION_SEND);
//                        intent.setType("image/*");
//                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getContext(),
//                                "com.example.serverapp.fileprovider",
//                                imgFile));
//                        Intent chooser = Intent.createChooser(intent, getContext().getString(R.string.share));
//                        if (null != intent.resolveActivity(getContext().getPackageManager())) {
//                            startActivity(chooser);
//                        } else {
//                            Snackbar.make(getView(), "Sending failed.", Snackbar.LENGTH_LONG)
//                                    .setAction("Action", null).show();
//                        }
//                    }
//                    this.pos = -1;
//                }
//                return true;
            default:
                this.pos = -1;
                return true;
        }
    }

    private void duplicateImage(Uri photoUri) {
        File image = null;
        Uri savingUri;
        try {
            image = createImageFile();
        } catch (IOException ex) {
            Snackbar.make(getView(), "Crop failed.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        if (null != image && image.exists()) {
            savingUri = Uri.fromFile(image);
            if (null != savingUri) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                if (null != photoUri && Build.VERSION.SDK_INT >= 26) {
                    File tempFile = new File(photoUri.getPath());
                    File dscFile = new File(currentPhotoPath);
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(dscFile, false);
                        fileOutputStream.write(Files.readAllBytes(tempFile.toPath()));
                        fileOutputStream.close();
                    } catch (Exception ex) {
                        Snackbar.make(getView(), "Saving failed.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    intent.setData(Uri.fromFile(dscFile));
                    getActivity().sendBroadcast(intent);
                    Snackbar.make(getView(), "Saved successfully.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(getView(), "Saving failed.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/Camera";
        File storageDir = new File(path);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CropImage
                    .CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = result.getUri();
                if (null != contentUri && Build.VERSION.SDK_INT >= 26) {
                    File tempFile = new File(contentUri.getPath());
                    File dscFile = new File(currentPhotoPath);
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(dscFile, false);
                        fileOutputStream.write(Files.readAllBytes(tempFile.toPath()));
                        fileOutputStream.close();
                    } catch (Exception ex) {
                        Snackbar.make(getView(), "Saving failed.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    intent.setData(Uri.fromFile(dscFile));
                    getActivity().sendBroadcast(intent);
                    Snackbar.make(getView(), "Saved successfully.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(getView(), "Saving failed.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            case SEND_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        if (bitmap != null) {
                            multipartImageUpload(bitmap);
                        }
                    }
                }
            default:
                break;
        }
    }

    private void getAllShownImagesPath() {
        ownerEmail = ((MainActivity) getActivity()).getOwnerEmail();
        if (ownerEmail != null) {
            compositeDisposable.add(iAppService.fetchImages(((MainActivity) getActivity()).getOwnerEmail())
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
        }
    }

    private void parseJson(String data) {
        String jsonString = "{\"images\": " + data + " }";
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(jsonString);
        JsonArray memberArray = (JsonArray) jsonObject.get("images");
        try {
            JsonObject testObject = (JsonObject) memberArray.get(0);
        } catch (OnErrorNotImplementedException ex) {
            memberArray = (JsonArray) memberArray.get(0);
        }

        for (int i = 0; i < memberArray.size() ; i++) {
            JsonObject tempObject = (JsonObject) memberArray.get(i);
            String tempString = tempObject.get("path").toString();
            localData.add("http://143.248.36.156:3000" + tempString.substring(1, tempString.length() - 1));
            Log.e("Json Parser", localData.get(i));
        }

        gallery = new GalleryAdapter(getActivity(), mGlideRequestManager, localData);
        gridView.setAdapter(gallery);
        gridView.setExpanded(true);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (null != gallery.getImages() && !gallery.getImages().isEmpty()) {
                    mGlideRequestManager.load(gallery.getImages().get(i))
                            .placeholder(R.mipmap.ic_launcher).centerCrop()
                            .into(imageView);
                    final ImagePopup imagePopup = new ImagePopup(getContext());
                    imagePopup.setBackgroundColor(Color.argb(128,0,0,0));
                    imagePopup.setFullScreen(true);
                    imagePopup.setHideCloseIcon(true);
                    imagePopup.setImageOnClickClose(true);
                    imagePopup.initiatePopup(imageView.getDrawable());
                    imagePopup.viewPopup();
                }
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (null != gallery.getImages() && !gallery.getImages().isEmpty()) {
                    showPopup(view, i);
                }
                return true;
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (null != gallery.getImages() && !gallery.getImages().isEmpty()) {
                    showPopup(view, i);
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (gallery != null) {
                    gallery.notifyDataSetChanged();
                    gridView.setAdapter(gallery);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment, new Fragment1());
                    transaction.commit();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }, 1000);
    }
}
