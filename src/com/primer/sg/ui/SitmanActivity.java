package com.primer.sg.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.primer.sg.R;
import com.primer.sg.data.Constants;
import com.primer.sg.data.MyApplication;
import com.primer.sg.service.PlayerService;

public class SitmanActivity extends Activity {
	
	private static final String RECORDPATH = "/data/data/com.primer.sg";
	private static final String RECORDSDPATH_STRING = "/sitman";
	private static final String RECORDFILENAME = "temp_recorder.3pg";
	private String recordNameString = "";
	
	private String modeString = "normal";
	
	private static final int CHOOSESONGCODE = 1;
	private static final int SETTINGCODE = 2;
	
	private FrameLayout container;
	private LinearLayout listParent;
	
	private Gallery gallery;
	
	private TextView currentTimeTextView;
	private TextView playProcesstTextView;
	
	private TextView songTextView;
	private SeekBar processSeekBar;
	private TextView readTextView;
	private ListView breakListView;
	private ScrollView textScrollView;
	
	private ImageButton playButton; 
	private ImageButton recordButton;
	
	private LrcMessageReceiver receiver;
	private IntentFilter myIntentFilter;

	private Boolean isPlaying = false;
	private Boolean isRecord = false;
	
	private String contentString;
	private int currentPosition;
	private int maxPosition;

	private ArrayList<HashMap<String, Object>> listItem;
	
	private MediaRecorder mRecorder;

	private File recordFile;
	private MyApplication myApplication;
	private ArrayList<String> playingPathFiles;
	private String playingSongString = "";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        //get view handler
        getViewHandler();
    	
		myApplication = (MyApplication) getApplication();
		setTitle(getString(R.string.app_name) + " -- normal");
		
//		container.setOnLongClickListener(new OnLongClickListener() {
//			
//			@Override
//			public boolean onLongClick(View v) {
//				// TODO Auto-generated method stub
//				callPointListView();
//				return false;
//			}
//		});
//		
//		textScrollView.setOnLongClickListener(new OnLongClickListener() {
//			
//			@Override
//			public boolean onLongClick(View v) {
//				// TODO Auto-generated method stub
//				callPointListView();
//				return false;
//			}
//
//		});
//
//		readTextView.setOnLongClickListener(new OnLongClickListener() {
//			
//			@Override
//			public boolean onLongClick(View v) {
//				// TODO Auto-generated method stub
//				callPointListView();
//				return true;
//			}
//		});

		findViewById(R.id.part_super).setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				callPointListView();
				return true;
			}
		});
		findViewById(R.id.part_one).setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				callPointListView();
				return true;
			}
		});	
		 
//		Drawable thumb = (Drawable) getResources().getDrawable(R.drawable.thumb_shape);
//		processSeekBar.setThumb(thumb);
		
		processSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub

				if (fromUser) {
					changePosition(processSeekBar.getProgress(), Constants.PLAYMESSAGE_POSITION_CHANGE); 
				}				
			}

		});
		
		gallery.setAdapter(new ImageAdapter(this));
		gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView parent, View v, int position, long id) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		contentString = "";
		currentPosition = 0;
		
	   	isPlaying = false;
    	isRecord = false;
		
		//播出电话暂停音乐播放   
        registerReceiver(new PhoneListener(), teleIntentFilter());  
        //创建一个电话服务   
        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);  
        //监听电话状态，接电话时停止播放   
        manager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE); 
        
		//获取新的歌曲
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//			playSong = intent.getStringExtra("SongName");
		}
    }  

	private void callPointListView() {
		if (listParent.getVisibility()==View.INVISIBLE) {
			listParent.setVisibility(View.VISIBLE);
		}else {
			listParent.setVisibility(View.INVISIBLE);
		}
	}
	
	private void getViewHandler() {
		container = (FrameLayout) findViewById(R.id.frame_parent);
        listParent = (LinearLayout) findViewById(R.id.list_parent);
        
        songTextView = (TextView) findViewById(R.id.song_name);
        
        gallery = (Gallery) findViewById(R.id.gallery_view);
        
        currentTimeTextView = (TextView) findViewById(R.id.current_time);
        playProcesstTextView = (TextView) findViewById(R.id.play_process);
        
        processSeekBar = (SeekBar) findViewById(R.id.process_seekbar);
        
        readTextView = (TextView) findViewById(R.id.read_text);
        textScrollView = (ScrollView) findViewById(R.id.scroll_view);
        
        recordButton = (ImageButton) findViewById(R.id.start_record);
        playButton = (ImageButton) findViewById(R.id.play_btn);

        breakListView = (ListView) findViewById(R.id.break_list);
	}
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		receiver = new LrcMessageReceiver();
		registerReceiver(receiver, getIntentFilter());
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
//		processSeekBar.setMax(maxPosition);
//		processSeekBar.setProgress(position);
//		readTextView.setText(contentString);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		
	}

	private IntentFilter getIntentFilter() {
		// TODO Auto-generated method stub
		if (myIntentFilter==null) {
			myIntentFilter = new IntentFilter();
			myIntentFilter.addAction(Constants.LRC_MESSAGE_ACTION);
		}
		return myIntentFilter;
	}
	
	private IntentFilter teleIntentFilter(){
		IntentFilter phoneIntentFilter = new IntentFilter();
		phoneIntentFilter.addAction(Intent.ACTION_CALL);
		
		return phoneIntentFilter;
	}

	public void btnClickDeal(View target) {

		switch (target.getId()) {		
				
			case R.id.prior_btn: //上一首
				nextSong(-1);
				break;
				
			case R.id.next_btn:  //下一首
				nextSong(1);
				break;
	
			case R.id.play_btn:  //播放/暂停
				if (playingSongString.length()>0) {
					play(true , false);
				}
				break;
			
			case R.id.start_record:  //录音
				if (isRecord) {
					stopRecordSound();
				}else {
					startRecordSound();
				} 
				break;
				
			case R.id.play_record:	//播放录音
				if (isPlaying) {
					play(true , false); 
				}
				if (recordNameString.length()>0) {
					playRecord();
				}
				break;

			case R.id.play_orinal:	//播放原音
				if (playingSongString.length()>0) {
					if (isPlaying) {
						play(true , false); 
					}
					play(true,true);
				}

				break;
			
//			case R.id.play_compare:	//播放 原音、录音
//				if (isPlaying) {
//					play(true , false); 
//				}
//				if (recordNameString.length()>0) {
//					playRecord();
//					play(true,true);
//				}
//				
//				break;
				
			case R.id.all_point:
				if (playingSongString.length()>0) {
					int count = myApplication.validPointList.size();
					for (int i = 0; i < count; i++) {
						myApplication.validPointList.set(i, true);
						listItem.get(i).put("Check", true);
					}
					breakListView.invalidateViews();
				}
				
				break;
				
			case R.id.no_point:
				if (playingSongString.length()>0) {
					int count = myApplication.validPointList.size();
					for (int i = 0; i < count; i++) {
						myApplication.validPointList.set(i, false);
						listItem.get(i).put("Check", false);
					}
					breakListView.invalidateViews();
				}
				break;
		}
	}

	private void stopRecordSound() {
		if (isRecord) {
			mRecorder.stop();
//			mRecorder.reset();
			mRecorder.release();
			mRecorder = null;
			
			Log.d("test", recordFile.length()+"");
			recordNameString = recordFile.getAbsolutePath();
			recordFile = null; 
			isRecord = false;

			recordButton.setImageResource(android.R.drawable.ic_btn_speak_now);
			
			playRecord(); 
		}
	}

	private void startRecordSound() { 
		
		if (!isRecord) { 
			
			if (isPlaying) {
				play(true , false); 
			}
						
			if (mRecorder==null) {
				mRecorder = new MediaRecorder();
			}
			
//			mRecorder.reset();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			
			File file;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + RECORDSDPATH_STRING);
			}else {
				file = new File(RECORDPATH);
			}
			
			if (!file.exists()) {
				file.mkdirs();
			}
			
			recordFile = new File(file.getAbsolutePath() + "/" + RECORDFILENAME);
			if (!recordFile.exists()) {
				try {
					recordFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}
			
			mRecorder.setOutputFile(recordFile.getAbsolutePath());
			try {
				mRecorder.prepare();
				mRecorder.start();
				isRecord = true;
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			recordButton.setImageResource(android.R.drawable.stat_notify_call_mute);
		}	
	}

	private void nextSong(int direct) {
		// TODO Auto-generated method stub
		if (playingPathFiles!=null) {
			int count = playingPathFiles.size();
			for (int j = 0; j < count; j++) {
				if (playingPathFiles.get(j).equals(playingSongString)) {
					if (direct==1) {  //next
						if (j+1!=count) {
							playingSongString = playingPathFiles.get(j+1);
							chooseSong(playingSongString);
						}
					}else {  //privous
						if (j!=0) {
							playingSongString = playingPathFiles.get(j-1);
							chooseSong(playingSongString);
						}
					}
					break;
				}
			}
		}
		setTitle(getString(R.string.app_name) + " -- " + modeString);
	} 

	private void play(Boolean msgBoolean, Boolean fromBoolean) { 
		Intent intent = new Intent(SitmanActivity.this,PlayerService.class);				
		if (isPlaying) {
			playButton.setImageResource(android.R.drawable.ic_media_play);
			intent.putExtra("PlayCommand", Constants.PLAYMESSAGE_PAUSE);
			intent.putExtra("PlayOrinal", fromBoolean);
			isPlaying = false;
		}else {
			playButton.setImageResource(android.R.drawable.ic_media_pause);
			intent.putExtra("PlayCommand", Constants.PLAYMESSAGE_PLAY); 
			intent.putExtra("PlayOrinal", fromBoolean);
			isPlaying = true; 
			
			if (isRecord) {
				isRecord = false;
				recordButton.setImageResource(android.R.drawable.ic_btn_speak_now);
				mRecorder.release();
				mRecorder = null;
				recordFile = null;
			}
		}

		if (msgBoolean) {
			startService(intent);
		}
		
	}
	
	private ArrayList<HashMap<String, Object>> fillArray(){
		ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = null;
		
		ArrayList<String> pointlist = myApplication.breakPointList;
		ArrayList<Boolean> chkList = myApplication.validPointList;

		for (int i = 0; i < pointlist.size(); i++) {
			map = new HashMap<String, Object>(); 
			map.put("Check", chkList.get(i));    
			map.put("ItemText", pointlist.get(i)); 
			items.add(map);
		}
		
		return items;
	}
	
	private void playRecord(){
		Intent intent = new Intent(SitmanActivity.this,PlayerService.class);
		intent.putExtra("PlayCommand", Constants.PLAYMESSAGE_RECORDSOUND);
		intent.putExtra("SongName", recordNameString); 
		startService(intent);
	}

	private void changePosition(int position, int changeMode) {
		if (playingSongString.length()>0) {
			Intent intent = new Intent(SitmanActivity.this, PlayerService.class);
			intent.putExtra("PlayCommand", changeMode);
			intent.putExtra("PlayPosition", position);
			startService(intent);
		}
	}

	private void chooseSong(String songString) {
		Intent intent = new Intent(SitmanActivity.this,PlayerService.class);
		intent.putExtra("PlayCommand", Constants.PLAYMESSAGE_SONG_CHANGE);
		intent.putExtra("SongName", songString);
		startService(intent);
		
		songTextView.setText(playingSongString);
	}
	
	private void changeSetting() {
//		Intent intent = new Intent(SitmanActivity.this,PlayerService.class);
//		intent.putExtra("PlayCommand", Constants.PLAYMESSAGE_SETTING);
//		startService(intent);
	}
	
	private void changePlayArea(int pos) {
		Intent intent = new Intent(SitmanActivity.this,PlayerService.class);
		intent.putExtra("PlayCommand", Constants.PLAYMESSAGE_CHANGEAREA);
		intent.putExtra("PlayAreaNum", pos);
		startService(intent);
	}
	
    /* 
     * 监听电话状态 
     */  
    final class MyPhoneStateListener extends PhoneStateListener {  
        public void onCallStateChanged(int state, String incomingNumber) {  
        	if (state!=TelephonyManager.CALL_STATE_IDLE) {
        		play(false , false); 
			}        	
        }
    }
	
	class LrcMessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int category = intent.getIntExtra("MessageType", 0);
			switch (category) {
				case Constants.FEED_CHANGE_SONG:
					playButton.setImageResource(android.R.drawable.ic_media_pause);
					break;
					
				case Constants.FEED_UPDATE_TEXT:
					contentString = intent.getStringExtra("SongText");
					contentString = contentString==null? "":contentString;
					readTextView.setText(contentString);
					break;
					
				case Constants.FEED_UPDATE_PROCESS:
					currentPosition = intent.getIntExtra("CurrentProcess", 0);
					processSeekBar.setProgress(currentPosition);
			        
					int hours =  currentPosition/MyApplication.HOUR;
					int rest = currentPosition%MyApplication.HOUR;
					int minutes = rest/MyApplication.MINUTE;
					rest = rest%MyApplication.MINUTE;
					int seconds = rest/MyApplication.SECOND;
					rest = rest%MyApplication.SECOND;
					int millSeconds = rest/MyApplication.MiLLSECOND;					
					String currentString = (hours<9? "0" + hours:"" + hours) + ":" + (minutes<9? "0" + minutes: "" + minutes) + ":" + (seconds<9? "0" + seconds:"" + seconds) + "." + millSeconds;
					
					hours = processSeekBar.getMax()/MyApplication.HOUR;
					rest = processSeekBar.getMax()%MyApplication.HOUR;
					minutes = rest/MyApplication.MINUTE;
					rest = rest%MyApplication.MINUTE;
					seconds = rest/MyApplication.SECOND;
					rest = rest%MyApplication.SECOND;
					millSeconds = rest/MyApplication.MiLLSECOND;					
					String maxString = (hours<9? "0" + hours:"" + hours) + ":" + (minutes<9? "0" + minutes: "" + minutes) + ":" + (seconds<9? "0" + seconds:"" + seconds) + "." + millSeconds;
					
			        playProcesstTextView.setText(String.format(getString(R.string.play_process), currentString , maxString));
			        currentTimeTextView.setText(String.format(getString(R.string.current_area),intent.getStringExtra("BeginArea"),"end".equals(intent.getStringExtra("EndArea"))==true? maxString:intent.getStringExtra("EndArea") ));
					break;
					
				case Constants.FEED_CHANGE_LENGTH:
					maxPosition = intent.getIntExtra("MaxProcess", 0);
					processSeekBar.setMax(maxPosition);
					break;
				
				case Constants.FEED_UPDATE_POINTLIST:
			        listItem = fillArray();  
			        CategoryAdapter adapterPointList = new CategoryAdapter(SitmanActivity.this,listItem);
					breakListView.setAdapter(adapterPointList);
					breakListView.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							// TODO Auto-generated method stub
								
//							LinearLayout layout = (LinearLayout) view.getParent();
							CheckBox box = (CheckBox) ((ViewGroup)view).getChildAt(0); 
							boolean flag = true;
							
							if (box.isChecked()) {
								flag =  false;
							}
							
							box.setChecked(flag);
							listItem.get(position).put("Check", flag);
							myApplication.validPointList.set(position, flag);
							
							switch (myApplication.playMode) {
								case Constants.PLAYMODE_NORMAL:
								case Constants.PLAYMODE_REPEATAREA:
								case Constants.PLAYMODE_RANDOM:
									changePlayArea(position);
									break;
									
								case Constants.PLAYMODE_LIMITREPEATAREA:
									if (flag) {
										changePlayArea(position);
									}
									break;
							}
						}
					});

					break;
					
				case Constants.FEED_UPDATE_STATE:
					
					int state = intent.getIntExtra("PlayState", Constants.PLAYER_STATE_PAUSE);
					
					switch (state) {
					
						case Constants.PLAYER_STATE_PLAYING:
							isPlaying = true;
							playButton.setImageResource(android.R.drawable.ic_media_pause);
							break;
	
						case Constants.PLAYER_STATE_PAUSE:
						case Constants.PLAYER_STATE_STOP:
							isPlaying = false;
							playButton.setImageResource(android.R.drawable.ic_media_play);							
							break;							
					}

					break;
			}	
		}
	}
	
    /* 
     * 收到广播时暂停 
     */  
    private final class PhoneListener extends BroadcastReceiver {  
        public void onReceive(Context context, Intent intent) {  
            play(false , false);   
        }  
    }
    
	public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {
		
		switch (requestCode) {
		
			case CHOOSESONGCODE:
		        if (resultCode == Activity.RESULT_OK) {
		        	playingSongString = data.getStringExtra(FileDialog.RESULT_PATH);
		            chooseSong(playingSongString);
		            
		    		String nameString;
		    		int pos;
		    		playingPathFiles = new ArrayList<String>();
		    		File file = new File(playingSongString.substring(0, playingSongString.lastIndexOf('/')+1));
		    		for (File fileItem : file.listFiles()) {
		            	nameString  = fileItem.getName();
    		        	pos = nameString.lastIndexOf('.');
    		        	if (pos!=-1 && ".mp3".equalsIgnoreCase(nameString.substring(pos, nameString.length()))) {
    		        		playingPathFiles.add(0,fileItem.getAbsolutePath());
    					}
		    		}		    				        	
				}
		        
				break;
	
			case SETTINGCODE:
		        if (resultCode == Activity.RESULT_OK) {

					switch (myApplication.playMode) {
						case Constants.PLAYMODE_NORMAL:
							modeString = "normal";
							break;
						case Constants.PLAYMODE_REPEATAREA:
							modeString = "repeat";
							break;	
						case Constants.PLAYMODE_LIMITREPEATAREA:
							modeString = "limited repeat";
							break;
						case Constants.PLAYMODE_RANDOM:
							changePosition(processSeekBar.getProgress(), Constants.PLAYMESSAGE_POSITION_CHANGE); 
							modeString = "random";
							break;							
					}
					
					setTitle(getString(R.string.app_name) + " -- " + modeString);
				}
		        
				break;
		}
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.setting:
				startActivityForResult(new Intent(SitmanActivity.this, SetOptiongActivity.class), SETTINGCODE);
				break;
				
			case R.id.choose:
				startActivityForResult(new Intent(SitmanActivity.this, FileDialog.class), CHOOSESONGCODE);
				break;
				
//			case R.id.download:
//				startActivityForResult(new Intent(SitmanActivity.this, SetOptiongActivity.class), CHOOSESONGCODE);
//				break;	
		}
		return super.onOptionsItemSelected(item);
	}

	private class CategoryAdapter  extends BaseAdapter {   
  	  
	    private LayoutInflater mInflater = null;
	    private ArrayList<HashMap<String, Object>> listItems;

		public CategoryAdapter(Context context, ArrayList<HashMap<String, Object>> listItems) {   
	        this.mInflater = LayoutInflater.from(context);
	        this.listItems = listItems;
	    }   
	  
	    @Override
	    public int getCount() {   
	        return listItems==null? 0 : listItems.size(); 
	    }   
	  
	    @Override
	    public Object getItem(int position) {   
	        return listItems==null? null : position;   
	    }   
	  
	    @Override
	    public long getItemId(int position) {   
	        return position;   
	    }   
	  
		public View getView(final int position, View convertView, ViewGroup parent) {

			final Holder holder;
			
			if (position >= 0 && position < listItems.size()){
				
				if (convertView==null) {
					convertView = mInflater.inflate(R.layout.list_item, parent, false);
					holder = new Holder();   
					holder.checkBox = (CheckBox)convertView.findViewById(R.id.chk_point);
					holder.timeTextView = (TextView)convertView.findViewById(R.id.break_point);
					convertView.setTag(holder);
				}else {
					holder = (Holder) convertView.getTag();
				}
				
				holder.timeTextView.setText((CharSequence) listItems.get(position).get("ItemText"));				
				holder.checkBox.setChecked((Boolean) listItems.get(position).get("Check"));

			}
			return convertView;
		}
		
	}
	
    static class Holder {
    	CheckBox checkBox;
    	TextView timeTextView;
    }
    
    class ImageAdapter extends BaseAdapter {
    	
    	private Context mContext;  //define Context 

        private Integer[] mImageIds = {  //picture source
                R.drawable.icon,
                R.drawable.icon,
                R.drawable.icon,
                R.drawable.icon_choose_music,
                R.drawable.icon_choose_music,
                R.drawable.icon_option,
                R.drawable.icon_option
        };

        public ImageAdapter(Context c) {  //define ImageAdapter
            mContext = c;
        }

        //get the picture number
        public int getCount() { 
            return mImageIds.length;
        }
        
        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);
            i.setImageResource(mImageIds[position]);  //set resource for the imageView
            i.setLayoutParams(new Gallery.LayoutParams(50, 80));  //layout
            i.setScaleType(ImageView.ScaleType.FIT_XY);   //set scale type
            return i;
        }
    }

}