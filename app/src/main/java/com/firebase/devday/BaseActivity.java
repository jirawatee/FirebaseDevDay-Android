package com.firebase.devday;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity {
	private long mBackPressed = 0;

	@Override
	public void onBackPressed() {
		if (mBackPressed + 2000 > System.currentTimeMillis()) {
			super.onBackPressed();
		} else {
			Toast.makeText(this, R.string.exit, Toast.LENGTH_SHORT).show();
			mBackPressed = System.currentTimeMillis();
		}
	}
}