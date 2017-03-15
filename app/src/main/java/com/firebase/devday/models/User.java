package com.firebase.devday.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
	public boolean isAdmin;
	public String email;
	public String avatar;
	public String speaker;

	public User() {
		// Default constructor required for calls to DataSnapshot.getValue(User.class)
	}

	public User(String email, String speaker, String avatar, boolean isAdmin) {
		this.email = email;
		this.speaker = speaker;
		this.avatar = avatar;
		this.isAdmin = isAdmin;
	}
}