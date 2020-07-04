package com.example.expenses;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class DisplayGroupDataActivity extends AppCompatActivity {
    private RecyclerView groupDataRecyclerView;
    private RecyclerView.Adapter groupDataRecyclerViewAdaptor;
    private RecyclerView.LayoutManager groupDataLayoutManager;
    private static final int SIGN_IN_REQUEST_CODE = 10;
    private FirebaseAuth mAuth;
    private String groupId = "";
    private String userId = "";
    private String userName = "";
    private String timestamp;
    private DatabaseReference groupMembersRef = FirebaseDatabase.getInstance().getReference("groupmembers");
    private DatabaseReference groupDataRef = FirebaseDatabase.getInstance().getReference("groupdata");
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    //public static ProgressBar progressBar = null;
    List<GroupDataModel> groupData = new ArrayList<>();
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_data_options_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch(menuItem.getItemId())
        {
            case R.id.groupDataOptionsAddMember:
                AddMemberToGroup(this.getTitle().toString());
                return true;
            case R.id.groupDataOptionsLeaveGroup:
                DeleteAndExitGroup(this.getTitle().toString(), userId);
                return false;
            default:
                return false;
        }
    }
    public void DeleteAndExitGroup(String groupName, String userId)
    {
        MainActivity.usersRef.child(userId).child("grouplist").child(groupName).removeValue();
        //should not delete group if other members are using the group
        MainActivity.groupMembersRef.child(groupName).child("members").child(userId).removeValue();
        this.finish();
    }
    public void AddMemberToGroup(final String groupId)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.add_member_to_group_layout, null);
        final EditText newUserIdEditText = (EditText) customLayout.findViewById(R.id.newUserIdEditText);
        final EditText newUserNameEditText = (EditText) customLayout.findViewById(R.id.newUserNameEditText);
        builder.setView(customLayout);
        builder.setCancelable(true);
        builder.setPositiveButton("Add",null);
        //Creating dialog box
        final AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Enter the Details of new member");
        alert.show();
        Button addUserButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newUserId = newUserIdEditText.getText().toString();
                final String newUserName = newUserNameEditText.getText().toString();
                groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(groupId))
                        {
                            groupMembersRef.child(groupId).child("members").child(newUserId).setValue(newUserName);
                            usersRef.child(newUserId).child("grouplist").child(groupId).setValue(groupId);
                            Toast.makeText(getBaseContext(), "Member added successfully",Toast.LENGTH_SHORT).show();
                            alert.dismiss();
                        }
                        else
                        {
                            Toast.makeText(getBaseContext(), "Group does not exists", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getBaseContext(), databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                        Log.d("createGroup:", databaseError.getMessage());
                    }
                });
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle b = getIntent().getExtras();
        assert b != null;
        groupId = b.getString("groupId");
        userId = b.getString("userId");
        userName = b.getString("userName");
        super.onCreate(savedInstanceState);
        this.setTitle(groupId);
        this.setContentView(R.layout.activity_group_data);
        //progressBar = (ProgressBar) findViewById(R.id.groupDataProgressBar);

        //progressBar.setVisibility(View.VISIBLE);
    }
    @Override
    protected void onStart() {
        super.onStart();
        groupDataRecyclerView = (RecyclerView) findViewById(R.id.groupDataRecyclerView);
        groupDataRecyclerView.setHasFixedSize(true);
        groupDataLayoutManager = new LinearLayoutManager(this);
        groupDataRecyclerView.setLayoutManager(groupDataLayoutManager);
        mAuth = FirebaseAuth.getInstance();
        UpdateGroupData();
    }
    public void UpdateGroupData(){
        groupDataRef.child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupData = new ArrayList<>();
                //progressBar.setVisibility(View.VISIBLE);
                int children = 0;
                for (DataSnapshot data:dataSnapshot.child("expenses").getChildren()) {
                    children++;
                    AddGroupDataToList(data.getValue(GroupDataModel.class));
                }
                if(children==0)
                {
                    AddGroupDataToList(null);
                }
                //progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Toast.makeText(getBaseContext(), databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                Log.d("userRef onDataChange:", databaseError.getMessage());
            }
        });
    }
    public void AddGroupDataToList(GroupDataModel expenseData)
    {
        if(expenseData!=null) {
            groupData.add(expenseData);
            Log.d("ADDED DATA TO DICT", expenseData.desc);
            Log.d("DICTIONARY SIZE", Integer.toString(groupData.size()));
        }

        //add to list of groups in ui
        groupDataRecyclerViewAdaptor = new GroupDataRecyclerViewAdaptor(groupData, userId, groupId);
        groupDataRecyclerView.setAdapter(groupDataRecyclerViewAdaptor);
    }

    public void AddDataFABClick(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.add_data_layout, null);
        final EditText popupDesc = (EditText) customLayout.findViewById(R.id.popupDesc);
        final EditText popupValue = (EditText) customLayout.findViewById(R.id.popupValue);
        builder.setView(customLayout);
        builder.setCancelable(true);
        builder.setPositiveButton("Add", new
                DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(popupDesc.getText()==null || popupValue==null)
                            Toast.makeText(getBaseContext(),"Enter valid data", Toast.LENGTH_LONG).show();
                        else
                            AddDataToGroup(popupDesc.getText().toString(), popupValue.getText().toString());
                    }
                });

        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Add new expense");
        alert.show();
    }
    public void AddDataToGroup(String desc, String value)
    {
        //progressBar.setVisibility(View.VISIBLE);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC+5:30"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("dd-MM-yyyy HH:mm a", Locale.UK);
        date.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
        timestamp = date.format(currentLocalTime);
        String pushId = groupDataRef.child(groupId).child("expenses").push().getKey();
        GroupDataModel newExpenseData = new GroupDataModel(pushId, userId, userName, desc, value, timestamp);
        groupDataRef.child(groupId).child("expenses").child(pushId).setValue(newExpenseData);
        //progressBar.setVisibility(View.GONE);
    }
}
