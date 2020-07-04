package com.example.expenses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private RecyclerView groupsRecyclerView;
    private RecyclerView.Adapter groupsRecyclerViewAdaptor;
    private RecyclerView.LayoutManager groupsLayoutManager;
    private static final int SIGN_IN_REQUEST_CODE = 0;
    private FirebaseAuth mAuth;
    private String groupId = "";
    private String userId = "";
    private String userEmail = "";
    private String userName = "";
    private String isGroupExist = "False";
    public static DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    public static DatabaseReference groupMembersRef = FirebaseDatabase.getInstance().getReference("groupmembers");
    public static DatabaseReference groupDataRef = FirebaseDatabase.getInstance().getReference("groupdata");
    List<String> userGroups = new ArrayList<>();
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_list_options_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch(menuItem.getItemId())
        {
            case R.id.groupListOptionsCreateGroup:
                CreateNewGroup();
                return true;
            case R.id.groupListOptionsMyProfile:
                ViewMyProfile();
                return true;
            case R.id.groupListOptionsLogOut:
                LogOut();
                this.recreate();
            default:
                return false;
        }
    }
    private void ViewMyProfile(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.my_profile_layout, null);
        final TextView profileIdTextView = (TextView) customLayout.findViewById(R.id.profileIdTextView);
        final TextView profileNameTextView = (TextView) customLayout.findViewById(R.id.profileNameTextView);
        final TextView profileEmailTextView = (TextView) customLayout.findViewById(R.id.profileEmailTextView);
        profileIdTextView.setText(userId);
        profileNameTextView.setText(userName);
        profileEmailTextView.setText(userEmail);
        builder.setView(customLayout);
        builder.setCancelable(true);
        builder.setPositiveButton("Ok",null);
        //Creating dialog box
        final AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Your Profile");
        alert.show();
    }
    private void CreateNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.create_group_layout, null);
        final EditText newGroupEditText = (EditText) customLayout.findViewById(R.id.newGroupName);
        builder.setView(customLayout);
        builder.setCancelable(true);
        builder.setPositiveButton("Create",null);
        //Creating dialog box
        final AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Enter a unique group name");
        alert.show();
        Button createGroupButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newGroupName = newGroupEditText.getText().toString();
                groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(newGroupName))
                        {
                            Toast.makeText(getBaseContext(), "Group already exists", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            groupMembersRef.child(newGroupName).child("members").child(userId).setValue(userName);
                            usersRef.child(userId).child("grouplist").child(newGroupName).setValue(newGroupName);
                            Toast.makeText(getBaseContext(), "Group successfully created",Toast.LENGTH_SHORT).show();
                            alert.dismiss();
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Groups");

        groupsRecyclerView = (RecyclerView)findViewById(R.id.groupListRecyclerView);
        groupsRecyclerView.setHasFixedSize(true);
        groupsLayoutManager = new LinearLayoutManager(this);
        groupsRecyclerView.setLayoutManager(groupsLayoutManager);
        mAuth = FirebaseAuth.getInstance();
        UserSignIn();
    }

    public void UpdateUserDetails(){
        userName = Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName();
        Toast.makeText(this, "Welcome "+userName, Toast.LENGTH_SHORT).show();
        userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        UpdateGroupList();
    }
    public void UpdateGroupList()
    {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //update username if not exists
                if(!dataSnapshot.child(userId).child("username").exists())
                {
                    usersRef.child(userId).child("username").setValue(userName);
                }
                int children = 0;
                userGroups = new ArrayList<>();
                for (DataSnapshot data: dataSnapshot.child(userId).child("grouplist").getChildren()) {
                    children++;
                    AddGroupIdToList(data.getValue(String.class));
                }
                if (children==0)
                {
                    AddGroupIdToList(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("userRef onDataChange:", databaseError.getMessage());
            }
        });
    }
    public void AddGroupIdToList(String data)
    {
        if(data!=null) {
            userGroups.add(data);
            Log.d("AddGroupIdToList", data);
            Log.d("LIST SIZE", Integer.toString(userGroups.size()));
        }
        else
        {
            Log.d("AddGroupIdToList", "data is null");
            Log.d("LIST SIZE", Integer.toString(userGroups.size()));
        }
        //add to list of groups in ui
        groupsRecyclerViewAdaptor = new GroupListRecyclerViewAdaptor(userGroups, userId, userName, this);
        groupsRecyclerView.setAdapter(groupsRecyclerViewAdaptor);
    }
    public void LogOut()
    {
        FirebaseAuth.getInstance().signOut();
    }
    public void UserSignIn()
    {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
            //usersRef.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child("username").setValue(mAuth.getCurrentUser().getDisplayName());
        }
        else {
            UpdateUserDetails();
//            if(userGroups.isEmpty())
//            {
//                //Alert JoinOrCreateGroup
//                Toast.makeText(this, "JoinOrCreateGroup", Toast.LENGTH_SHORT).show();
//            }
//            else
//            {
//                //foreach group display data
//                Toast.makeText(this, "you are involved in " + userGroups.size() + " groups", Toast.LENGTH_SHORT).show();
//            }
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK)
            {
                UpdateUserDetails();
            }
            else
            {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Toast.makeText(this, "Please SignIn to your account", Toast.LENGTH_SHORT).show();
                UserSignIn();
            }
        }
    }


}
