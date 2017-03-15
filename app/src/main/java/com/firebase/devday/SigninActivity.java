package com.firebase.devday;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.firebase.devday.helpers.MyHelper;
import com.firebase.devday.models.User;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class SigninActivity extends BaseActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
	private static final int RC_SIGN_IN = 9001;
	private FirebaseAuth mAuth;
	private GoogleApiClient mGoogleApiClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
		mAuth = FirebaseAuth.getInstance();

		SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		signInButton.setSize(SignInButton.SIZE_WIDE);
		signInButton.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mAuth.getCurrentUser() != null) {
			MyHelper.redirect(this, MainActivity.class);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == RC_SIGN_IN) {
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			if (result.isSuccess()) {
				firebaseAuthWithGoogle(result.getSignInAccount());
			} else {
				Toast.makeText(this, result.getStatus().getStatusMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.sign_in_button) {
			if (mGoogleApiClient == null) {
				mGoogleApiClient = buildGoogleSignIn();
			}
			Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
			startActivityForResult(signInIntent, RC_SIGN_IN);
		}
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
	}

	private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
		MyHelper.showDialog(this);
		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {
					FirebaseUser firebaser = mAuth.getCurrentUser();
					User user = new User(
							firebaser.getEmail(),
							firebaser.getDisplayName(),
							firebaser.getPhotoUrl().toString(),
							true
					);
					FirebaseDatabase.getInstance().getReference().child("users").child(firebaser.getUid()).setValue(user);
					MyHelper.dismissDialog();
					MyHelper.redirect(SigninActivity.this, MainActivity.class);
				} else {
					MyHelper.dismissDialog();
					//noinspection ThrowableResultOfMethodCallIgnored,ConstantConditions
					Toast.makeText(SigninActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public GoogleApiClient buildGoogleSignIn() {
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.build();
		return new GoogleApiClient.Builder(this)
				.enableAutoManage(this, this)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();
	}
}