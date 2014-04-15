package io.helio.android.ui.corelist;

import static org.solemnsilence.util.Py.truthy;
import io.helio.android.R;
import io.helio.android.app.DeviceState;
import io.helio.android.cloud.api.Device;
import io.helio.android.smartconfig.SmartConfigState;
import io.helio.android.ui.BaseActivity;
import io.helio.android.ui.eesd.EesdFragment;
import io.helio.android.ui.smartconfig.SmartConfigActivity;
import io.helio.android.ui.util.NamingHelper;
import io.helio.android.ui.util.Ui;

import org.solemnsilence.util.TLog;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

public class CoreListActivity extends BaseActivity implements
		CoreListFragment.Callbacks {
	public static Device deviceById;
	private static final TLog log = new TLog(CoreListActivity.class);

	public static final String ARG_SKIP_TO_SMART_CONFIG = "ARG_SKIP_TO_SMART_CONFIG";
	public static final String ARG_ENTERING_FROM_LAUNCH = "ARG_ENTERING_FROM_LAUNCH";
	public static final String ARG_SELECT_DEVICE_ID = "ARG_SELECT_DEVICE_ID";

	private static final String STATE_SELECTED_DEVICE_ID = "STATE_SELECTED_DEVICE_ID";
	private static final String STATE_PANE_OPEN = "STATE_PANE_OPEN";

	private LayerDrawable actionBarBackgroundDrawable;
	private ActionBar actionBar;
	private DrawerLayout drawerLayout;
	private String selectedItemId;

	// EESD Global Variables
	private int batLevel;
	private ColorPicker colorPicker;
	private SaturationBar sBar;
	private ValueBar vBar;
	private int colorOld;
	private int colorNew;
	private Thread colorThread;
	private Thread batteryThread;
	private boolean isRunning;
	
	//EESD Handler to redraw menu for batterylevel updates
	@SuppressLint("HandlerLeak")
	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			invalidateOptionsMenu();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_core_list);

		//EESD
		colorPicker = (ColorPicker) findViewById(R.id.picker);
		sBar = (SaturationBar) findViewById(R.id.sbar);
		vBar = (ValueBar) findViewById(R.id.vbar);

		colorPicker.addSaturationBar(sBar);
		colorPicker.addValueBar(vBar);
		ColorPicker.OnColorChangedListener cListener = new ColorPicker.OnColorChangedListener() {

			@Override
			public void onColorChanged(int color) {
				// Log.d("PICKER", Integer.toHexString(color));
				colorPicker.setOldCenterColor(color);
				colorNew = color;
			}
		};
		colorPicker.setOnColorChangedListener(cListener);
		//end EESD

		String deviceIdToSelect = null;
		boolean openPane = true;

		// The below is to try to present the user with the "best"
		// activity on launch, but still allowing them to return to the Core
		// list.
		Intent intentToSkipTo = null;
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_SELECTED_DEVICE_ID)) {
				deviceIdToSelect = savedInstanceState
						.getString(STATE_SELECTED_DEVICE_ID);
			}
			if (savedInstanceState.containsKey(STATE_PANE_OPEN)) {
				openPane = savedInstanceState.getBoolean(STATE_PANE_OPEN);
			}

		} else if (getIntent().hasExtra(ARG_SKIP_TO_SMART_CONFIG)) {
			getIntent().removeExtra(ARG_SKIP_TO_SMART_CONFIG);
			intentToSkipTo = new Intent(this, SmartConfigActivity.class);

		} else if (getIntent().hasExtra(ARG_ENTERING_FROM_LAUNCH)) {
			log.i("Known devices count: "
					+ DeviceState.getKnownDevices().size());
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

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

//		drawerLayout.setDrawerListener((DrawerListener) new SliderListener());
		drawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new InitialLayoutListener());

//		if (openPane) {
//			drawerLayout.openDrawer();
//		} else {
//			drawerLayout.closeDrawer();
//		}

		if (deviceIdToSelect != null) {
			onItemSelected(deviceIdToSelect);
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.hasExtra(ARG_SELECT_DEVICE_ID)) {
			String deviceIdToSelect = intent
					.getStringExtra(ARG_SELECT_DEVICE_ID);
			intent.removeExtra(ARG_SELECT_DEVICE_ID);
			onItemSelected(deviceIdToSelect);
		}
	}

	private void initActionBar() {
		// this is such a rad effect. Huge props to Cyril Mottier for his
		// "Pushing the ActionBar to the Next Level" article, which inspired the
		// basis of this
		actionBarBackgroundDrawable = (LayerDrawable) getResources()
				.getDrawable(R.drawable.action_bar_layers);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			actionBarBackgroundDrawable.setCallback(new Drawable.Callback() {

				@Override
				public void invalidateDrawable(Drawable who) {
					getActionBar().setBackgroundDrawable(who);
				}

				@Override
				public void scheduleDrawable(Drawable who, Runnable what,
						long when) {
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
//		outState.putBoolean(STATE_PANE_OPEN, drawerLayout.isOpen());
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
		// EESD
		// The following code handles the menu button presses
		Log.d("MENU TEST", "Menu selected");
		switch (item.getItemId()) {
		
		case R.id.action_bat_level:
			Toast.makeText(getApplicationContext(), "BAT:" + Integer.toString(batLevel) + "%", Toast.LENGTH_SHORT).show();
			return true;

		case R.id.action_turn_on_off:
			api.toggleActivation(deviceById.id);
			return true;

		case R.id.action_rainbow:
			api.rainbow(deviceById.id);
			return true;

		case R.id.action_blink_led:
			String color = "630063";
			String rate = "0500";
			int iter = 2;
			api.blinkLed(deviceById.id, color, rate, iter);
			return true;

		case R.id.action_set_up_a_new_core:
			startActivity(new Intent(this, SmartConfigActivity.class));
			return true;
		
		case R.id.action_rename_core:
			new NamingHelper(this, api).showRenameDialog(deviceById);
			return true;

//		case android.R.id.home:
//			if (!drawerLayout.isOpen()) {
//				drawerLayout.openDrawer();
//				return true;
//			}
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
//			drawerLayout.closeDrawer();
			return;
		}

		deviceById = DeviceState.getDeviceById(id);
		setCustomActionBarTitle(deviceById.name);

		selectedItemId = id;
		getFragmentManager().beginTransaction()
				.replace(R.id.eesd_container, EesdFragment.newInstance(id))
				.commit();

		CoreListFragment listFrag = Ui.findFrag(this, R.id.core_list);
		listFrag.setActivatedItem(selectedItemId);
//		drawerLayout.closeDrawer();
	}

	@Override
	public void onBackPressed() {
//		if (!drawerLayout.isOpen()) {
//			drawerLayout.openDrawer();
//		} else {
			super.onBackPressed();
//		}
	}

	protected boolean shouldShowUpButtonWhenDevicesListNotEmpty() {
		return false;
	}

	private void drawerOpened() {
		Fragment eesdFrag = Ui.findFrag(this, R.id.eesd_container);

		if (eesdFrag == null) {
			log.v("Eesd fragment is null");
		}

//		if (drawerLayout.isSlideable()) {
			Ui.findFrag(this, R.id.core_list).setHasOptionsMenu(true);
			if (eesdFrag != null) {
				eesdFrag.setHasOptionsMenu(false);
//			}
//		} else {
//			Ui.findFrag(this, R.id.core_list).setHasOptionsMenu(true);
//			if (eesdFrag != null) {
//				eesdFrag.setHasOptionsMenu(true);
//			}
		}

		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		setCustomActionBarTitle(getString(R.string.app_name_lower));
	}

	private void drawerClosed() {
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

	private class MyDrawerListener implements DrawerLayout.DrawerListener {

		@Override
		public void onDrawerOpened(View drawer) {
			drawerOpened();
		}

		@Override
		public void onDrawerClosed(View drawer) {
			drawerClosed();
		}

		@Override
		public void onDrawerSlide(View view, float v) {
			final int newAlpha = (int) (v * 255 * 0.5);
			actionBarBackgroundDrawable.getDrawable(1).setAlpha(newAlpha);
		}

		@Override
		public void onDrawerStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}
	}

	private class InitialLayoutListener implements
			ViewTreeObserver.OnGlobalLayoutListener {

		@SuppressWarnings("deprecation")
		@SuppressLint("NewApi")
		@Override
		public void onGlobalLayout() {
//			if (slidingLayout.isSlideable() && !slidingLayout.isOpen()) {
//				panelClosed();
//			} else {
//				panelOpened();
//			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				drawerLayout.getViewTreeObserver()
						.removeOnGlobalLayoutListener(this);
			} else {
				drawerLayout.getViewTreeObserver()
						.removeGlobalOnLayoutListener(this);
			}
		}
	}

	// EESD All following defined Functions
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.eesd_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		menu.findItem(R.id.action_bat_level).setTitle("BAT:" + Integer.toString(batLevel) + "%");
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("LIFE CYCLE", "onResume called");
		isRunning = true;
		// EESD code
		//Thread handles the changing battery level on the ball
		Runnable batteryRunnable = new Runnable() {
			public void run() {
				batLevel = api.getBatteryLife(deviceById.id);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					Log.d("EXCEPTION", e1.toString());
					e1.printStackTrace();
				}
				while (isRunning) {
					batLevel = api.getBatteryLife(deviceById.id);
					//Log.d("TEST", "batLevel is: " + Integer.toString(batLevel));
					handler.sendMessage(new Message());
					try {
						Thread.sleep(5 * 1000);
					} catch (InterruptedException e) {
						Log.d("EXCEPTION", e.toString());
						e.printStackTrace();
					}
				}
			}
		};
		batteryThread = new Thread(batteryRunnable);
		batteryThread.start();

		//Thread handles the colorchanges on the colorwheel
		Runnable colorRunnable = new Runnable() {
			public void run() {
				String sargb;
				String srgb;
				while (isRunning) {
					if (colorOld != colorNew) {
						sargb = Integer.toHexString(colorNew);
						srgb = sargb.substring(2);
						api.setRgbl(deviceById.id, srgb);
						//Log.d("COLORTHREAD", "colorOld = " + Integer.toHexString(colorOld));
						//Log.d("COLORTHREAD", "colorNew = " + Integer.toHexString(colorNew));
						colorOld = colorNew;
					}

					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						Log.d("EXCEPTION", e.toString());
						e.printStackTrace();
					}
				}
			}
		};
		colorThread = new Thread(colorRunnable);
		colorThread.start();
	}

	@Override
	protected void onPause() {
		isRunning = false;
		Log.d("LIFE CYCLE", "onPause called");
		try {
			colorThread.join();
			batteryThread.join();
		} catch (Exception e) {
			Log.d("EXCEPTION", e.toString());
			e.printStackTrace();
		}
		//api.saveColor(deviceById.id);
		super.onPause();
	}

	public void toggleActivation(View view) {
		api.toggleActivation(deviceById.id);
	}

	public void rainbow(View view) {
		api.rainbow(deviceById.id);
		Log.d("button", "rainbow called");
	}

	public void blinkLed(View view) {
		String color = "630063";
		String rate = "0500";
		int iter = 2;
		api.blinkLed(deviceById.id, color, rate, iter);
		Log.d("button", "blinkLed called");
	}

}
