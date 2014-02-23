package io.spark.core.android.ui.corelist;

import static org.solemnsilence.util.Py.truthy;
import io.spark.core.android.R;
import io.spark.core.android.app.DeviceState;
import io.spark.core.android.cloud.api.Device;
import io.spark.core.android.smartconfig.SmartConfigState;
import io.spark.core.android.ui.BaseActivity;
//import io.spark.core.android.ui.corelist.CoreListActivity.eesdSeekBarRListener;
import io.spark.core.android.ui.eesd.EesdFragment;
import io.spark.core.android.ui.smartconfig.SmartConfigActivity;
import io.spark.core.android.ui.util.Ui;

import org.solemnsilence.util.TLog;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class RedListener extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
	
	private SeekBar redBar;
	private int red;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Add SeekBars
		SeekBar seekBarR = (SeekBar) findViewById(R.id.seekBar_r);
		seekBarR.setOnSeekBarChangeListener(this);
	}
	
	public void setRed(int red){
		this.red = red;
	}
	
	public int getRed(){
		return red;
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {			
		if(fromUser = true){
			RedListener.this.setRed(progress);
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
