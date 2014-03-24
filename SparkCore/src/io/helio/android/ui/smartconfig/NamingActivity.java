package io.helio.android.ui.smartconfig;

import io.helio.android.ui.BaseActivity;
import io.helio.android.ui.corelist.CoreListActivity;
import io.helio.android.ui.util.Ui;
import io.helio.android.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.TextView;


public class NamingActivity extends BaseActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_smart_config);
		getFragmentManager()
				.beginTransaction()
				.add(R.id.smart_config_frag, new NamingFragment())
				.commit();

		TextView finePrint = Ui.findView(this, R.id.fine_print);
		finePrint.setText(R.string.you_can_change_these_names_at_any_point);
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
