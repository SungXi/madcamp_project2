package com.example.serverapp.Fragment0;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class Fragment0 extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private String ownerEmail;
    private int profileIndex = 0;
    private final int EDIT_CONTACT = 1;
    private final int ADD_CONTACT = 2;
    private boolean use_local = true;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ContactAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<AddressItem> addressList = new ArrayList<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IAppService iAppService;
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
                            // Empty Handler
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
                Intent detail_intent = new Intent(getActivity(), AddressEdit.class);
                detail_intent.putExtra("position", position);
                detail_intent.putExtra("name", addressList.get(position).getName());
                detail_intent.putExtra("number", addressList.get(position).getNumber());
                detail_intent.putExtra("email", addressList.get(position).getEmail());
                detail_intent.putExtra("owner_email", ownerEmail);
                startActivityForResult(detail_intent, EDIT_CONTACT);
            }
        });
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = view.findViewById(R.id.contactFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detail_intent = new Intent(getActivity(), AddressEdit.class);
                detail_intent.putExtra("position", -1);
                detail_intent.putExtra("name", "name");
                detail_intent.putExtra("number", "010-0000-0000");
                detail_intent.putExtra("email", "test@test.com");
                detail_intent.putExtra("owner_email", ownerEmail);
                detail_intent.putExtra("imageID", images[profileIndex]);
                increaseProfileIndex();
                startActivityForResult(detail_intent, ADD_CONTACT);
            }
        });

        compositeDisposable.add(iAppService.updatePerson(((MainActivity) getActivity()).getOwnerEmail())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String data) throws Exception {
                        if (data != null) {
                            String jsonString = "{\"images\":" + data + "}";
                            JsonParser jsonParser = new JsonParser();
                            addressList.clear();
                            mAdapter.notifyDataSetChanged();
                            JsonObject jsonObject = (JsonObject) jsonParser.parse(jsonString);
                            JsonArray memberArray = (JsonArray) jsonObject.get("images");
                            Log.e("Refresh", jsonString);

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
                        Log.e("After edit", mAdapter.getItemCount() + ":" + mAdapter.getData().get(position).getName());
                        mAdapter.notifyItemChanged(position);
                        compositeDisposable.add(iAppService.editPerson(original_item.getName(), original_item.getNumber(), original_item.getEmail(),
                                edit_item.getName(), edit_item.getNumber(), edit_item.getEmail(),
                                ((MainActivity) getActivity()).getOwnerEmail())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<String>() {
                                    @Override
                                    public void accept(String data) throws Exception {
                                        // Empty Handler
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
                    Log.e("After add", mAdapter.getItemCount() + ":" + mAdapter.getData().get(mAdapter.getData().size() - 1).getName());
                    mAdapter.notifyItemInserted(mAdapter.getData().size() - 1);
                    mAdapter.notifyDataSetChanged();
                    compositeDisposable.add(iAppService.addPerson(new_item.getName(), new_item.getNumber(), new_item.getEmail(), ((MainActivity) getActivity()).getOwnerEmail())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<String>() {
                                @Override
                                public void accept(String data) throws Exception {
                                    // Empty Handler
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
                                    String jsonString = "{\"images\":" + data + "}";
                                    JsonParser jsonParser = new JsonParser();
                                    addressList.clear();
                                    mAdapter.notifyDataSetChanged();
                                    JsonObject jsonObject = (JsonObject) jsonParser.parse(jsonString);
                                    JsonArray memberArray = (JsonArray) jsonObject.get("images");

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
