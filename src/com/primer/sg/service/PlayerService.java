package sg.com.sitman.service;

import java.io.IOException;

import sg.com.sitman.data.Constants;
import sg.com.sitman.data.MyApplication;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class PlayerService extends Service {

	private MyApplication myApplication;
	private long playPosition = 0;
	private long beginPosition = 0;
	private long endPosition = 0;
	
	private Handler handlerProcess;
	private UpdateCallback updateCallback;
	
	private Handler handlerText;
	private UpdateText updateText;
	
	private Handler handlerHint;
	private UpdateHint updateHint;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		handlerProcess = new Handler();
		updateCallback = new UpdateCallback();
		
		handlerText = new Handler();
		updateText = new UpdateText();
		
		handlerHint = new Handler();
		updateHint = new UpdateHint();
		
		myApplication = (MyApplication) getApplication();
		myApplication.mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				handlerProcess.removeCallbacks(updateCallback);
			}
		});
		myApplication.mediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
			
			@Override
			public void onSeekComplete(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mp.start();
			}
		});

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub		
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		int msg = intent.getIntExtra("PlayCommand",0);
		switch (msg) {
			case Constants.PLAYMESSAGE_PLAY:
				if (!myApplication.isPlaying) {
					if (!myApplication.isRelease) {
						myApplication.mediaPlayer.start();
						myApplication.isPlaying = true;
						
						Intent backIntent = new Intent();
						backIntent.putExtra("MessageType", Constants.FEED_CHANGE_LENGTH);
						backIntent.putExtra("MaxProcess", myApplication.mediaPlayer.getDuration());
						backIntent.setAction(Constants.LRC_MESSAGE_ACTION);
						sendBroadcast(backIntent);
					}else {
						myApplication.mediaPlayer.stop();
						myApplication.mediaPlayer.release();
						myApplication.isRelease = true;
						play();
					}
				}

				break;

			case Constants.PLAYMESSAGE_PAUSE:
				pause();
				break;
				
			case Constants.PLAYMESSAGE_FF:
				
				break;
				
			case Constants.PLAYMESSAGE_REW:
				
				break;

			case Constants.PLAYMESSAGE_POSITION_CHANGE:
				int pos = intent.getIntExtra("PlayPosition", 0);
				if (myApplication.mediaPlayer.isPlaying()) {
					handlerProcess.removeCallbacks(updateCallback);
					myApplication.mediaPlayer.pause();
					myApplication.mediaPlayer.seekTo(pos);
					myApplication.mediaPlayer.start();
					handlerProcess.postDelayed(updateCallback, 5);
				}
				if (myApplication.isPause) {
					myApplication.mediaPlayer.seekTo(pos);
				}
				break;

			case Constants.PLAYMESSAGE_SONG_CHANGE:
				if (!myApplication.isRelease) {
					myApplication.mediaPlayer.stop();
					myApplication.mediaPlayer.release();
					myApplication.isRelease = true;
				}else {
					myApplication.isPlaying = true;
				}
				handlerProcess.removeCallbacks(updateCallback);
				play();
				Intent backIntent = new Intent();
				backIntent.putExtra("MessageType", Constants.FEED_CHANGE_SONG);
				backIntent.setAction(Constants.LRC_MESSAGE_ACTION);
				sendBroadcast(backIntent);
				break;
				
			default:
				break;
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private void play() {
		// TODO Auto-generated method stub
		try {
			myApplication.mediaPlayer = MediaPlayer.create(this, Uri.parse("file://" + myApplication.playSong));
			myApplication.playSongBack = myApplication.playSong;
			myApplication.mediaPlayer.prepare();	
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		//myApplication.beginArea = 0;
		//myApplication.endArea = myApplication.mediaPlayer.getDuration();
		//myApplication.playProcess = endPosition - beginPosition;
		//myApplication.mediaPlayer.seekTo((int)myApplication.playProcess+5);		
		beginPosition = System.currentTimeMillis();
		myApplication.mediaPlayer.start();
		
		myApplication.isPlaying = true;		
		myApplication.isRelease = false;

		Intent backIntent = new Intent();
		backIntent.putExtra("MessageType", Constants.FEED_CHANGE_LENGTH);
		backIntent.putExtra("MaxProcess", myApplication.mediaPlayer.getDuration());
		backIntent.setAction(Constants.LRC_MESSAGE_ACTION);
		sendBroadcast(backIntent);
		
		handlerProcess.postDelayed(updateCallback, 5);
		Log.d("player", "play");
		
	}

	private void pause() {
		// TODO Auto-generated method stub
		if (myApplication.isPlaying && !myApplication.isRelease) {
			endPosition = System.currentTimeMillis();
			myApplication.mediaPlayer.pause();			
			myApplication.isPlaying = false;
			myApplication.isPause = true;
		}		
	}
	
	class UpdateCallback implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (myApplication.isPlaying) {
				int process = myApplication.mediaPlayer.getCurrentPosition();
				Intent backIntent = new Intent();
				backIntent.putExtra("MessageType", Constants.FEED_UPDATE_PROCESS);
				backIntent.putExtra("CurrentProcess", process);
				backIntent.setAction(Constants.LRC_MESSAGE_ACTION);
				sendBroadcast(backIntent);
				handlerProcess.postDelayed(updateCallback, 5);
			}
		}

	}

	class UpdateText implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (myApplication.isPlaying) {
				int process = myApplication.mediaPlayer.getCurrentPosition();
				Intent backIntent = new Intent();
				backIntent.putExtra("MessageType", Constants.FEED_UPDATE_PROCESS);
				backIntent.putExtra("CurrentProcess", process);
				backIntent.setAction(Constants.LRC_MESSAGE_ACTION);
				sendBroadcast(backIntent);
				handlerProcess.postDelayed(updateCallback, 5);
			}
		}
	}
	
	class UpdateHint implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (myApplication.isPlaying) {
				int process = myApplication.mediaPlayer.getCurrentPosition();
				Intent backIntent = new Intent();
				backIntent.putExtra("MessageType", Constants.FEED_UPDATE_PROCESS);
				backIntent.putExtra("CurrentProcess", process);
				backIntent.setAction(Constants.LRC_MESSAGE_ACTION);
				sendBroadcast(backIntent);
				handlerProcess.postDelayed(updateCallback, 5);
			}
		}
	}	

}
