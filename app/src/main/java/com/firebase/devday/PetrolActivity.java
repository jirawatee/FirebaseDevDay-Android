package com.firebase.devday;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.firebase.devday.helpers.MyHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class PetrolActivity extends AppCompatActivity {
	private Query mQuery;
	private TextView mTextView;
	private ValueEventListener valueEventListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_petrol);
		mTextView = findViewById(R.id.txt);
		DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
		mQuery = mDatabase.child("Petrol").orderByValue();
	}

	@Override
	protected void onStart() {
		super.onStart();
		MyHelper.showDialog(this);
		valueEventListener = new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				MyHelper.dismissDialog();
				Iterable<DataSnapshot> children = dataSnapshot.getChildren();
				while(children.iterator().hasNext()) {
					String key = children.iterator().next().getKey();
					mTextView.append(dataSnapshot.getKey());
					mTextView.append(": " + dataSnapshot.child(key).getValue(Double.class));
					mTextView.append("\n");

					Log.d("Petrol", dataSnapshot.getKey() + ": " + dataSnapshot.child(key).getValue(Double.class));
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				MyHelper.dismissDialog();
			}
		};
		mQuery.addValueEventListener(valueEventListener);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (valueEventListener != null) {
			mQuery.removeEventListener(valueEventListener);
		}
	}
}