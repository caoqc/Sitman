package com.primer.sg.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.primer.sg.R;
import com.primer.sg.data.Constants;
import com.primer.sg.data.MyApplication;
import com.primer.sg.service.PlayerService;

public class SitmanActivity extends Activity implements SeekBar.OnSeekBarChangeListener , OnItemClickListener{
	
	private TextView currentTimeTextView;
	private TextView playTypeStatusTextView;
	private TextView playProcesstTextView;
	
	private SeekBar processSeekBar;
	private TextView readTextView;
	private ListView breakListView;
	
	private ImageButton playButton;
	
	private LrcMessageReceiver receiver;
	private IntentFilter myIntentFilter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        //get view handler
        currentTimeTextView = (TextView) findViewById(R.id.current_time);
        playTypeStatusTextView = (TextView) findViewById(R.id.play_type_status);
        playProcesstTextView = (TextView) findViewById(R.id.play_process);
        processSeekBar = (SeekBar) findViewById(R.id.process_seekbar);
        readTextView = (TextView) findViewById(R.id.read_text);
        breakListView = (ListView) findViewById(R.id.break_list);
        playButton = (ImageButton) findViewById(R.id.play_btn);

        ArrayList<HashMap<String, Object>> listItem = fillArray();   	
		ListAdapter adapterList = new SimpleAdapter(this,listItem, R.layout.list_item,
				new String[] {"ItemText"},	new int[] {R.id.break_point});
		breakListView.setAdapter(adapterList);
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

	private IntentFilter getIntentFilter() {
		// TODO Auto-generated method stub
		if (myIntentFilter==null) {
			myIntentFilter = new IntentFilter();
			myIntentFilter.addAction(Constants.LRC_MESSAGE_ACTION);
		}
		return myIntentFilter;
	}

	public void btnClickDeal(View target) {
    	
    	Intent intent = null;
    	
		switch (target.getId()) {
			case R.id.option_btn: 
				intent = new Intent(this,SetOptiongActivity.class);
				startActivity(intent);
				break;
				
			case R.id.choose_btn: 
				intent = new Intent(this,FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, "/sdcard");
				startActivityForResult(intent, Constants.REQUEST_LOAD);
				break;			
				
			case R.id.ff_btn: 
				intent = new Intent(this,PlayerService.class);
				intent.putExtra("PlayCommand", Constants.PLAYMESSAGE_FF);				
				startService(intent);				
				break;
				
			case R.id.rew_btn:
				intent = new Intent(this,PlayerService.class);
				intent.putExtra("PlayCommand", Constants.PLAYMESSAGE_REW);				
				startService(intent);				
				break;
				
	/*		case R.id.prior_btn: 
				
				break;*/
				
	/*		case R.id.next_btn:
				
				break;*/
	
			case R.id.play_btn:
				intent = new Intent(SitmanActivity.this,PlayerService.class);				
				if (((MyApplication)getApplication()).isPlaying) {
					playButton.setImageResource(android.R.drawable.ic_media_play);
					intent.putExtra("PlayCommand", Constants.PLAYMESSAGE_PAUSE);
				}else {
					playButton.setImageResource(android.R.drawable.ic_media_pause);
					intent.putExtra("PlayCommand", Constants.PLAYMESSAGE_PLAY);
				}
				startService(intent);
				break;
				
		}
	}
	private ArrayList<HashMap<String, Object>> fillArray(){
		ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = null;
  
		String[] establishments = getResources().getStringArray(R.array.listarray);

		for (int i = 0; i < establishments.length; i++) {
			map = new HashMap<String, Object>(); 
			map.put("no", i);    
			map.put("ItemText", establishments[i]); 
			items.add(map);
		}
		
		return items;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// TODO Auto-generated method stub
		Toast.makeText(this, progress + "%" , Toast.LENGTH_LONG);
		
		Intent intent = new Intent(this,PlayerService.class);
		intent.putExtra("PlayCommand", Constants.PLAYMESSAGE_POSITION_CHANGE);
		intent.putExtra("PlayPosition", processSeekBar.getProgress());
		startService(intent);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub 
		//nothing
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		//nothing		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		// TODO Auto-generated method stub		
		ArrayList<HashMap<String, Object>> item = (ArrayList<HashMap<String, Object>>) parent.getItemAtPosition(position);
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
					String textString = intent.getStringExtra("TextMessage");
					textString = textString==null? "":textString;
					readTextView.setText(textString);
					break;
					
				case Constants.FEED_UPDATE_PROCESS:
					processSeekBar.setProgress(intent.getIntExtra("CurrentProcess", 0));
					break;
					
				case Constants.FEED_CHANGE_LENGTH:
					int maxProcess = intent.getIntExtra("MaxProcess", 0);
					processSeekBar.setMax(maxProcess);
					break;
					
				default:
					break;
			}	
		}
	}
	
	public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            ((MyApplication)getApplication()).playSong = data.getStringExtra(FileDialog.RESULT_PATH);
        }
        if (!((MyApplication)getApplication()).playSongBack.equalsIgnoreCase(((MyApplication)getApplication()).playSong)) {
        	Intent intent = new Intent(SitmanActivity.this,PlayerService.class);
        	intent.putExtra("PlayCommand", Constants.PLAYMESSAGE_SONG_CHANGE);
        	startService(intent);
		}

    }
}