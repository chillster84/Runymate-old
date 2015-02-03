package com.nimyrun.map;

import java.lang.reflect.Type;
import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/* @formatter:off */
/**
 * must import Gson .jar into project
 * 1. download https://code.google.com/p/google-gson/downloads/detail?name=google-gson-2.2.4-release.zip&can=1&q=
 * 2. right click on project, then go to properties
 * 3. go to java build path
 * 4. click on libraries
 * 5. click on add external jar
 * 6. choose the gson-2.2.4.jar and add it
 * @author hhaider
 *
 */
/* @formatter:on */

public class StoreRoutes {

	/**
	 * Using Gson, converts object into a json string, and stores with
	 * sharedPreferences
	 * 
	 * @param sharedPreferences
	 * @param routes
	 */
	public void storeRoutes(SharedPreferences sharedPreferences,
			List<Route> routes) {
		Editor editor = sharedPreferences.edit();
		String json = new Gson().toJson(routes);
		editor.putString("routes", json);
		editor.commit();
	}


	/**
	 * Retrieve json string from sharedPreferences, and using Gson converts the
	 * json string back to object
	 * 
	 * @param sharedPreferences
	 * @return
	 */
	public List<Route> retrieveRoutes(SharedPreferences sharedPreferences) {
		String json = sharedPreferences.getString("routes", null);
		Type type = new TypeToken<List<Route>>() {
		}.getType();
		List<Route> routes = new Gson().fromJson(json, type);
		return routes;
	}
}
