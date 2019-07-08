package com.example.serverapp.Fragment0;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serverapp.R;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private Context context;
    private ArrayList<AddressItem> persons;
    public ContactAdapter(Context mContext, ArrayList<AddressItem> data) {
        context = mContext;
        persons = data;
    }

    private ItemClick itemClick;
    public interface ItemClick {
        void onClick(View view,int position);
    }

    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        public Context context;
        public View totalView;
        public TextView nameView;
        public TextView numberView;
        public ImageView profileView;
        public ContactViewHolder(View v, Context mContext) {
            super(v);
            context = mContext;
            totalView = v;
            nameView = v.findViewById(R.id.nameText);
            numberView = v.findViewById(R.id.numberText);
            profileView = v.findViewById(R.id.profileView);
        }
    }

    @Override
    public int getItemCount() {
        return persons.size();
    }

    public ArrayList<AddressItem> getData() { return persons; }

    @NonNull
    @Override
    public ContactAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                               int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        ContactViewHolder vh = new ContactViewHolder(v, context);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, final int position) {
        final int Position = position;
        holder.nameView.setText(persons.get(Position).getName());
        holder.numberView.setText(persons.get(Position).getNumber());
        holder.profileView.setImageResource(persons.get(Position).getImageID());
        holder.totalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != itemClick) {
                    itemClick.onClick(view, Position);
                }
            }
        });
    }
}
