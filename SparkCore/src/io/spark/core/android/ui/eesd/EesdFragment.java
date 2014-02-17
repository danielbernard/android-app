package io.spark.core.android.ui.eesd;

import org.solemnsilence.util.TLog;

import io.spark.core.android.R;
import io.spark.core.android.app.DeviceState;
import io.spark.core.android.cloud.api.Device;
import io.spark.core.android.ui.BaseFragment;
import io.spark.core.android.ui.tinker.Pin;
import io.spark.core.android.ui.tinker.PinAction;
import io.spark.core.android.ui.util.NamingHelper;
import io.spark.core.android.ui.util.Ui;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class EesdFragment  extends BaseFragment implements OnClickListener {
	private static final TLog log = new TLog(EesdFragment.class);
	
	public static final String ARG_DEVICE_ID = "ARG_DEVICE_ID";

	AlertDialog selectDialog;
	private Device device;

	public static EesdFragment newInstance(String deviceId) {
		Bundle arguments = new Bundle();
		arguments.putString(EesdFragment.ARG_DEVICE_ID, deviceId);
		EesdFragment fragment = new EesdFragment();
		fragment.setArguments(arguments);
		return fragment;
	}
	
	public EesdFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (getArguments().containsKey(ARG_DEVICE_ID)) {
			device = DeviceState.getDeviceById(getArguments().getString(ARG_DEVICE_ID));
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loadViews();
		setupListeners();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.tinker, menu);
		inflater.inflate(R.menu.core_row_overflow, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_rename_core:
				new NamingHelper(getActivity(), api).showRenameDialog(device);
				return true;
/*
			case R.id.action_reflash_tinker:
				api.reflashTinker(device.id);
				return true;

			case R.id.action_clear_tinker:
				prefs.clearTinker(device.id);
				for (Pin pin : allPins) {
					pin.setConfiguredAction(PinAction.NONE);
					pin.reset();
				}
				return true;*/
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadViews() {
		// Implement
	}
	
	private void setupListeners() {
		// Implement
		Ui.findView(this, R.id.tinker_main).setOnClickListener(this);
	}
	
	private void onOptionSelect(OptionType selectedOption) {
		if (selectedOption != null) {
			switch (selectedOption) {
				case TOGGLE_ACTIVATION:
					doToggleActivation();
					break;
				case SET_RGBL:
					doSetRgbl();
					break;
				case RAINBOW:
					doRainbow();
					break;
				case BLINK_LED:
					doBlinkLed();
					break;
				default:
					break;
			}
		}
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.eesd_button_toggle_activation:
				onFunctionSelected(selectedOption, ButtonAction.TOGGLE_ACTIVATION);
				break;
			case R.id.eesd_button_set_rgbl:
				onFunctionSelected(selectedOption, ButtonAction.SET_RGBL);
				break;
			default:
				break;
				
		}
	}
	
	private void onFunctionSelected(Pin selectedPin, PinAction action) {
		if (selectDialog != null) {
			selectDialog.dismiss();
			selectDialog = null;
		}
		//toggleViewVisibilityWithFade(R.id.tinker_logo, true);

		//selectedPin.reset();
		//selectedPin.setConfiguredAction(action);
		prefs.savePinFunction(device.id, selectedPin.name, action);
		// hideTinkerSelect();
		// unmutePins();
	}

	@Override
	public int getContentViewLayoutId() {
		// TODO Auto-generated method stub
		return 0;
	}
}
