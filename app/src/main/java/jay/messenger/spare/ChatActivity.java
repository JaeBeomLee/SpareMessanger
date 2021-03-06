package jay.messenger.spare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

import jay.messenger.spare.RoomList.Chat;
import jay.messenger.spare.util.DateFormat;

import static jay.messenger.spare.util.Values.*;

public class ChatActivity extends AppCompatActivity {
    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView messageTextView;
        TextView messengerTextView;
        TextView timestampTextView;
//        ImageView messageImageView;


        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.message_text);
            messengerTextView = (TextView) itemView.findViewById(R.id.messenger_text);
            timestampTextView = (TextView) itemView.findViewById(R.id.timestemp_text);
//            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
        }
    }
    //UI init
    Button sendBtn;
    EditText messageText;
    private ProgressBar mProgressBar;

    //RecyclerView
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    //Firebase init
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<Chat, MessageViewHolder> mFirebaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Intent intent = getIntent();
        final String IDKey = intent.getStringExtra("IDKey");
        sendBtn = (Button)findViewById(R.id.send_btn);
        messageText = (EditText) findViewById(R.id.message_text);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.message_recycler);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference = mFirebaseDatabaseReference.child(CHILD_ROOMS).child(IDKey).child(CHILD_CHAT);

        final Date timeStampDate = Calendar.getInstance().getTime();
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chat chat = new Chat(messageText.getText().toString(), DateFormat.getTimeStampFormat(timeStampDate), UserInfo.getName());
                mFirebaseDatabaseReference.push().setValue(chat);
                messageText.setText("");
            }
        });


        mFirebaseAdapter = new FirebaseRecyclerAdapter<Chat, MessageViewHolder>(
                Chat.class,
                R.layout.item_message,
                MessageViewHolder.class,
                mFirebaseDatabaseReference) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Chat model, int position) {
                mProgressBar.setVisibility(View.INVISIBLE);

                viewHolder.messageTextView.setText(model.getText());
                viewHolder.messengerTextView.setText(model.getName());
                viewHolder.timestampTextView.setText(model.getTimeStamp());
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);

                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount -1) &&
                                lastVisiblePosition == (positionStart - 1))){
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }
}
