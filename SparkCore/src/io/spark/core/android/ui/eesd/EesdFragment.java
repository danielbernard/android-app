package io.spark.core.android.ui.eesd;

import io.spark.core.android.R;
import io.spark.core.android.cloud.api.Device;
import io.spark.core.android.ui.BaseFragment;
import io.spark.core.android.ui.corelist.CoreListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.SeekBar;

/**
 * A fragment representing a single Core detail screen. This fragment is either
 * contained in a {@link CoreListActivity} in two-pane mode (on tablets) or a
 *
 */
public class EesdFragment  extends BaseFragment {

	int r,g,b;
	SeekBar seekBarR;
	public EesdFragment() {
	}
	
	public static final String ARG_DEVICE_ID = "ARG_DEVICE_ID";
	
	private Device device;
	
	public static EesdFragment newInstance(String deviceId) {
		Bundle arguments = new Bundle();
		arguments.putString(EesdFragment.ARG_DEVICE_ID, deviceId);
		EesdFragment fragment = new EesdFragment();
		return fragment;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		
		//setContentView(R.layout.fragment_eesd);
		
		// Add SeekBars
		
		//Shitty code
		seekBarR = (SeekBar)getResources().getResourceName(R.id.seekBar_r);
		seekBarR.setMax(255);
		//SeekBar seekBarG = (SeekBar) findViewById(R.id.seekBar_g);
		//SeekBar seekBarB = (SeekBar) findViewById(R.id.seekBar_b);
		
		SeekBar.OnSeekBarChangeListener rlisten = new SeekBar.OnSeekBarChangeListener(){
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {			
				r = progress;
				Log.d("Listener", "Progress is " + Integer.toString(progress));
			}
		
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				Log.d("Listener", "Progress is start ");
			}
		
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.d("Listener", "Progress is stop ");
			}
		
		};
		
		
		// Crashes on next line, NullPointerException
		seekBarR.setOnSeekBarChangeListener(rlisten);
		
//		Button button = (Button) findViewById(R.id.button_id); 
//		button.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View view) {
//				api.toggleActivation(device.id);
//			}
//		});

	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.tinker, menu);
		inflater.inflate(R.menu.core_row_overflow, menu);
	}
	
//	public void eesdButton(View view) {
//		api.toggle_activation(device.id);
//		Log.d("button","BUTTON!");
//	}
	
	
	
/*	@Override
	public void onClick(View arg0) {
		api.toggle_activation(device.id);
		Log.d("onClick","onClick called");
	}*/

	@Override
	public int getContentViewLayoutId() {
		return R.layout.fragment_eesd;
	}
	
	public void setRgbl(View view) {
		String color = Integer.toHexString(r) + "AAAA";
		api.setRgbl(CoreListActivity.deviceById.id, color);
		Log.d("button","setRgbl color " + color);
		Log.d("button","setRgbl progress " + Integer.toString(seekBarR.getProgress()));
	}

}
