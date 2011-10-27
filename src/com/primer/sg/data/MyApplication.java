package com.primer.sg.data;


import java.util.ArrayList;

import android.app.Application;
import android.media.MediaPlayer;

public class MyApplication extends Application {

	public static final int MiLLSECOND = 100;  // 1/10毫秒
	public static final int SECOND = 1000;     //1秒
	public static final int MINUTE = 60000;    //1分
	public static final int HOUR = 3600000;    //1小时
	
	public MediaPlayer mediaPlayer;

	//play type option
	public int playMode;
	public int repeatTimes;
	public Boolean isPauseOverRepeat;
	public int waitTime;
	public int repeatHintType;
	
	public ArrayList<String> breakPointList;
	public ArrayList<String> pointTextList;
	public ArrayList<Boolean> validPointList;
	public ArrayList<Integer> timePointList;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		playMode = Constants.PLAYMODE_NORMAL;
		repeatTimes = 1;
		waitTime = 300;
		isPauseOverRepeat = true;
		
		mediaPlayer = new MediaPlayer();
		
	}
	
}
