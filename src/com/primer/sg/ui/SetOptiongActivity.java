package com.primer.sg.ui;

import android.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Spinner;

import com.primer.sg.data.Constants;
import com.primer.sg.data.MyApplication;

public class SetOptiongActivity  extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.option_setting);
	}

	public void dealButtonClick(View target){
		
		MyApplication myApplication = (MyApplication) getApplication();
		
		switch (target.getId()) {
			case R.id.ok_btn:
				//get play mode
				int index = (int) ((Spinner)findViewById(R.id.play_mode)).getSelectedItemId();
				switch (index) {
					case 1:
						myApplication.playMode = Constants.PLAYMODE_NORMAL;
						break;
					case 2:
						myApplication.playMode = Constants.PLAYMODE_REPEATAREA;
						break;
					case 3:
						myApplication.playMode = Constants.PLAYMODE_LIMITREPEATAREA;
						break;
					case 4:
						myApplication.playMode = Constants.PLAYMODE_RANDOM;
						break;						
				}
				//get repeat times
				String timeString = (String) ((Spinner)findViewById(R.id.repeat_time)).getSelectedItem();
				myApplication.repeatTimes = Integer.parseInt(timeString);
				//get hint type
				index = (int) ((Spinner)findViewById(R.id.repeat_option)).getSelectedItemId();
				if (index==1) {
					myApplication.repeatHintType = Constants.HINTTYPE_RING;
				}else {
					myApplication.repeatHintType = Constants.HINTTYPE_WAIT;
				}
				//is pause or not after repeat
//				if (findViewById(R.id.repeat_pause).isSelected()) {
//					myApplication.isPauseOverRepeat = true;
//				}else {
//					myApplication.isPauseOverRepeat = false;
//				}
				break;
	
			case R.id.cancel_btn:
				KeyEvent up = new KeyEvent(System.currentTimeMillis(),System.currentTimeMillis(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0);
				dispatchKeyEvent(up);
				finish();
				break;
		}
	}
	
}
