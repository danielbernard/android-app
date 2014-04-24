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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.danh32.fontify.Switch;
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
	private SlidingPaneLayout slidingLayout;
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
	private NotificationReceiver nReceiver;
	private Switch mSwitch;
	private boolean notificationActive;

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
		
		nReceiver = new NotificationReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("io.helio.android.ui.eesd.NOTIFICATION_LISTENER");
		registerReceiver(nReceiver,filter);
		
		notificationActive = true;
		mSwitch = (Switch) findViewById(R.id.notification_switch);
		mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				notificationActive = isChecked;
				Log.d("NOTIFY", "notificationActive is: " + notificationActive);
			}
		});
		
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

		slidingLayout = (SlidingPaneLayout) findViewById(R.id.sliding_pane_layout);

		slidingLayout.setPanelSlideListener(new SliderListener());
		slidingLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new InitialLayoutListener());

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
		// EESD
		// The following code handles the menu button presses
		Log.d("MENU TEST", "Menu selected");
		switch (item.getItemId()) {

		case R.id.action_bat_level:
			Toast.makeText(getApplicationContext(), "BAT:" + Integer.toString(batLevel) + "%", Toast.LENGTH_SHORT).show();
			break;

		case R.id.action_turn_on_off:
			api.toggleActivation(deviceById.id);
			break;

		case R.id.action_rainbow:
			api.rainbow(deviceById.id);
			break;

		case R.id.action_set_up_a_new_core:
			startActivity(new Intent(this, SmartConfigActivity.class));
			break;

		case R.id.action_rename_core:
			new NamingHelper(this, api).showRenameDialog(deviceById);
			break;

		case android.R.id.home:
			if (!slidingLayout.isOpen()) {
				slidingLayout.openPane();
				break;
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
		getFragmentManager().beginTransaction()
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
			slidingLayout.closePane();
			//super.onBackPressed();
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

	private class SliderListener extends
	SlidingPaneLayout.SimplePanelSlideListener {

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

	private class InitialLayoutListener implements
	ViewTreeObserver.OnGlobalLayoutListener {

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
				slidingLayout.getViewTreeObserver()
				.removeOnGlobalLayoutListener(this);
			} else {
				slidingLayout.getViewTreeObserver()
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
		// EESD code
		Log.d("LIFE CYCLE", "onResume called");
		isRunning = true;
		//Thread handles the changing battery level on the ball
		Runnable batteryRunnable = new Runnable() {
			public void run() {

				while (isRunning) {
					try {
						batLevel = api.getBatteryLife(deviceById.id);
						Thread.sleep(5 * 1000);
						batLevel = api.getBatteryLife(deviceById.id);
						Thread.sleep(5 * 1000);
						handler.sendMessage(new Message());
						Thread.sleep(20 * 1000);
					} catch (InterruptedException e) {
						Log.d("EXCEPTION", e.toString());
						e.printStackTrace();
					}
				}
			}
		};
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
		batteryThread = new Thread(batteryRunnable);
		batteryThread.start();
		colorThread.start();
		
	}

	@Override
	protected void onPause() {
		super.onPause();
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
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(nReceiver);
		super.onDestroy();
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
	
	class NotificationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			if (notificationActive) {
				String pkgName = intent.getStringExtra("notification_package_name");
				int notifId = intent.getIntExtra("notification_id",0);
				Log.d("NOTIFY", pkgName);
				String notificationColor = "FF0000";
				String notificationType = "b";
				if ((notifId != 20001) && (pkgName != "com.google.android.googlequicksearchbox") && 
						(pkgName != "com.android.systemui")) {
					switch(pkgName) {
					case "com.google.android.apps.googlevoice":
						notificationColor = "00FF00";
						api.notifyUser(deviceById.id, notificationType, notificationColor);
						break;
					case "com.android.mms" :
						notificationColor = "00FF00";
						api.notifyUser(deviceById.id, notificationType, notificationColor);
						break;
					case "com.snapchat.android":
						notificationColor = "FFCC00";
						notificationType = "g";
						api.notifyUser(deviceById.id, notificationType, notificationColor);
						break;
					case "com.facebook.orca":
						notificationColor = "3B5999";
						api.notifyUser(deviceById.id, notificationType, notificationColor);
						break;
					case "com.facebook.katana":
						notificationColor = "3B5999";
						notificationType = "g";
						api.notifyUser(deviceById.id, notificationType, notificationColor);
						break;
					case "com.android.calendar":
						notificationColor = "FF00FF";
						api.notifyUser(deviceById.id, notificationType, notificationColor);
						break;
					default:
						break;
					}
				}
			}
		}
	}
}
