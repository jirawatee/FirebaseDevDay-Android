package com.firebase.devday.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Message {
	public String message;
	public boolean translated;

	public Message() {
		// Default constructor required for calls to DataSnapshot.getValue(User.class)
	}

	public Message(String message, boolean translated) {
		this.message = message;
		this.translated = translated;
	}
}