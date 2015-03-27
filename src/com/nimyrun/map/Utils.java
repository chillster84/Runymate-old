package com.nimyrun.map;

import java.io.InputStream;
import java.io.OutputStream;

import android.app.Service;
import android.text.format.Time;

public class Utils {
	
	private Service mService;
	private static Utils instance = null;
	
	public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }
	
	public void setService(Service service) {
        mService = service;
    }
  
    
    /********** Time **********/
    
    public static long currentTimeInMillis() {
        Time time = new Time();
        time.setToNow();
        return time.toMillis(false);
    }
	
	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}
}