package com.firebase.devday;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.firebase.devday.helpers.MyHelper;
import com.firebase.devday.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReadWriteActivity extends AppCompatActivity implements View.OnClickListener {
	private static final String USER_ID = "6uvSd7wRhrYc1BkgC4whL8HwuCi2";
	private DatabaseReference mDatabase, mUsersRef;
	private TextView mTextView;
	private ValueEventListener valueEventListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_write);

		mTextView = (TextView) findViewById(R.id.txt);
		findViewById(R.id.btn_set_single).setOnClickListener(this);
		findViewById(R.id.btn_set_object).setOnClickListener(this);
		findViewById(R.id.btn_push).setOnClickListener(this);

		mDatabase = FirebaseDatabase.getInstance().getReference();
		mUsersRef = mDatabase.child("users").child(USER_ID);
	}

	@Override
	protected void onStart() {
		super.onStart();
		MyHelper.showDialog(this);
		valueEventListener = new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				MyHelper.dismissDialog();
				User user = dataSnapshot.getValue(User.class);
				updateView(user);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				MyHelper.dismissDialog();
			}
		};
		mUsersRef.addValueEventListener(valueEventListener);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (valueEventListener != null) {
			mUsersRef.removeEventListener(valueEventListener);
		}
	}

	@Override
	public void onClick(View view) {
		User user = new User(
				"firebasethailand@gmail.com",
				"Firebaser",
				"https://pbs.twimg.com/profile_images/744165523978493952/02eZ4I34.jpg",
				true
		);
		switch (view.getId()) {
			case R.id.btn_set_single:
				mUsersRef.child("email").setValue("firebasethailand@gmail.com");
				break;
			case R.id.btn_set_object:
				mUsersRef.setValue(user);
				break;
			case R.id.btn_push:
				mDatabase.child("users").push().setValue(user);
				break;
		}
	}

	private void updateView(User user) {
		mTextView.setText(user.email);
		mTextView.append("\n" + user.speaker);
		mTextView.append("\n" + user.isAdmin);
	}
}