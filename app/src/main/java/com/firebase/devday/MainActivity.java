package com.firebase.devday;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.devday.helpers.MyHelper;
import com.firebase.devday.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity implements View.OnClickListener, TextToSpeech.OnInitListener{
	private static final int RESULT_SPEECH = 1;
	private static final int MY_DATA_CHECK_CODE = 2;
	private TextToSpeech myTTS;
	private DatabaseReference mDatabase, mMessagesRef, mMessagesThaiRef;
	private ValueEventListener valueEventListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.btn_read_write).setOnClickListener(this);
		findViewById(R.id.btn_orderby_value).setOnClickListener(this);
		findViewById(R.id.btn_listener).setOnClickListener(this);
		findViewById(R.id.btn_translate).setOnClickListener(this);
		findViewById(R.id.btn_sign_out).setOnClickListener(this);

		mDatabase = FirebaseDatabase.getInstance().getReference();
		mMessagesRef = mDatabase.child("messages");

		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_read_write:
				startActivity(new Intent(this, ReadWriteActivity.class));
				break;
			case R.id.btn_listener:
				startActivity(new Intent(this, ListActivity.class));
				break;
			case R.id.btn_orderby_value:
				startActivity(new Intent(this, PetrolActivity.class));
				break;
			case R.id.btn_translate:
				if (valueEventListener != null) {
					mMessagesThaiRef.removeEventListener(valueEventListener);
				}
				if (isSpeechRecognitionActivityPresented(this)) {
					Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
					try {
						startActivityForResult(intent, RESULT_SPEECH);
					} catch (ActivityNotFoundException a) {
						Toast.makeText(getApplicationContext(), "Ops! Your device doesn't support Speech to Text",Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(MainActivity.this, "In order to activate speech recognition you must install \"Google Voice Search\"", Toast.LENGTH_LONG).show();
					installGoogleVoiceSearch(MainActivity.this);
				}
				break;
			case R.id.btn_sign_out:
				signOut();
				break;
		}
	}

	@Override
	public void onInit(int initStatus) {
		Locale mThai = new Locale("th", "TH");
		String msg = "";

		switch (initStatus) {
			case TextToSpeech.SUCCESS:
				if (myTTS.isLanguageAvailable(mThai) == 1) {
					int result = myTTS.setLanguage(mThai);
					if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
						msg = "Sorry! TH is no data.";
						myTTS.setLanguage(Locale.US);
					} else {
						msg = "SUCCESS : TH is supported.";
					}
				} else {
					myTTS.setLanguage(Locale.US);
					msg = "SUCCESS : Only EN is available.";
				}
				break;
			case TextToSpeech.ERROR:
				msg = "ERROR : Initial failed.";
				break;
			case TextToSpeech.ERROR_NETWORK:
				msg = "ERROR : No internet connection.";
				break;
			case TextToSpeech.ERROR_NETWORK_TIMEOUT:
				msg = "ERROR : Network timeout.";
				break;
			case TextToSpeech.ERROR_NOT_INSTALLED_YET:
				msg = "ERROR : Not install yet.";
				break;
		}
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case RESULT_SPEECH:
				if (resultCode == RESULT_OK && null != data) {
					ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					final Message message = new Message(text.get(0), false);

					String topicKey = mMessagesRef.child("en").push().getKey();
					mMessagesRef.child("en").child(topicKey).setValue(message);
					mMessagesThaiRef = mMessagesRef.child("th").child(topicKey);

					valueEventListener = new ValueEventListener() {
						@Override
						public void onDataChange(DataSnapshot dataSnapshot) {
							Message messages = dataSnapshot.getValue(Message.class);
							if (messages != null) {
								myTTS.speak(messages.message, TextToSpeech.QUEUE_FLUSH, null);
							}
						}

						@Override
						public void onCancelled(DatabaseError databaseError) {
							Log.e("DatabaseError", databaseError.getMessage());
						}
					};
					mMessagesThaiRef.addValueEventListener(valueEventListener);
				}
				break;
			case MY_DATA_CHECK_CODE:
				if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
					myTTS = new TextToSpeech(this, this);
				} else {
					Intent installTTSIntent = new Intent();
					installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
					startActivity(installTTSIntent);
				}
				break;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (valueEventListener != null) {
			mMessagesThaiRef.removeEventListener(valueEventListener);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myTTS != null) {
			myTTS.stop();
			myTTS.shutdown();
		}
	}

	private static boolean isSpeechRecognitionActivityPresented(Activity callerActivity) {
		try {
			PackageManager pm = callerActivity.getPackageManager();
			List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
			if (activities.size() != 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static void installGoogleVoiceSearch(final Activity ownerActivity) {
		Dialog dialog = new android.app.AlertDialog.Builder(ownerActivity)
				.setMessage("For recognition it’s necessary to install \"Google Voice Search\"")
				.setTitle("Install Voice Search from Google Play?")
				.setPositiveButton("Install", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.voicesearch"));
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
							ownerActivity.startActivity(intent);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				})
				.setNegativeButton("Cancel", null)
				.create();
		dialog.show();
	}

	private void signOut() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage("Sign out?");
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
				FirebaseAuth.getInstance().signOut();
				MyHelper.redirect(MainActivity.this, SigninActivity.class);
			}
		});
		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});
		alert.show();
	}
}