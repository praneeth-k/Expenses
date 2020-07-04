package com.example.expenses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GroupListRecyclerViewAdaptor extends  RecyclerView.Adapter<GroupListRecyclerViewAdaptor.MyViewHolder> {
    public List<String> allGroups = new ArrayList<>();
    private String userId, userName;
    private Context context;
    public TextView groupTextView;
    public GroupListRecyclerViewAdaptor(List<String> data, String userId, String userName, Context context) {
        this.allGroups = data;
        this.userId = userId;
        this.userName = userName;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        final CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_list_item_layout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(cardView);

        return viewHolder;
    }



    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.groupNameTextView.setText(allGroups.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Bundle extras = new Bundle();
                    groupTextView = view.findViewById(R.id.groupName);
                    extras.putString("groupId", (String) groupTextView.getText());
                    extras.putString("userId", userId);
                    extras.putString("userName", userName);
                    Intent intent = new Intent((Activity)context, DisplayGroupDataActivity.class);
                    intent.putExtras(extras);
                    ((Activity)context).startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onLongClick(View view) {
                Log.d("Group List Item","On long click listener");
                groupTextView = view.findViewById(R.id.groupName);
                PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.group_long_click_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(
                        new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                Log.d("MENU ITEM", Integer.toString(menuItem.getItemId()));
                                switch (menuItem.getItemId()) {
                                    case R.id.deleteGroup:
                                        try {
                                            MainActivity.usersRef.child(userId).child("grouplist").child(groupTextView.getText().toString()).removeValue();
                                            //should not delete group if other members are using the group
                                            MainActivity.groupMembersRef.child(groupTextView.getText().toString()).child("members").child(userId).removeValue();
                                        }
                                        catch (Exception e)
                                        {
                                            Toast.makeText(holder.itemView.getContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        }
                );
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return allGroups.size();
    }

    public static class MyViewHolder extends  RecyclerView.ViewHolder{
        public TextView groupNameTextView;
        public MyViewHolder(final CardView cardView)
        {
            super(cardView);
            groupNameTextView = cardView.findViewById(R.id.groupName);

        }
    }
}
