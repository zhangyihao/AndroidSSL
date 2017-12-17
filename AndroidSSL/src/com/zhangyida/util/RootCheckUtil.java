package com.zhangyida.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

/**
 * 
 * @author zhangyihao
 *
 */
public class RootCheckUtil {

	private static final String LOG_TAG = "rootcheck";
	
	public static boolean checkIsRoot() {
		if(checkRootPathSU()) {
			return true;
		}
		return false;
	}
	
	private static boolean checkRootPathSU() {
		File f = null;
		final String[] kSuSearchPaths = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/"};
		try {
			for (int i = 0; i < kSuSearchPaths.length; i++) {
				f = new File(kSuSearchPaths[i] + "su");
				if (f != null && f.exists()) {
					Log.i(LOG_TAG,"find su in : "+kSuSearchPaths[i]);
					if(canExecuteCommand(kSuSearchPaths[i]+"which su")) {
						Log.i(LOG_TAG,"execute whiec su in : "+kSuSearchPaths[i]);
						return true;
					}
				}
			}
			if(canExecuteCommand("busybox which su") || canExecuteCommand("which su")) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}
	
	private static boolean canExecuteCommand(String command) {
        Process process = null;
        BufferedReader in = null;
        String result = null;
        try {
            process = Runtime.getRuntime().exec(command);
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            result = in.readLine();
        } catch (Exception e) {
            //do noting
        } finally {
        	if(in != null) {
        		try {
					in.close();
				} catch (IOException e) {
				}
        	}
            if (process != null) {
            	process.destroy();
            }
        }
        return (null != result);
    }

}