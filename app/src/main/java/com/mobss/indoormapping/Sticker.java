package com.mobss.indoormapping;

public class Sticker {

	private String mId;
	private String mName;
	private int mRssi;
	private long time;
	
	public Sticker(String mId, String mName, int mRssi, long time) {
		super();
		this.mId = mId;
		this.mName = mName;
		this.mRssi = mRssi;
		this.time = time;
		
	}

	
	public String getmId() {
		return mId;
	}


	public void setmId(String mId) {
		this.mId = mId;
	}


	public String getmName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
	}

	public int getmRssi() {
		return mRssi;
	}

	public void setmRssi(int mRssi) {
		this.mRssi = mRssi;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	
	

}
