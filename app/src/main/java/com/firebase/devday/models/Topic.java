package com.firebase.devday.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Topic {
	public String avatar;
	public String body;
	public String picture;
	public String speaker;
	public String status = "future";
	public long timeAdd = 0;
	public String title;
	public String uid;

	public int voteCount = 0;
	public Map<String, Boolean> votes = new HashMap<>();

	public int joinCount = 0;
	public Map<String, Boolean> joins = new HashMap<>();

	public int redeemCount = 0;

	public Topic() {
		// Default constructor required for calls to DataSnapshot.getValue(Topic.class)
	}

	public Topic(String uid, String speaker, String avatar, String title, String body, String picture) {
		this.uid = uid;
		this.speaker = speaker;
		this.avatar = avatar;
		this.title = title;
		this.body = body;
		this.picture = picture;
	}

	@Exclude
	public Map<String, Object> toMap() {
		HashMap<String, Object> result = new HashMap<>();
		result.put("uid", uid);
		result.put("speaker", speaker);
		result.put("avatar", avatar);

		result.put("title", title);
		result.put("body", body);
		result.put("picture", picture);

		result.put("status", status);
		if (timeAdd == 0) {
			result.put("timeAdd", ServerValue.TIMESTAMP);
		} else {
			result.put("timeAdd", timeAdd);
		}

		result.put("voteCount", voteCount);
		result.put("joinCount", joinCount);
		result.put("redeemCount", redeemCount);

		result.put("votes", votes);
		result.put("joins", joins);
		return result;
	}
}