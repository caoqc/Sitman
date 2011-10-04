package com.primer.sg.data;


public class Constants {
	
	//播放模式
	public static final int PLAYMODE_NORMAL = 1;
	public static final int PLAYMODE_REPEATAREA = 2;
	public static final int PLAYMODE_LIMITREPEATAREA = 3;
	public static final int PLAYMODE_RANDOM = 4;
	
	//播放状态消息
	public static final int PLAYMESSAGE_PLAY = 1;
	public static final int PLAYMESSAGE_PAUSE = 2;
	public static final int PLAYMESSAGE_STOP = 3;
	public static final int PLAYMESSAGE_FF = 4;
	public static final int PLAYMESSAGE_REW = 5;
	public static final int PLAYMESSAGE_POSITION_CHANGE = 6;
	public static final int PLAYMESSAGE_SONG_CHANGE = 7;
	
	public static final int HINTTYPE_RING = 1;
	public static final int HINTTYPE_WAIT = 2;
	
	public static final int REQUEST_LOAD = 1;
	
	public static final String LRC_MESSAGE_ACTION = "caoqc.receiver.music";
	public static final int FEED_CHANGE_SONG = 1;
	public static final int FEED_UPDATE_TEXT = 2;
	public static final int FEED_UPDATE_PROCESS = 3;
	public static final int FEED_CHANGE_LENGTH = 4;
}
