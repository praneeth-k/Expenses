package com.example.expenses;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupDataRecyclerViewAdaptor extends  RecyclerView.Adapter<GroupDataRecyclerViewAdaptor.MyViewHolder> {
    private List<GroupDataModel> groupData = new ArrayList<>();
    private String currentUserId="", groupId = "";
    public GroupDataRecyclerViewAdaptor(List<GroupDataModel> data, String currentUserId, String groupId) {
        this.groupData = data;
        this.groupId = groupId;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_data_item_layout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(cardView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        //TextView userId = holder.groupDataUserId;
        TextView userName = holder.groupDataUserName;
        TextView desc = holder.groupDataDesc;
        TextView value = holder.groupDataValue;
        TextView timestamp = holder.groupDataTimestamp;
        //userId.setText(groupData.get(position).userId);
        userName.setText(groupData.get(position).userName);
        desc.setText(groupData.get(position).desc);
        value.setText(groupData.get(position).value);
        timestamp.setText(groupData.get(position).timestamp);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d("Group Data Item","On long click listener");
                PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.group_data_long_click_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Log.d("MENU ITEM", Integer.toString(menuItem.getItemId()));
                        if(menuItem.getItemId() == R.id.groupDataItemDelete)
                        {
                            MainActivity.groupDataRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.child("expenses").child(groupData.get(position).key).child("userId").getValue(String.class)
                                            .equals(currentUserId)){
                                        MainActivity.groupDataRef.child(groupId).child("expenses").child(groupData.get(position).key).removeValue();
                                    }
                                    else {
                                        Toast.makeText(holder.itemView.getContext(),
                                                "Data created by different user cannot be deleted", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("Delete Group data item",databaseError.getMessage());
                                }
                            });
                        }
                        return false;
                    }
                });
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupData.size();
    }

    public static class MyViewHolder extends  RecyclerView.ViewHolder{
        //public TextView groupDataUserId;
        public TextView groupDataUserName;
        public TextView groupDataDesc;
        public TextView groupDataValue;
        public TextView groupDataTimestamp;
        public MyViewHolder(View itemView)
        {
            super(itemView);
            //groupDataUserId = (TextView)itemView.findViewById(R.id.groupDataUserId);
            groupDataUserName = (TextView)itemView.findViewById(R.id.groupDataUserName);
            groupDataDesc = (TextView)itemView.findViewById(R.id.groupDataDesc);
            groupDataValue = (TextView)itemView.findViewById(R.id.groupDataValue);
            groupDataTimestamp = (TextView)itemView.findViewById(R.id.groupDataTime);
        }
    }
}
