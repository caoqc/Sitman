
package com.primer.sg.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.primer.sg.data.Constants;
import com.primer.sg.data.MyApplication;


public class PlayerService extends Service {
	
	//file to play
	private String playSong = "";
	 
	//control the player status
	private Boolean isPlaying; 
	private Boolean isPause;
	private Boolean isStop;
	private Boolean isRelease;
	private Boolean isRecordSound;
	private Boolean isPlayRecord;

	private MyApplication myApplication;
	private MediaPlayer mPlayer;
	
	//repeat area	
	private int beginPosition = 0;
	private int endPosition = 0;
	private int currentPosition = 0;
	private int maxPosition = 0;
	
	//repeated times
	private int repeatedTimes;
	private int playAreaNum;
	
	private Timer timer;
	private TimerTask timerTask;
	private boolean isChooseArea = false;
	
	private StringBuffer textBlockString;
	private ArrayList<String> breakPointList;
	private ArrayList<String> pointTextList;
	private ArrayList<Boolean> validPointList;
	
	private Handler handlerProcess;
	private UpdateProcessCallback updateProcessCallback;
	
	private Handler handlerText;
	private UpdateTextCallback updateTextCallback;
	
	private Handler handlerHint;
	private UpdateHintCallback updateHintCallback;
	
	private Handler handlerPoint;
	private UpdatePointCallback updatePointCallback;
	private ArrayList<Integer> timePointList;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	    
		handlerProcess = new Handler();
		updateProcessCallback = new UpdateProcessCallback();
		
		handlerText = new Handler();
		updateTextCallback = new UpdateTextCallback();
		
		handlerHint = new Handler();
		updateHintCallback = new UpdateHintCallback();
		
		handlerPoint = new Handler();
		updatePointCallback = new UpdatePointCallback(); 
		
		myApplication = (MyApplication) getApplication();
		if (myApplication.mediaPlayer!=null) {
			mPlayer = myApplication.mediaPlayer;
		}else {
			mPlayer = new MediaPlayer();
			myApplication.mediaPlayer = mPlayer;
		}
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		mPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				
				handlerProcess.removeCallbacks(updateProcessCallback);
				
				if (isRecordSound) {

					mPlayer.reset();
					try {
						mPlayer.setDataSource(playSong);
						mPlayer.prepare();
						mPlayer.seekTo(currentPosition);
						isPlaying = true;
						isPause = true; 
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		});
		
		mPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
			
			@Override
			public void onSeekComplete(MediaPlayer mp) {
				// TODO Auto-generated method stub
 
				if (isRecordSound) {
					isRecordSound = false;
				}else {
					//找到位置开始播放
					mPlayer.start();
					isPlaying = true;
					isPause = false;
					updateUIPlayerState(Constants.PLAYER_STATE_PLAYING);
					
					//启动字幕动态更新
					handlerText.postDelayed(updateTextCallback, 30);
					//启动进度动态更新
					handlerProcess.postDelayed(updateProcessCallback, 5);
				}
			}
		});

		mPlayer.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				mPlayer.release();
				mPlayer = null;
				mPlayer = new MediaPlayer();
				myApplication.mediaPlayer = mPlayer;
				mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				
				isPlaying = false;
				isPause = false; 
				isStop = false;
				isRelease = true;
				isRecordSound = false;
				
				Log.d("test", "error is happened!");
				
				getSongInfo(playSong);
				try {
					mPlayer.setDataSource(playSong);
					mPlayer.prepare();
					mPlayer.seekTo(beginPosition);
					
					isPlaying = true;
					isPause = true;
					isRelease = false;
					
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return true; 
			}
		});
		
		isPlaying = false;
		isPause = false;
		isStop = false;
		isRelease = true;
		isRecordSound = false;
		isPlayRecord = false;
		
		repeatedTimes = 1;
		
		timer = new Timer();
        timerTask = new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				setPlayParameter();
			}
		};
		
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
				if (!isPlaying) {
					getSongInfo(playSong);	
				}
				isPlayRecord = intent.getBooleanExtra("PlayOrinal", false);
				play();
				break;

			case Constants.PLAYMESSAGE_PAUSE:
				pause();				
				break;

			case Constants.PLAYMESSAGE_POSITION_CHANGE:
				//获取用户指定的播放位置
				int pos = intent.getIntExtra("PlayPosition", 0);
				changePosition(pos);
				
				break;

			case Constants.PLAYMESSAGE_SONG_CHANGE:
				changeSong(intent);
				break;
			
			case Constants.PLAYMESSAGE_RECORDSOUND:
				playRecordSound(intent); 
				break;
				
//			case Constants.PLAYMESSAGE_SETTING:  //参数设置
//				changeSetting();
//				break;

			case Constants.PLAYMESSAGE_CHANGEAREA:  //改变播放区间
				changePlayArea(intent.getIntExtra("PlayAreaNum", 0));
				break;
				
			default:
				break;
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private void changePlayArea(int pos) {
		// TODO Auto-generated method stub
		beginPosition = timePointList.get(pos);
		endPosition = timePointList.get(pos+1);
		playAreaNum = pos;
		repeatedTimes = 1;

		mPlayer.seekTo(beginPosition);
	}

	private void playRecordSound(Intent intent) {
		// TODO Auto-generated method stub

			File file = new File(intent.getStringExtra("SongName"));
			if (file.exists()) {
				try {
					mPlayer.stop();
					mPlayer.reset();
					mPlayer.setDataSource(file.getAbsolutePath());
					mPlayer.prepare();	
					mPlayer.start();
					
					//设置播放器状态
					isPlaying = true;		
					isRelease = false;
					isPause = false;
					isStop = false;
					isRecordSound = true;
					
					//取消进度更新
					handlerProcess.removeCallbacks(updateProcessCallback);
				
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
			}
	}

	
	private void changeSong(Intent intent) {
		//获取新的歌曲
		playSong = intent.getStringExtra("SongName");
		
		//取消进度更新和字幕更新
		handlerProcess.removeCallbacks(updateProcessCallback);
		handlerText.removeCallbacks(updateTextCallback);
		
		Log.d("test", "changeSong isPlaying:" + isPlaying);
		//根据播放器状态，加载或播放新歌曲
		if (isPlaying) {   //由播放状态进入
			mPlayer.stop();
			mPlayer.reset(); 
			isPlaying = false;
			play();
		}else {   //由非播放状态进入
			play();
//			mPlayer.pause();
//			isPause = true;
//			updateUIPlayerState(Constants.PLAYER_STATE_PAUSE);			
		}	

		//获取歌曲的断点列表和歌词列表		
		getSongInfo(playSong);
		
	}

	
	private void getSongInfo(String playSong) {
		// TODO Auto-generated method stub
		
		breakPointList = new ArrayList<String>();
		pointTextList = new ArrayList<String>();
		validPointList = new ArrayList<Boolean>();
		timePointList = new ArrayList<Integer>();
		textBlockString = new StringBuffer();
		
		String songInfoName = playSong.substring(0,playSong.lastIndexOf(".")) + ".rtf";
		File file = new File(songInfoName);

		if (file.exists()) {
			
			boolean flag = false;
			int pos = 0;
			String line = null;
			String timeString = null;
			String textString = null;
			BufferedReader reader = null;
			
			try {
				reader = new BufferedReader(new FileReader(file));
				
				while ((line=reader.readLine())!=null) {
					
					Log.d("test", line);
					
					if (line.indexOf("<MP>")!=-1) {
						break;
					}
					
				}
				
				while ((line=reader.readLine())!=null) {
					
					Log.d("test", line);
					
					if (line.indexOf("</MP>")==-1) {
						
						if (flag) {
							
							textBlockString.append(line);
							
						}else {
							
							pos = line.indexOf('.', 2);
							timeString = line.substring(2,pos+2);
							textString = line.substring(pos+4);
							
							timeString = timeString.length()==10?timeString:"00:" + timeString;
							
							pos = 0;
							pos += Integer.parseInt(timeString.substring(0,2)) * MyApplication.HOUR;
							pos += Integer.parseInt(timeString.substring(3, 5)) * MyApplication.MINUTE;
							pos += Integer.parseInt(timeString.substring(6,8)) * MyApplication.SECOND;
							pos += Integer.parseInt(timeString.substring(9)) * MyApplication.MiLLSECOND;
							
							breakPointList.add(timeString);
							validPointList.add("E".equalsIgnoreCase(line.substring(0, 1)));
							pointTextList.add(textString);
							timePointList.add(pos);
						}			

					} else {
						flag = true;
					}
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					if (reader!=null) {
						reader.close();
					}					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				file = null;
			}
			
		}
		
		if (breakPointList.size()==0){
			breakPointList.add("00:00:00.0");
			validPointList.add(true);
			pointTextList.add("");
			timePointList.add(0);
			
			int rest = 0;
			int hours =  endPosition/MyApplication.HOUR;
			rest = endPosition%MyApplication.HOUR;
			int minutes = rest/MyApplication.MINUTE;
			rest = rest%MyApplication.MINUTE;
			int seconds = rest/MyApplication.SECOND;
			rest = rest%MyApplication.SECOND;
			int millSeconds = rest/MyApplication.MiLLSECOND;
			
			breakPointList.add( (hours<9? "0" + hours:"" + hours) + ":" + (minutes<9? "0" + minutes: "" + minutes) + ":" + (seconds<9? "0" + seconds:"" + seconds) + "." + millSeconds);
			validPointList.add(true);
			pointTextList.add("");
			timePointList.add(endPosition);
		}
		
		myApplication.breakPointList = breakPointList;
		myApplication.pointTextList = pointTextList;
		myApplication.validPointList = validPointList;
		myApplication.timePointList = timePointList;
		
		//启动断点更新进程
		handlerPoint.post(updatePointCallback);

	}

	
	private void changePosition(int pos) {
		
		//取消进度更新和字幕更新
		handlerProcess.removeCallbacks(updateProcessCallback);
		handlerText.removeCallbacks(updateTextCallback);
		
		//
		if (isPlaying) {
			isPause = true;
			mPlayer.pause();
			mPlayer.seekTo(pos); 
			int count = validPointList.size()-1;
			for (int i = 0; i < count; i++) {
				if (pos>=timePointList.get(i) && pos<=timePointList.get(i+1)) {
					playAreaNum = i;
					beginPosition = timePointList.get(i);
					endPosition = timePointList.get(i+1);
					break;
				}
			}
		}
	}

	private void play() {
		// TODO Auto-generated method stub
		Log.d("test", "1. isPlaying:" + isPlaying);
		if (isRecordSound) {  //由 播放录音状态 进入 正常播放状态
			
			try { 
				
				mPlayer.reset();
				mPlayer.setDataSource(playSong);
				mPlayer.prepare();	 
				mPlayer.seekTo(currentPosition);
				
				isPlaying = true;
				isPause = true;
				isRelease = false;
				isStop = false;
				isRecordSound = false;
				
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else {   
			Log.d("test", "2. isPlaying:" + isPlaying);
			if (isPlaying) {   
				
				if (isPause) {   //由 暂停状态 进入 正常播放状态
					
					if (currentPosition>=endPosition || isPlayRecord) {
						currentPosition = beginPosition;
					}
					mPlayer.seekTo(currentPosition);

					isPause = false;
					isStop = false;
					isRelease = false;
					
					//启动进度动态更新
					handlerProcess.postDelayed(updateProcessCallback, 5);
					
				}else {   //由 正常播放状态 进入 暂停状态
					mPlayer.pause();					
					isPause = true;
					handlerProcess.removeCallbacks(updateProcessCallback);
				}

				
			}else {      //由空闲状态（包含出错时恢复后的空闲状态）进入 正常播放状态
				Log.d("test", "3. isPlaying:" + isPlaying);
				try {
					
					mPlayer.setDataSource(playSong);
					mPlayer.prepare();
					maxPosition = mPlayer.getDuration();
					beginPosition = 0;
					endPosition = maxPosition;
					repeatedTimes = 1;
					playAreaNum = -1;
					mPlayer.seekTo(0);
					
					//设置播放器状态
					isPlaying = true;		
					isRelease = false;
					isPause = false;
					isStop = false;
					 
					//设置进度条的最大值
					Intent backIntent = new Intent();
					backIntent.putExtra("MessageType", Constants.FEED_CHANGE_LENGTH);
					backIntent.putExtra("MaxProcess", endPosition);
					backIntent.setAction(Constants.LRC_MESSAGE_ACTION);
					sendBroadcast(backIntent);
					
					//启动进度动态更新
					handlerProcess.postDelayed(updateProcessCallback, 5);
					Log.d("player", "play");
					
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
			}
		}

	}

	
	private void pause() {
		// TODO Auto-generated method stub
		if (isPlaying && !isPause) {
			
			mPlayer.pause();
			
			isPause = true;
			
			//取消进度动态更新
			handlerProcess.removeCallbacks(updateProcessCallback);
		}		
	}
	
	private void setPlayParameter() {
		// TODO Auto-generated method stub
		
		if (isPlayRecord) {
			isPause = true;
			mPlayer.pause();
			updateUIPlayerState(Constants.PLAYER_STATE_PAUSE);
			return;
		}
		
		switch (myApplication.playMode) {
		
			case Constants.PLAYMODE_NORMAL:
				isPause = true;
				mPlayer.pause();
				updateUIPlayerState(Constants.PLAYER_STATE_PAUSE);
				repeatedTimes = 0;
				break;
				
			case Constants.PLAYMODE_REPEATAREA:
				mPlayer.seekTo(beginPosition);
				repeatedTimes = 1;
				break;
				
			case Constants.PLAYMODE_LIMITREPEATAREA:

				Log.d("test", "repeatedTimes:" + repeatedTimes);
				if (repeatedTimes > myApplication.repeatTimes) {
					
					if (beginPosition==0 && endPosition==maxPosition) {  //as whole file to repeat
						if (myApplication.isPauseOverRepeat) {
							mPlayer.pause();
							isPause = true;
							updateUIPlayerState(Constants.PLAYER_STATE_PAUSE);
							repeatedTimes = 0;
						}else {
							mPlayer.seekTo(0);
							repeatedTimes = 1;
						}
						break;
						
					} else {   //part to repeat
						
						int amount = validPointList.size();
						int loop = playAreaNum; 
						
						loop++;
						loop = loop % amount;
						
						if ((loop+1)==amount) {  //this is the Last point

							if (myApplication.isPauseOverRepeat) {   //本次播放完毕，暂停
								mPlayer.pause();
								isPause = true;
								updateUIPlayerState(Constants.PLAYER_STATE_PAUSE);
								repeatedTimes = 0;
								break;
							} else {
								loop = findNextPlayPoint(0,amount,playAreaNum);
							}
							
						} else {
							loop = findNextPlayPoint(loop,amount,playAreaNum);
						}

						//play the next area
						if (loop!=-1) {
							mPlayer.seekTo(beginPosition);
							playAreaNum = loop;
							repeatedTimes = 1;
						}else {
							mPlayer.pause();
							isPause = true;
						}
					}
				
				}else {
					repeatedTimes++;
					mPlayer.seekTo(beginPosition);
				}

				break;
				
				
			case Constants.PLAYMODE_RANDOM:

				if (myApplication.repeatTimes<repeatedTimes) {
					
					Random random = new Random();
					int nextArea = random.nextInt(validPointList.size()-1);
					Log.d("test", "random:" + nextArea);

					beginPosition = timePointList.get(nextArea);
					endPosition = timePointList.get((nextArea+1)%validPointList.size());
					
					repeatedTimes = 1;
					
				}else {
					repeatedTimes++;
				}

				mPlayer.seekTo(beginPosition);
				break;
				
			default:
				mPlayer.seekTo(beginPosition);
				break;
		}		
		
		isChooseArea = false;
	}

	private int findNextPlayPoint(int i, int amount, int currentPosition) {
		// TODO Auto-generated method stub
		
		int nextPosition = i;
		
		while (nextPosition != currentPosition ) {  //search for next play area
			Log.d("test", "current position:" + currentPosition + "   nextPosition:" + nextPosition + "  --  " + validPointList.get(nextPosition));
			if (validPointList.get(nextPosition)) {
				
				if (nextPosition==0) {   //the first one
					beginPosition = 0;
				} else {
					beginPosition = timePointList.get(nextPosition);
				}
				
				if (nextPosition+1 == amount) {  //倒数第一个
					endPosition = maxPosition;
				}else {
					endPosition = timePointList.get(nextPosition+1);
				}

				break;
				
			} else {
				
				nextPosition++;
				nextPosition = nextPosition % amount;
				
				if (nextPosition+1==amount) { 
					
					if (myApplication.isPauseOverRepeat) {  //本次播放完毕，暂停
						mPlayer.pause();
						updateUIPlayerState(Constants.PLAYER_STATE_PAUSE);
						nextPosition = -1;
						break;
					}
				}
			}					
		}
		Log.d("test", "choose:" + nextPosition + " beginPosition:" + beginPosition + "--endPosition:" + endPosition);
		return nextPosition;
	}

	
	private void updateUIPlayerState(int state){
		Intent feedbackIntent = new Intent();
		feedbackIntent.putExtra("MessageType", Constants.FEED_UPDATE_STATE);
		feedbackIntent.putExtra("PlayState", state);
		feedbackIntent.setAction(Constants.LRC_MESSAGE_ACTION);
		sendBroadcast(feedbackIntent);
	}
	
	
	//更新进度条的回调
	class UpdateProcessCallback implements Runnable{
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if (mPlayer.isPlaying()) {
				
				currentPosition = mPlayer.getCurrentPosition();
				
				if (currentPosition >= endPosition) {  //当前播放区间已经完成
					
					mPlayer.pause();  Log.d("test", playAreaNum + " is over!");
					isPause = true;
					
					setPlayParameter();
				}	
				
				Intent feedbackIntent = new Intent();
				feedbackIntent.putExtra("MessageType", Constants.FEED_UPDATE_PROCESS);
				feedbackIntent.putExtra("CurrentProcess", currentPosition);
				if (playAreaNum==-1) {
					feedbackIntent.putExtra("BeginArea", breakPointList.get(0));
					feedbackIntent.putExtra("EndArea", "end");
				}else {
					feedbackIntent.putExtra("BeginArea", breakPointList.get(playAreaNum));
					feedbackIntent.putExtra("EndArea", breakPointList.get(playAreaNum+1));
				}

				feedbackIntent.setAction(Constants.LRC_MESSAGE_ACTION);
				sendBroadcast(feedbackIntent);
				handlerProcess.postDelayed(updateProcessCallback, 5);
			}
		}
	}

	//更新歌词的回调
	class UpdateTextCallback implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mPlayer.isPlaying()) {
				
				String textString;
				
				if (playAreaNum==-1) {
					textString = textBlockString.toString();
				}else {
					textString = pointTextList.get(playAreaNum);
				}
				 
				Intent feedbackIntent = new Intent();
				feedbackIntent.putExtra("MessageType", Constants.FEED_UPDATE_TEXT);
				feedbackIntent.putExtra("SongText", textString);
				feedbackIntent.setAction(Constants.LRC_MESSAGE_ACTION);
				sendBroadcast(feedbackIntent);
				handlerText.postDelayed(updateTextCallback, 30);
			}
		}
	} 

	class UpdateHintCallback implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mPlayer.isPlaying()) {
				int process = mPlayer.getCurrentPosition();
				Intent feedbackIntent = new Intent();
				feedbackIntent.putExtra("MessageType", Constants.FEED_UPDATE_PROCESS);
				feedbackIntent.putExtra("CurrentProcess", process);
				feedbackIntent.setAction(Constants.LRC_MESSAGE_ACTION);
				sendBroadcast(feedbackIntent);
				handlerProcess.postDelayed(updateHintCallback, 5);
			}
		}
	}	

	//更新断点列表
	class UpdatePointCallback implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Intent feedbackIntent = new Intent();
			feedbackIntent.putExtra("MessageType", Constants.FEED_UPDATE_POINTLIST);
			feedbackIntent.setAction(Constants.LRC_MESSAGE_ACTION);
			sendBroadcast(feedbackIntent);
		}
		
	}
}
