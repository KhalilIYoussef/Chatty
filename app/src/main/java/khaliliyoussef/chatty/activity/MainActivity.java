
package khaliliyoussef.chatty.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import khaliliyoussef.chatty.R;
import khaliliyoussef.chatty.adapter.MessageAdapter;
import khaliliyoussef.chatty.model.ChattyMessage;

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    @BindView(R.id.messageListView) RecyclerView mMessageRecyclerView;
     @BindView(R.id.progressBar) ProgressBar mProgressBar;
   @BindView(R.id.photoPickerButton) ImageButton mPhotoPickerButton;
     @BindView(R.id.messageEditText) EditText mMessageEditText;
    @BindView(R.id.sendButton) Button mSendButton;
    private MessageAdapter mMessageAdapter;
    private List<ChattyMessage> chattyMessages;
    private String mUsername;
    //referencing the entry point for the database
    private FirebaseDatabase mFirebaseDatabase;
    //Message DatabaseReference
    private DatabaseReference mMessaageDatabaseReference;
    // Child event listener to read from the database
    private ChildEventListener mChildEventListener;
    //TODO authentication for firenbase
    //for authentication must get an instance
    private FirebaseAuth mFirebaseAuth;
    //listener to differentiate between signed in and not signed in
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mUsername = ANONYMOUS;
        //get a reference to the root node
        mFirebaseDatabase =FirebaseDatabase.getInstance();
        //get preference to a specific part of the database
        mMessaageDatabaseReference=mFirebaseDatabase.getReference().child("messages");

        //initialize the auth object
        mFirebaseAuth=FirebaseAuth.getInstance();

        // Initialize message RecyclerView and its adapter
         chattyMessages = new ArrayList<>();

        mMessageAdapter = new MessageAdapter(this, chattyMessages);
        mMessageRecyclerView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // TODO: Fire an intent to show an image picker
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click
                ChattyMessage chattyMessage = new ChattyMessage(mMessageEditText.getText().toString(), mUsername, null);

                //notice setValue only takes a single object that's why we made a class for messages
                mMessaageDatabaseReference.push().setValue(chattyMessage);


                // Clear input box
                mMessageEditText.setText("");

            }
        });

        mAuthStateListener=new FirebaseAuth.AuthStateListener() {

            //the parameter in here is garented to contain whether the user is signe din or no at this moment
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mUser = firebaseAuth.getCurrentUser();
                //show if it's logged in or not
                if(mUser!=null)
                {
                    //user logged in
                    Toast.makeText(MainActivity.this, "Well Hello You Are signed in", Toast.LENGTH_SHORT).show();
                    onSignedInIntitialize(mUser.getDisplayName());
                }
                else
                {
                    onSingedOutCleanUp();
                    //user is logged out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(
                                            AuthUI.EMAIL_PROVIDER,
                                            AuthUI.GOOGLE_PROVIDER
                                    )
                                    .build(),
                            RC_SIGN_IN);

                }
            }
        };
    }

    private void onSingedOutCleanUp() {
        //unset the username
        mUsername = ANONYMOUS;
        //clear the messages
        chattyMessages.clear();
        //detatch the listener
        if (mChildEventListener != null) {
            mMessaageDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener=null;
        }

    }

    private void onSignedInIntitialize(String displayName) {
        mUsername=displayName;
        if (mChildEventListener==null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //when we add a child to our database
                    //when a new message is inserted & when the listener is attached (called twice)
                    ChattyMessage mMessage = dataSnapshot.getValue(ChattyMessage.class);
                    chattyMessages.add(mMessage);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    //when we change the value of a database
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    //when remove a child
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    //when moving a child from one position to another
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //error happened or don't have permission to read the data

                }
            };
            //please notice not to attach a listener to the root directory of the database
            //listen to the massages location and here is what will happen
            mMessaageDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN)
        {
            if (resultCode==RESULT_OK)
            {
                Toast.makeText(this, " signed in", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode==RESULT_CANCELED)
            {
                Toast.makeText(this, "Signing in  cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //pass the listener to the authentication object
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //detatch the listener
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        //detatch the read listener
        if (mChildEventListener != null) {
            mMessaageDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener=null;
        }
        //clear the messages
        chattyMessages.clear();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//code fo loggung out of the app
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
