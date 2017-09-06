package com.ivy.sd.png.bo;

public class ChannelBO {

	private int channelId;
	private String channelName;

	public ChannelBO() {
		// TODO Auto-generated constructor stub
	}

	public ChannelBO(int channelId, String channelName) {
		this.channelId = channelId;
		this.channelName = channelName;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return channelName;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public int getChannelId() {
		return channelId;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getChannelName() {
		return channelName;
	}
}
