package com.musaulker.devfesttrvolleysample;

import android.app.Application;

public class App extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		init();
	}

	private void init() {
		MyVolley.init(this);
	}
}
