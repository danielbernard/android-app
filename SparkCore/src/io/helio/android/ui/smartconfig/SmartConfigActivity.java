package io.helio.android.ui.smartconfig;

import io.helio.android.ui.BaseActivity;
import io.helio.android.ui.corelist.CoreListActivity;
import io.helio.android.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;


public class SmartConfigActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_smart_config);
		getFragmentManager()
				.beginTransaction()
				.add(R.id.smart_config_frag, new SmartConfigFragment())
				.commit();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpTo(this, new Intent(this, CoreListActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
