package io.helio.android.app;

import io.helio.android.cloud.WebHelpers;
import io.helio.android.storage.Prefs;
import io.helio.android.storage.TinkerPrefs;
import android.app.Application;


public class SparkCoreApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		AppConfig.initialize(this);
		Prefs.initialize(this);
		TinkerPrefs.initialize(this);
		WebHelpers.initialize(this);
		DeviceState.initialize(this);
	}

}
