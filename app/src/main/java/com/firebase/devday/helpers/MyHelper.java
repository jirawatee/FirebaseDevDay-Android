package com.firebase.devday.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.firebase.devday.R;

public class MyHelper {
	private static Dialog mDialog;

	public static void showDialog(Context context) {
		mDialog = new Dialog(context, R.style.CustomDialog);
		mDialog.addContentView(
				new ProgressBar(context),
				new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
		);
		mDialog.setCancelable(true);
		if (!mDialog.isShowing()) {
			mDialog.show();
		}
	}

	public static void dismissDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	public static void redirect(Activity activity, Class<?> activityClass) {
		activity.startActivity(new Intent(activity, activityClass));
		activity.finish();
	}
}