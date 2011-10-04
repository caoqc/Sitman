package sg.com.sitman.data;

import android.app.Application;
import android.media.MediaPlayer;

public class MyApplication extends Application {

	public MediaPlayer mediaPlayer;
	
	//control the player status
	public Boolean isPlaying;
	public Boolean isPause;
	public Boolean isStop;
	public Boolean isRelease;
	
	//play type option
	public int playMode;
	public int repeatTimes;
//	public Boolean isPauseOverRepeat;
	public int repeatHintType;
	
	//file to play
	public String playSong;
	public String playSongBack;
	
	//repeat area
	public long beginArea;
	public long endArea;
	
	//repeated times
	public int repeatedTimes;
	public long playingPosition;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		isPlaying = false; 
		isRelease = true;
		isPause = false;
		isStop = true;
		
		playMode = Constants.PLAYMODE_NORMAL;
		repeatTimes = 1;
		repeatHintType = Constants.HINTTYPE_RING;

		beginArea = 0;
		endArea = 0;
		
		repeatedTimes = 0;
		playingPosition = 0;
		
		playSong = "/sdcard/songs/leo sayer - i love you more than i can say.mp3";
		
		mediaPlayer = new MediaPlayer();
		
	}

}
