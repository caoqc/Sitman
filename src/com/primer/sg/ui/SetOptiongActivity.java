package com.primer.sg.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;
import com.primer.sg.R;
import com.primer.sg.data.Constants;
import com.primer.sg.data.MyApplication;

public class SetOptiongActivity  extends Activity{

	private Spinner playModeSpinner;
	private Spinner repeatTimeSpinner;
	private Spinner repeatOptionSpinner;
	private CheckBox pauseCheckBox;
	private MyApplication myApplication;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.option_setting);
		
		initActivity();
		
	}

	private void initActivity() {
		// TODO Auto-generated method stub
		playModeSpinner = (Spinner)findViewById(R.id.play_mode);
		repeatTimeSpinner = (Spinner)findViewById(R.id.repeat_time);
		repeatOptionSpinner = (Spinner)findViewById(R.id.repeat_option);
		pauseCheckBox = (CheckBox) findViewById(R.id.repeat_pause);
		
		myApplication = (MyApplication) getApplication();
		playModeSpinner.setSelection(myApplication.playMode-1);
		repeatTimeSpinner.setSelection(myApplication.repeatTimes-1);
		repeatOptionSpinner.setSelection(myApplication.repeatHintType-1);
		pauseCheckBox.setChecked(myApplication.isPauseOverRepeat);
	}

	public void dealButtonClick(View target){
		
		switch (target.getId()) {
		
			case R.id.ok_btn:
				//get play mode
				int index = (int) playModeSpinner.getSelectedItemId();
				switch (index) {
					case 0:
						myApplication.playMode = Constants.PLAYMODE_NORMAL;
						break;
					case 1:
						myApplication.playMode = Constants.PLAYMODE_REPEATAREA;
						break;
					case 2:
						myApplication.playMode = Constants.PLAYMODE_LIMITREPEATAREA;
						break;
					case 3:
						myApplication.playMode = Constants.PLAYMODE_RANDOM;
						break;						
				}
				
				//get repeat times
				String timeString = (String) repeatTimeSpinner.getSelectedItem();
				myApplication.repeatTimes = Integer.parseInt(timeString);
				
				//get hint type
				index = (int) repeatOptionSpinner.getSelectedItemId();
				if (index==0) {
					myApplication.repeatHintType = Constants.HINTTYPE_RING;
				}else {
					myApplication.repeatHintType = Constants.HINTTYPE_WAIT;
				}
				
				//is pause or not after repeat
				if (pauseCheckBox.isSelected()) {
					myApplication.isPauseOverRepeat = true;
				}else {
					myApplication.isPauseOverRepeat = false;
				}
				
				setResult(RESULT_OK);
				break;
	
			case R.id.cancel_btn:
				setResult(RESULT_CANCELED);
				break;
		}
		
		finish();
	}
	
}
