package io.wireless.ball.android.app;

import io.wireless.ball.android.cloud.WebHelpers;
import io.wireless.ball.android.storage.Prefs;
import io.wireless.ball.android.storage.TinkerPrefs;
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
