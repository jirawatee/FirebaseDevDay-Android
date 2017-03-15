package com.firebase.devday;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.devday.helpers.MyHelper;
import com.firebase.devday.models.Topic;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListActivity extends AppCompatActivity implements View.OnClickListener{
	private static final String TAG = "ListActivity";
	private Query mQuery;
	private RecyclerView mRecycler;
	private TopicAdapter mAdapter;
	private DatabaseReference mDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_all_topics);
		findViewById(R.id.fab).setOnClickListener(this);

		mRecycler = (RecyclerView) findViewById(R.id.topic_list);
		mRecycler.setHasFixedSize(true);
		LinearLayoutManager mManager = new LinearLayoutManager(this);
		//mManager.setReverseLayout(true);
		//mManager.setStackFromEnd(true);
		mRecycler.setLayoutManager(mManager);

		mDatabase = FirebaseDatabase.getInstance().getReference();

		/* Ordering */
		mQuery = mDatabase.child("topics");
		// orderByKey()
		//mQuery = mQuery.orderByChild("voteCount");


		/* Filtering */
		//mQuery = mQuery.limitToFirst(3);
		//mQuery = mQuery.limitToLast(3);
		//mQuery = mQuery.equalTo("Firebase Dev Day BKK");
		//mQuery = mQuery.startAt("Firebase Realtime Database");
		//mQuery = mQuery.startAt(50);
		//mQuery = mQuery.endAt(50);
		//mQuery = mQuery.startAt(50).limitToFirst(2);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mAdapter = new TopicAdapter(this, mQuery);
		mRecycler.setAdapter(mAdapter);
	}

	@Override
	public void onStop() {
		super.onStop();
		mAdapter.cleanupListener();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.fab:
				FirebaseUser firebaser = FirebaseAuth.getInstance().getCurrentUser();

				String uid = firebaser.getUid();
				String avatar = firebaser.getPhotoUrl().toString();
				String speaker = "Jirawatee";
				String picture = "https://pbs.twimg.com/media/C4yDZ6QUkAEJ0ZW.jpg";
				String title = "Hello World!";
				String body = "Geek Alert!";

				final Topic topic = new Topic(uid, speaker, avatar, title, body, picture);
				Map<String, Object> topicValues = topic.toMap();
				Map<String, Object> childUpdates = new HashMap<>();

				String topicKey = mDatabase.push().getKey();
				childUpdates.put("/topics/" + topicKey, topicValues);
				childUpdates.put("/user-topics/" + uid + "/" + topicKey, topicValues);

				MyHelper.showDialog(this);
				mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
					@Override
					public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
						MyHelper.dismissDialog();
						if (databaseError != null) {
							Toast.makeText(ListActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(ListActivity.this, "Added!", Toast.LENGTH_SHORT).show();
						}
					}
				});
				break;
		}
	}

	private static class TopicViewHolder extends RecyclerView.ViewHolder {
		ImageView avatar;
		TextView titleView, speakerView, voteView;
		TopicViewHolder(View itemView) {
			super(itemView);
			avatar = (ImageView) itemView.findViewById(R.id.avatar);
			titleView = (TextView) itemView.findViewById(R.id.title);
			speakerView = (TextView) itemView.findViewById(R.id.speaker);
			voteView = (TextView) itemView.findViewById(R.id.vote);
		}
	}

	private class TopicAdapter extends RecyclerView.Adapter<TopicViewHolder> {
		private Context mContext;
		private Query mDatabaseReference;
		private ValueEventListener mValueEventListener;
		private ChildEventListener mChildEventListener;
		private List<String> mTopicIds = new ArrayList<>();
		private List<Topic> mTopics = new ArrayList<>();

		TopicAdapter(Context context, Query query) {
			mContext = context;
			mDatabaseReference = query;
			MyHelper.showDialog(mContext);
			mChildEventListener = new ChildEventListener() {
				@Override
				public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
					MyHelper.dismissDialog();

					Topic topic = dataSnapshot.getValue(Topic.class);
					Log.d("onChildAdded", topic.voteCount + ": " + topic.title);

					mTopicIds.add(dataSnapshot.getKey());
					mTopics.add(topic);
					notifyItemInserted(mTopics.size() - 1);
				}

				@Override
				public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
					MyHelper.dismissDialog();

					Topic newTopic = dataSnapshot.getValue(Topic.class);
					Log.d("onChildChanged", newTopic.title);

					String topicKey = dataSnapshot.getKey();
					int topicIndex = mTopicIds.indexOf(topicKey);
					if (topicIndex > -1) {
						mTopics.set(topicIndex, newTopic);
						notifyItemChanged(topicIndex);
					} else {
						Log.w(TAG, "onChildChanged:unknown_child:" + topicKey);
					}
				}

				@Override
				public void onChildRemoved(DataSnapshot dataSnapshot) {
					MyHelper.dismissDialog();
					Log.d("onChildRemoved", dataSnapshot.getKey());

					String topicKey = dataSnapshot.getKey();
					int topicIndex = mTopicIds.indexOf(topicKey);
					if (topicIndex > -1) {
						mTopicIds.remove(topicIndex);
						mTopics.remove(topicIndex);
						notifyItemRemoved(topicIndex);
					} else {
						Log.w(TAG, "onChildRemoved:unknown_child:" + topicKey);
					}
				}

				@Override
				public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
					MyHelper.dismissDialog();
					Log.d("onChildMoved", dataSnapshot.getKey());
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {
					MyHelper.dismissDialog();
					Log.w(TAG, databaseError.toException());
					Toast.makeText(mContext, "Failed to load topics.", Toast.LENGTH_SHORT).show();
				}
			};
			query.addChildEventListener(mChildEventListener);
		}

		@Override
		public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			View view = inflater.inflate(R.layout.item_topic, parent, false);
			return new TopicViewHolder(view);
		}

		@Override
		public void onBindViewHolder(final TopicViewHolder holder, final int position) {
			final Topic topic = mTopics.get(position);
			holder.titleView.setText(topic.title);
			holder.speakerView.setText(topic.speaker);
			holder.voteView.setText(mContext.getString(R.string.vote, topic.voteCount));
			Glide.with(mContext).load(topic.avatar).error(R.mipmap.ic_launcher_round).into(holder.avatar);

			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(ListActivity.this, DetailActivity.class);
					intent.putExtra("topicKey", mTopicIds.get(holder.getAdapterPosition()));
					startActivity(intent);
				}
			});

			holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					String topicKey = mTopicIds.get(holder.getAdapterPosition());
					confirmDelete(topicKey, topic);
					return true;
				}
			});
		}

		@Override
		public int getItemCount() {
			return mTopics.size();
		}

		void cleanupListener() {
			if (mValueEventListener != null) {
				mDatabaseReference.removeEventListener(mValueEventListener);
			}
			if (mChildEventListener != null) {
				mDatabaseReference.removeEventListener(mChildEventListener);
			}
		}

		private void confirmDelete(final String topicKey, final Topic topic) {
			AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
			alert.setMessage(getString(R.string.confirm_delete, topic.title));
			alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
					//mDatabase.child("topics").child(topicKey).setValue(null);
					//mDatabase.child("user-topics").child(topic.uid).child(topicKey).removeValue();

					Map<String, Object> childUpdates = new HashMap<>();
					childUpdates.put("/topics/" + topicKey, null);
					childUpdates.put("/user-topics/" + topic.uid + "/" + topicKey, null);
					FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
				}
			});
			alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
				}
			});
			alert.setCancelable(true);
			alert.show();
		}
	}
}