package com.firebase.devday;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.devday.helpers.MyHelper;
import com.firebase.devday.models.Topic;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailActivity extends AppCompatActivity {
	private DatabaseReference mTopicRef;
	private ImageView mImageView;
	private TextView mTextView;
	private ValueEventListener valueEventListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);

		mImageView = findViewById(R.id.picture);
		mTextView = findViewById(R.id.txt);

		String topicKey = getIntent().getStringExtra("topicKey");

		DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
		mTopicRef = mDatabase.child("topics").child(topicKey);
	}

	@Override
	protected void onStart() {
		super.onStart();
		MyHelper.showDialog(this);
		valueEventListener = new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				MyHelper.dismissDialog();
				Topic topic = dataSnapshot.getValue(Topic.class);
				updateView(topic);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				MyHelper.dismissDialog();
			}
		};
		mTopicRef.addValueEventListener(valueEventListener);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (valueEventListener != null) {
			mTopicRef.removeEventListener(valueEventListener);
		}
	}

	private void updateView(Topic topic) {
		Glide.with(this).load(topic.picture).into(mImageView);
		mTextView.setText(topic.title);
		mTextView.append("\n\n" + topic.speaker);
		mTextView.append("\n\n" + topic.body);
		mTextView.append("\n\nVote " + topic.voteCount);
	}
}