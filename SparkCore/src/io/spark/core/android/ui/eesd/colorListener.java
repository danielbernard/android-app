package io.spark.core.android.ui.eesd;

import android.R;
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
			colorListener.this.setColor(progress);
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}
	
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		//Toast.makeText(CoreListActivity.this, "Red: "+ CoreListActivity.this.getR(), Toast.LENGTH_SHORT).show();
	}

}
