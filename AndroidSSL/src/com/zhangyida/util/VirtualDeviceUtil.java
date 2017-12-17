package com.zhangyida.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 
 * @author zhangyihao
 *
 */
public class VirtualDeviceUtil {

	private static String TAG = "virtual";
	private static String[] imeis = {"000000000000000"};
	private static String[] imsiIds = {"310260000000000"};
	private static String[] known_files = {"/system/lib/libc_malloc_debug_qemu.so","/sys/qemu_trace","/system/bin/qemu-props"};
	private static String[] known_qemu_drivers = {"goldfish"};
	
	public static boolean checkVirtualDevice(Context context) {
		return isEmulatorByImei(context) || checkEmulatorBuild() || checkEmulatorFiles() || checkQEmuDriverFile();
	}
	
	private static boolean isEmulatorByImei(Context context){
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  
        String imei = tm.getDeviceId();
        String imsiID =tm.getSubscriberId();
        
        for (String knowDeviceid : imeis) {
            if (knowDeviceid.equalsIgnoreCase(imei)) {
                Log.v(TAG, "Find device id: "+knowDeviceid);
                return true;
            }
        }
        
        for (String knowImsiId : imsiIds) {
            if (knowImsiId.equalsIgnoreCase(imsiID)) {
                Log.v(TAG, "Find imis id: "+knowImsiId);
                return true;
            }
        }
        
        return false;  
    }
	
	private static boolean checkQEmuDriverFile() {
        File driverFile = new File("/proc/tty/drivers");
        if (driverFile.exists() && driverFile.canRead()) {
            byte[] data = new byte[1024];
            try {
                InputStream inStream = new FileInputStream(driverFile);
                inStream.read(data);
                inStream.close();
            } catch (Exception e) {
            }
            String driverData = new String(data);
            for (String knownQemuDriver : known_qemu_drivers) {
                if (driverData.indexOf(knownQemuDriver) != -1) {
                    Log.i(TAG, "Find know_qemu_drivers!");
                    return true;
                }
            }
        }
        return false;
    }
    
	private static boolean checkEmulatorFiles() {
		//检测模拟器上特有的几个文件
        for (int i = 0; i < known_files.length; i++) {
            String fileName = known_files[i];
            File qemuFile = new File(fileName);
            if (qemuFile.exists()) {
                Log.v(TAG, "Find Emulator Files!");
                return true;
            }
        }
        return false;
    }
    
	private static boolean checkEmulatorBuild() {
		//检测手机上的一些硬件信息
        String model = android.os.Build.MODEL;
        if (model.equalsIgnoreCase("google_sdk") || model.equalsIgnoreCase("sdk")) {
            Log.v(TAG, "Find build modle is "+model);
            return true;
        }
        return false;
    }
	
}
