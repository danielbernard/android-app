package io.wireless.ball.android.ui.corelist;

import static org.solemnsilence.util.Py.truthy;
import io.wireless.ball.android.R;
import io.wireless.ball.android.app.DeviceState;
import io.wireless.ball.android.cloud.api.Device;
import io.wireless.ball.android.smartconfig.SmartConfigState;
import io.wireless.ball.android.ui.BaseActivity;
import io.wireless.ball.android.ui.eesd.EesdFragment;
import io.wireless.ball.android.ui.smartconfig.SmartConfigActivity;
import io.wireless.ball.android.ui.util.Ui;

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
import android.widget.EditText;
import android.widget.SeekBar;


public class CoreListActivity extends BaseActivity implements CoreListFragment.Callbacks {
	public static Device deviceById;
	private static final TLog log = new TLog(CoreListActivity.class);


	public static final String ARG_SKIP_TO_SMART_CONFIG = "ARG_SKIP_TO_SMART_CONFIG";
	public static final String ARG_ENTERING_FROM_LAUNCH = "ARG_ENTERING_FROM_LAUNCH";
	public static final String ARG_SELECT_DEVICE_ID = "ARG_SELECT_DEVICE_ID";

	private static final String STATE_SELECTED_DEVICE_ID = "STATE_SELECTED_DEVICE_ID";
	private static final String STATE_PANE_OPEN = "STATE_PANE_OPEN";


	private LayerDrawable actionBarBackgroundDrawable;
	private ActionBar actionBar;
	private SlidingPaneLayout slidingLayout;
	private String selectedItemId;

	public int red = 50;
	public int green = 0;
	public int blue = 0;
	public SeekBar seekBarR;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_core_list);
		
		// Add SeekBars
		
		seekBarR = (SeekBar)findViewById(R.id.seekBar_r);
		seekBarR.setMax(32);
		
		SeekBar.OnSeekBarChangeListener rlisten = new SeekBar.OnSeekBarChangeListener(){
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {			
				setR(progress);
				String color = Integer.toHexString(seekBarR.getProgress()) + "0000";
				//Log.d("Listener", "Progress is " + Integer.toString(progress));
				//Log.d("Listener","setRgbl color " + color);
				//Log.d("Listener","setRgbl progress " + Integer.toString(seekBarR.getProgress()));
				//Core can't handle update at every new progress
				//api.setRgbl(deviceById.id, color);
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
		
		seekBarR.setOnSeekBarChangeListener(rlisten);
		
		String deviceIdToSelect = null;
		boolean openPane = true;

		// The below is to try to present the user with the "best"
		// activity on launch, but still allowing them to return to the Core
		// list.
		Intent intentToSkipTo = null;
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_SELECTED_DEVICE_ID)) {
				deviceIdToSelect = savedInstanceState.getString(STATE_SELECTED_DEVICE_ID);
			}
			if (savedInstanceState.containsKey(STATE_PANE_OPEN)) {
				openPane = savedInstanceState.getBoolean(STATE_PANE_OPEN);
			}

		} else if (getIntent().hasExtra(ARG_SKIP_TO_SMART_CONFIG)) {
			getIntent().removeExtra(ARG_SKIP_TO_SMART_CONFIG);
			intentToSkipTo = new Intent(this, SmartConfigActivity.class);

		} else if (getIntent().hasExtra(ARG_ENTERING_FROM_LAUNCH)) {
			log.i("Known devices count: " + DeviceState.getKnownDevices().size());
			if (DeviceState.getKnownDevices().isEmpty()) {
				intentToSkipTo = new Intent(this, SmartConfigActivity.class);

			} else if (DeviceState.getKnownDevices().size() == 1) {
				Device device = DeviceState.getKnownDevices().get(0);
				deviceIdToSelect = device.id;
			}

			getIntent().removeExtra(ARG_ENTERING_FROM_LAUNCH);

		} else if (getIntent().hasExtra(ARG_SELECT_DEVICE_ID)) {
			deviceIdToSelect = getIntent().getStringExtra(ARG_SELECT_DEVICE_ID);
			getIntent().removeExtra(ARG_SELECT_DEVICE_ID);
		}

		// NOTE EARLY RETURN!
		if (intentToSkipTo != null) {
			startActivity(intentToSkipTo);
			finish();
			return;
		}


		actionBar = getActionBar();

		initActionBar();

		slidingLayout = (SlidingPaneLayout) findViewById(R.id.sliding_pane_layout);

		slidingLayout.setPanelSlideListener(new SliderListener());
		slidingLayout.getViewTreeObserver().addOnGlobalLayoutListener(new InitialLayoutListener());

		if (openPane) {
			slidingLayout.openPane();
		} else {
			slidingLayout.closePane();
		}

		if (deviceIdToSelect != null) {
			onItemSelected(deviceIdToSelect);
		}
		
	}
	


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.hasExtra(ARG_SELECT_DEVICE_ID)) {
			String deviceIdToSelect = intent.getStringExtra(ARG_SELECT_DEVICE_ID);
			intent.removeExtra(ARG_SELECT_DEVICE_ID);
			onItemSelected(deviceIdToSelect);
		}
	}

	private void initActionBar() {
		// this is such a rad effect. Huge props to Cyril Mottier for his
		// "Pushing the ActionBar to the Next Level" article, which inspired the
		// basis of this
		actionBarBackgroundDrawable = (LayerDrawable) getResources().getDrawable(
				R.drawable.action_bar_layers);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			actionBarBackgroundDrawable.setCallback(new Drawable.Callback() {

				@Override
				public void invalidateDrawable(Drawable who) {
					getActionBar().setBackgroundDrawable(who);
				}

				@Override
				public void scheduleDrawable(Drawable who, Runnable what, long when) {
				}

				@Override
				public void unscheduleDrawable(Drawable who, Runnable what) {
				}
			});
		}
		actionBar.setBackgroundDrawable(actionBarBackgroundDrawable);
		actionBarBackgroundDrawable.getDrawable(1).setAlpha(0);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_PANE_OPEN, slidingLayout.isOpen());
		if (selectedItemId != null) {
			outState.putString(STATE_SELECTED_DEVICE_ID, selectedItemId);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		SmartConfigState.clearSmartConfigData();
		api.requestAllDevices();
		if (selectedItemId == null && !DeviceState.getKnownDevices().isEmpty()) {
			onItemSelected(DeviceState.getKnownDevices().get(0).id);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.action_set_up_a_new_core:
				startActivity(new Intent(this, SmartConfigActivity.class));
				return true;

			case android.R.id.home:
				if (!slidingLayout.isOpen()) {
					slidingLayout.openPane();
					return true;
				}
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Callback method from {@link CoreListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		// same item selected, just close the pane
		if (id.equals(selectedItemId)) {
			slidingLayout.closePane();
			return;
		}

		deviceById = DeviceState.getDeviceById(id);
		setCustomActionBarTitle(deviceById.name);

		selectedItemId = id;
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.eesd_container, EesdFragment.newInstance(id))
				.commit();

		CoreListFragment listFrag = Ui.findFrag(this, R.id.core_list);
		listFrag.setActivatedItem(selectedItemId);
		slidingLayout.closePane();
	}

	@Override
	public void onBackPressed() {
		if (!slidingLayout.isOpen()) {
			slidingLayout.openPane();
		} else {
			super.onBackPressed();
		}
	}

	protected boolean shouldShowUpButtonWhenDevicesListNotEmpty() {
		return false;
	}

	private void panelOpened() {
		Fragment eesdFrag = Ui.findFrag(this, R.id.eesd_container);

		if (eesdFrag == null) {
			log.v("Eesd fragment is null");
		}

		if (slidingLayout.isSlideable()) {
			Ui.findFrag(this, R.id.core_list).setHasOptionsMenu(true);
			if (eesdFrag != null) {
				eesdFrag.setHasOptionsMenu(false);
			}
		} else {
			Ui.findFrag(this, R.id.core_list).setHasOptionsMenu(true);
			if (eesdFrag != null) {
				eesdFrag.setHasOptionsMenu(true);
			}
		}

		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		setCustomActionBarTitle(getString(R.string.app_name_lower));
	}

	private void panelClosed() {
		Ui.findFrag(this, R.id.core_list).setHasOptionsMenu(false);
		Fragment eesdFragment = Ui.findFrag(this, R.id.eesd_container);
		if (eesdFragment != null) {
			eesdFragment.setHasOptionsMenu(true);
		}

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		if (selectedItemId != null) {
			Device selectedDevice = DeviceState.getDeviceById(selectedItemId);
			if (selectedDevice != null && truthy(selectedDevice.name)) {
				setCustomActionBarTitle(selectedDevice.name);
			} else {
				setCustomActionBarTitle(getString(R.string._unnamed_core_));
			}
		} else {
			log.wtf("Selected item is null?");
		}
	}


	private class SliderListener extends SlidingPaneLayout.SimplePanelSlideListener {

		@Override
		public void onPanelOpened(View panel) {
			panelOpened();
		}

		@Override
		public void onPanelClosed(View panel) {
			panelClosed();
		}

		@Override
		public void onPanelSlide(View view, float v) {
			final int newAlpha = (int) (v * 255 * 0.5);
			actionBarBackgroundDrawable.getDrawable(1).setAlpha(newAlpha);
		}
	}


	private class InitialLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {

		@SuppressWarnings("deprecation")
		@SuppressLint("NewApi")
		@Override
		public void onGlobalLayout() {
			if (slidingLayout.isSlideable() && !slidingLayout.isOpen()) {
				panelClosed();
			} else {
				panelOpened();
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				slidingLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			} else {
				slidingLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		}
	}
	
	
	//Our Janky Method. It works!
	public void toggleActivation(View view) {
		api.toggleActivation(deviceById.id);
		Log.d("button","toggleActivation called");
	}
	
	//Tester Button
//	public void grabColor(View view){
//		EditText editText = (EditText)findViewById(R.id.edittext_grab_color);
//		String color = editText.getText().toString();
//		api.setRgbl(deviceById.id, color);
//	}
	
	public void setRgbl(View view) {
		double progress = seekBarR.getProgress();
		int red = (int) (Math.sin(.2 * progress) * 127 + 128);
		int green = (int) (Math.sin(.2 * progress + 2.094) * 127 + 128);
		int blue = (int) (Math.sin(.2 * progress + 4.187) * 127 + 128);
		
		String rColor;
		String gColor;
		String bColor;
		
		rColor = intToHexString(red);
		gColor = intToHexString(green);
		bColor = intToHexString(blue);
		String color = rColor + gColor + bColor;
		api.setRgbl(deviceById.id, color);
		
		Log.d("button","setRgbl color " + rColor + " " + gColor + " " + bColor);
		Log.d("button","setRgbl progress " + Integer.toString(seekBarR.getProgress()));
	}
	
	//properly converts an int to a hex string of length 2
	public String intToHexString(int color){
		String colorString;
		if (color < 15){
			colorString = "0" + Integer.toHexString(color);
		} else {
			colorString = Integer.toHexString(color);
		}
		return colorString;
	}
	
	public void rainbow(View view) {
		api.rainbow(deviceById.id);
		Log.d("button","rainbow called");
	}
	public void blinkLed(View view) {
//		String color = Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b);
		String color = "630063";
		String rate = "1000";
		int iter = 5; 
		api.blinkLed(deviceById.id, color, rate, iter);
		Log.d("button","blinkLed called");
	}

	
	//Get/Set Red
	public void setR(int newR) {
		this.red = newR;
	}
	
	public int getR(){
		return this.red;
	}
	
	//Get/Set Green 
	public void setG(int newG) {
		this.green = newG;
	}
	
	public int getG(){
		return this.green;
	}
	
	//Get/Set Blue
	public void setB(int newB) {
		this.blue = newB;
	}
	
	public int getB(){
		return this.blue;
	}
	

/*	// Below methods are the listeners for setting and changing R, G, and B based on sliders
	private class eesdSeekBarRListener implements SeekBar.OnSeekBarChangeListener {

		//Constructor
		public eesdSeekBarRListener(){
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {			
			if(fromUser = true){
				CoreListActivity.this.setR(progress);			
			}
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Toast.makeText(CoreListActivity.this, "Red: "+ CoreListActivity.this.getR(), Toast.LENGTH_SHORT).show();
		}
	}
	
	private class eesdSeekBarGListener implements SeekBar.OnSeekBarChangeListener {
		
		//Constrcutor
		public eesdSeekBarGListener(){
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if(fromUser = true){
				CoreListActivity.this.setG(progress);
			}
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Toast.makeText(CoreListActivity.this, "Green: "+ CoreListActivity.this.getG(), Toast.LENGTH_SHORT).show();
		}
	}
	
	private class eesdSeekBarBListener implements SeekBar.OnSeekBarChangeListener {
		
		//Constructor
		public eesdSeekBarBListener(){
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if(fromUser = true){
				CoreListActivity.this.setB(progress);
			}
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Toast.makeText(CoreListActivity.this, "Blue: "+ CoreListActivity.this.getB(), Toast.LENGTH_SHORT).show();
		}
	}
	*/

}


