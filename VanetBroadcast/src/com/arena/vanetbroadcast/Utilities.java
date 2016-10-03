package com.arena.vanetbroadcast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.io.*;
import java.util.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;

public class Utilities {
	static final int SEND_MSG = 1;
	static final int RECV_MSG = 2;
	static long m_lnRecvSize = 0;
	static int m_nSendIndex = 0;
	static int m_nRecvIndex = 0;
	
	public static String leftPad(int n, int padding) {
		return String.format("%0" + padding + "d", n);
	}

	public static String leftPad(long lnum, int padding) {
		return String.format("%0" + padding + "d", lnum);
	}
	
	public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Get Local IP Exception", ex.toString());
        }
        
        return null;
    }

	
	/**
     * Get IP address from first non-localhost interface
     * @param ipv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        
        return "";
    }
	
	
	public static boolean StoreByteImage(Context mContext, byte[] imageData, int quality, String nameFile) {

        File sdImageMainDirectory = new File("/sdcard/CPS210LOG");
		FileOutputStream fileOutputStream = null;
		
		try {

			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 5;
			
			Bitmap myImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length,options);

			
			fileOutputStream = new FileOutputStream(sdImageMainDirectory.toString() +"/" + nameFile + ".jpg");
			Log.d("fileclass", sdImageMainDirectory.toString() +"/" + nameFile + ".jpg");
							
  
			BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

			myImage.compress(CompressFormat.JPEG, quality, bos);

			bos.flush();
			bos.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.d("fileclass", "no jpg file found");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	
	
}


/*
        FileOutputStream outStream = null;
               
        try {
        	//create directory

			File newPicDir = new File(TagSense.rootDir + CameraPreview.pictureTime);
			if (!newPicDir.exists()){
				newPicDir.mkdirs();
			}
			// create a File object for the output file
			File outputFile = new File(newPicDir, CameraPreview.pictureTime+".jpg");        	
            // Write to SD Card
            outStream = new FileOutputStream(outputFile); 
            outStream.write(data);
            outStream.close();
            //Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);



        } catch (FileNotFoundException e) { 
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
        }

 
 */
