package com.firebase.devday;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.View;

import com.firebase.devday.helpers.MyHelper;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends BaseActivity implements View.OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.btn_read_write).setOnClickListener(this);
		findViewById(R.id.btn_orderby_value).setOnClickListener(this);
		findViewById(R.id.btn_listener).setOnClickListener(this);
		findViewById(R.id.btn_sign_out).setOnClickListener(this);
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
			case R.id.btn_sign_out:
				signOut();
				break;
		}
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