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

/**
 * A fragment representing a single Core detail screen. This fragment is either
 * contained in a {@link CoreListActivity} in two-pane mode (on tablets) or a
 * {@link CoreDetailActivity} on handsets.
 */
public class EesdFragment  extends BaseFragment {

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
		
//		Button button = (Button) findViewById(R.id.button_id); 
//		button.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View view) {
//				api.toggle_activation(device.id);
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
	
}
