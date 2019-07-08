package com.example.serverapp.Fragment0;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.serverapp.MainActivity;
import com.example.serverapp.R;
import com.example.serverapp.Retrofit.IAppService;
import com.example.serverapp.Retrofit.RetrofitClient;
import com.example.serverapp.SwipeController;
import com.example.serverapp.SwipeControllerActions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class Fragment0 extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private String ownerEmail;
    private int profileIndex = 0;
    private final int EDIT_CONTACT = 1;
    private final int ADD_CONTACT = 2;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ContactAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<AddressItem> addressList = new ArrayList<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IAppService iAppService;
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    public Integer[] images = new Integer[] {
            R.drawable.adam, R.drawable.anjali,
            R.drawable.arjun, R.drawable.jorge,
            R.drawable.maya, R.drawable.rahul,
            R.drawable.sadona, R.drawable.sandy,
            R.drawable.sid, R.drawable.steve
    };

    private SwipeController swipeController = new SwipeController(new SwipeControllerActions() {
        @Override
        public void onRightClicked(int position) {
            AddressItem delete_item = new AddressItem(addressList.get(position).getName(),
                    addressList.get(position).getNumber(),
                    addressList.get(position).getEmail(),
                    addressList.get(position).getImageID());
            addressList.remove(position);
            mAdapter.notifyItemRemoved(position);
            compositeDisposable.add(iAppService.deletePerson(delete_item.getName(), delete_item.getNumber(), delete_item.getEmail(), ((MainActivity) getActivity()).getOwnerEmail())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String data) throws Exception {
                            Log.e("DELETE", data.replace("\"", ""));
                        }
                    }));
        }

        @Override
        public void onLeftClicked(int position) {
            Intent detail_intent = new Intent(getActivity(), AddressEdit.class);
            detail_intent.putExtra("position", position);
            detail_intent.putExtra("name", addressList.get(position).getName());
            detail_intent.putExtra("number", addressList.get(position).getNumber());
            detail_intent.putExtra("email", addressList.get(position).getEmail());
            detail_intent.putExtra("imageID", addressList.get(position).getImageID());
            startActivityForResult(detail_intent, EDIT_CONTACT);
        }
    });

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    public Fragment0() {
        // Empty Public Constructor
    }

    private void increaseProfileIndex() {
        if (profileIndex == 9) {
            profileIndex = 0;
        } else {
            profileIndex++;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_0, container, false);
        ownerEmail = ((MainActivity) getActivity()).getOwnerEmail();
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        Retrofit retrofitClient = RetrofitClient.getInstance();
        iAppService = retrofitClient.create(IAppService.class);
        layoutManager = new LinearLayoutManager(getContext());
        ((LinearLayoutManager) layoutManager).setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
        mAdapter = new ContactAdapter(getContext(), addressList);
        mAdapter.setItemClick(new ContactAdapter.ItemClick() {
            @Override
            public void onClick(View view, int position) {
                Intent dial_intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + addressList.get(position).getNumber()));
                startActivity(dial_intent);
            }
        });
        recyclerView.setAdapter(mAdapter);

        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        fab = view.findViewById(R.id.contactFab);
        fab1 = view.findViewById(R.id.contactFab1);
        fab2 = view.findViewById(R.id.contactFab2);
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
                Intent detail_intent = new Intent(getActivity(), AddressEdit.class);
                detail_intent.putExtra("position", -1);
                detail_intent.putExtra("name", "name");
                detail_intent.putExtra("number", "01012345678");
                detail_intent.putExtra("email", "test@test.com");
                detail_intent.putExtra("owner_email", ownerEmail);
                detail_intent.putExtra("imageID", images[profileIndex]);
                increaseProfileIndex();
                startActivityForResult(detail_intent, ADD_CONTACT);
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncList();
            }
        });

        compositeDisposable.add(iAppService.updatePerson(((MainActivity) getActivity()).getOwnerEmail())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String data) throws Exception {
                        if (!data.isEmpty()) {
                            String jsonString = "{\"data\": " + data + " }";
                            JsonParser jsonParser = new JsonParser();
                            addressList.clear();
                            mAdapter.notifyDataSetChanged();
                            Log.e("Refresh", jsonString);
                            JsonObject jsonObject = (JsonObject) jsonParser.parse(jsonString);
                            JsonArray memberArray = (JsonArray) jsonObject.get("data");
                            try {
                                JsonObject testObject = (JsonObject) memberArray.get(0);
                            } catch (OnErrorNotImplementedException ex) {
                                memberArray = (JsonArray) memberArray.get(0);
                            }

                            for (int i = 0; i < memberArray.size(); i++) {
                                JsonObject tempObject = (JsonObject) memberArray.get(i);
                                String nameString = tempObject.get("name").toString().replace("\"", "");
                                String numberString = tempObject.get("number").toString().replace("\"", "");
                                String emailString = tempObject.get("email").toString().replace("\"", "");
                                AddressItem item = new AddressItem(nameString, numberString, emailString, images[profileIndex]);
                                increaseProfileIndex();
                                addressList.add(item);
                                mAdapter.notifyItemInserted(mAdapter.getData().size() - 1);
                            }
                        }
                    }
                }));

        mAdapter.notifyDataSetChanged();
        return view;
    }

    public void syncList() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
        };
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor contactCursor = getActivity().getContentResolver().query(uri, projection, null, null, sortOrder);

        if (contactCursor.moveToFirst()) {
            compositeDisposable.add(iAppService.removeAll(((MainActivity) getActivity()).getOwnerEmail())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String data) throws Exception {
                            Log.e("REMOVE_ALL", data.replace("\"", ""));
                        }
                    }));
            do {
                AddressItem new_item;
                addressList.clear();
                mAdapter.notifyDataSetChanged();
                String[] data = new String[3];
                data[0] = contactCursor.getString(1); // Name
                data[1] = contactCursor.getString(0).replace("-", "").replace("?","");
                if (data[1].startsWith("//")) {
                    data[1] = data[1].substring(2);
                } else if (data[1].startsWith("+82")) {
                    data[1] = "0" + data[1].substring(3);
                } // Number
                data[2] = contactCursor.getString(2); // Address
                if (data[2].equals(contactCursor.getString(0))) {
                    new_item = new AddressItem(data[0], data[1], "test@test.com", images[profileIndex]);
                } else {
                    new_item = new AddressItem(data[0], data[1], data[2], images[profileIndex]);
                }
                increaseProfileIndex();
                addressList.add(new_item);
                mAdapter.notifyItemInserted(mAdapter.getData().size() - 1);
                compositeDisposable.add(iAppService.addPerson(new_item.getName(), new_item.getNumber(), new_item.getEmail(), ((MainActivity) getActivity()).getOwnerEmail())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String data) throws Exception {
                                Log.e("ADD", data.replace("\"", ""));
                            }
                        }));
            } while (contactCursor.moveToNext());
        }
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
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EDIT_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    int position = data.getIntExtra("position", -1);
                    String new_name = data.getStringExtra("name");
                    String new_number = data.getStringExtra("number");
                    String new_email = data.getStringExtra("email");
                    if (position >= 0) {
                        AddressItem edit_item = new AddressItem(new_name,
                                new_number,
                                new_email,
                                images[profileIndex]);
                        increaseProfileIndex();
                        AddressItem original_item = addressList.get(position);
                        addressList.set(position, edit_item);
                        mAdapter.notifyItemChanged(position);
                        compositeDisposable.add(iAppService.editPerson(original_item.getName(), original_item.getNumber(), original_item.getEmail(),
                                edit_item.getName(), edit_item.getNumber(), edit_item.getEmail(),
                                ((MainActivity) getActivity()).getOwnerEmail())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<String>() {
                                    @Override
                                    public void accept(String data) throws Exception {
                                        Log.e("EDIT", data.replace("\"", ""));
                                    }
                                }));
                    }
                }
                break;
            case ADD_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    String new_name = data.getStringExtra("name");
                    String new_number = data.getStringExtra("number");
                    String new_email = data.getStringExtra("email");
                    AddressItem new_item = new AddressItem(new_name,
                            new_number,
                            new_email,
                            images[profileIndex]);
                    increaseProfileIndex();
                    addressList.add(new_item);
                    mAdapter.notifyItemInserted(mAdapter.getData().size() - 1);
                    mAdapter.notifyDataSetChanged();
                    compositeDisposable.add(iAppService.addPerson(new_item.getName(), new_item.getNumber(), new_item.getEmail(), ((MainActivity) getActivity()).getOwnerEmail())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<String>() {
                                @Override
                                public void accept(String data) throws Exception {
                                    Log.e("ADD", data.replace("\"", ""));
                                }
                            }));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                compositeDisposable.add(iAppService.updatePerson(((MainActivity) getActivity()).getOwnerEmail())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String data) throws Exception {
                                if (data != null) {
                                    String jsonString = "{\"images\": " + data + " }";
                                    JsonParser jsonParser = new JsonParser();
                                    addressList.clear();
                                    mAdapter.notifyDataSetChanged();
                                    JsonObject jsonObject = (JsonObject) jsonParser.parse(jsonString);
                                    JsonArray memberArray = (JsonArray) jsonObject.get("images");
                                    try {
                                        JsonObject testObject = (JsonObject) memberArray.get(0);
                                    } catch (OnErrorNotImplementedException ex) {
                                        memberArray = (JsonArray) memberArray.get(0);
                                    }

                                    for (int i = 0; i < memberArray.size(); i++) {
                                        JsonObject tempObject = (JsonObject) memberArray.get(i);
                                        String nameString = tempObject.get("name").toString().replace("\"", "");
                                        String numberString = tempObject.get("number").toString().replace("\"", "");
                                        String emailString = tempObject.get("email").toString().replace("\"", "");
                                        AddressItem item = new AddressItem(nameString, numberString, emailString, images[profileIndex]);
                                        increaseProfileIndex();
                                        addressList.add(item);
                                        mAdapter.notifyItemInserted(mAdapter.getData().size() - 1);
                                    }
                                }
                            }
                        }));
                mAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }
}
