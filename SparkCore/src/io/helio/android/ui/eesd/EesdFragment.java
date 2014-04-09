package io.helio.android.ui.eesd;

import io.helio.android.ui.BaseFragment;
import io.helio.android.ui.corelist.CoreListActivity;
import io.helio.android.R;
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

	public EesdFragment() {
	}
	
	public static final String ARG_DEVICE_ID = "ARG_DEVICE_ID";
	
	public static EesdFragment newInstance(String deviceId) {
		Bundle arguments = new Bundle();
		arguments.putString(EesdFragment.ARG_DEVICE_ID, deviceId);
		EesdFragment fragment = new EesdFragment();
		return fragment;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public int getContentViewLayoutId() {
		return R.layout.fragment_eesd;
	}

}
