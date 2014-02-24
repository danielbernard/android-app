package io.spark.core.android.ui.eesd;

import android.R;
import android.util.Log;
import android.widget.SeekBar;

public class colorListener implements SeekBar.OnSeekBarChangeListener {
	
	//private SeekBar bar;
	private int color;
	
	//Constructor
	public colorListener(){

	}
	
	public colorListener(int colorInitial){
		this.setColor(colorInitial);
	}
	
	public void setColor(int color){
		this.color = color;
	}
	
	public int getcolor(){
		return color;
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {			
		if(fromUser = true){
			Log.d("Listener", "Progress is 1");
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		Log.d("Listener", "Progress is 2");
	}
	
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.d("Listener", "Progress is 3");
	}

}
