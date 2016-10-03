package com.arena.vanetbroadcast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.net.*;

import android.os.Environment;
import android.os.Looper;
import android.util.*;
import android.app.*;

import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.os.Message;
import android.content.res.*;
import android.app.*;
import android.util.*;
import java.util.Calendar;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class DataSender implements Runnable {
	
	private static final String BROADCAST_ADDR = "169.254.255.255";  //Broadcast address
//	private static final String BROADCAST_ADDR = "255.255.255.255";  //Broadcast address

	private static final int DEST_PORT = 8888;
	private static final int BUF_SIZE = 60*1024;  //50KB
		
	private static String strSmallData = "mengrufeng0123456789"; //Small size data to broadcast
	private static String strMedianData = "Median.jpg"; //Median size data to broadcast
	private static String strBigData = "Big.jpg";  //Big size data to broadcast
	private static String strLargeData = "Large.jpg"; //Large size data to broadcast
	
	private int m_nDataSizeType;
	private int m_nSendCount;
	private int m_nSendInterval;
	
	public DataSender(int nDataSizeType, int nSendCount, int nSendInterval) {
		m_nDataSizeType = nDataSizeType;
		m_nSendCount = nSendCount;
		m_nSendInterval = nSendInterval;
		//VanetBroadcast.m_tvShowInfo.setText("Test from DataSender1");
	}
	
	// @Override
	public void run() {
		byte[] sndBuf = new byte[BUF_SIZE];
		byte[] sndIndex = new byte[15];
		byte[] sndCurTime = new byte[30];
		byte[] sndFinalBuf = new byte[BUF_SIZE];
		DatagramPacket sndPkt = null;
		DatagramSocket sndSock = null;
		InetAddress destAddr = null;  //Destination address
		FileInputStream fileInputStream = null;
		int nBytesAvailable = 0;
		int nBytesRead;
		String sData = "";
		String sIndex = "";
		String sCurTime = "";
		int nIndexLen = 0;
		int nCurTimeLen = 0;
		int i,j,k;
		int nOffset = 0;
		
		//Get Data
		if (m_nDataSizeType == 1) { //Small data size
			sndBuf = strSmallData.getBytes();
			nBytesAvailable = sndBuf.length;
		} else if (m_nDataSizeType == 2 || m_nDataSizeType == 3 || m_nDataSizeType == 4) {
			try {
				if (m_nDataSizeType == 2) {  //Median data size
					sData = Environment.getExternalStorageDirectory().getAbsolutePath() + 
							File.separator + strMedianData;
				} else if (m_nDataSizeType == 3) {  //Big data size
					sData = Environment.getExternalStorageDirectory().getAbsolutePath() + 
							File.separator + strBigData;
				} else { //Large data size
					sData = Environment.getExternalStorageDirectory().getAbsolutePath() + 
							File.separator + strLargeData;					
				}

				fileInputStream = new FileInputStream(new File(sData));				
				nBytesAvailable = fileInputStream.available();
				
				//Utilities.m_lnTestCount = nBytesAvailable;
				
				if (nBytesAvailable > BUF_SIZE) {
					//Too big, don't send
					Log.d("Communication", "File size is too big to send");
					fileInputStream.close();
					return;
				}
				
				nBytesRead = fileInputStream.read(sndBuf, 0, nBytesAvailable);
				
				fileInputStream.close();
				
			} catch (Exception e) {
				//Exception handling
				Log.e("Communication", "Failed to read in data: " + e.toString());
			}	

		}
		
		//sndBuf = strSmallData.getBytes();
		
		try {
			destAddr = InetAddress.getByName(BROADCAST_ADDR);
			//VanetBroadcast.m_tvShowInfo.setText("Test from DataSender3");
			
//			sndPkt = new DatagramPacket(sndBuf, nBytesAvailable, destAddr, DEST_PORT);
			sndSock = new DatagramSocket();
			sndSock.setBroadcast(true);

			//sndSock.setBroadcast(false);
			//sndSock.setReceiveBufferSize(60*1024);
			////

			for (i=1; i<=m_nSendCount; i++) {
				sIndex = Utilities.leftPad(i, 4) + ",";  //Append data index part
				sndIndex = sIndex.getBytes();
				nIndexLen = sIndex.length();
				
				//System.arraycopy
				
				for (k=0; k<nIndexLen; k++) {
					sndFinalBuf[k] = sndIndex[k];
				}

				nOffset = nIndexLen + 25;   //25 = length of time part
				for (j=0; j<nBytesAvailable; j++) {
					sndFinalBuf[j+nOffset] = sndBuf[j];
				}
				
				//Get current time
				//sCurTime = Long.toString(Calendar.getInstance().getTimeInMillis()) + ",";
				sCurTime = Utilities.leftPad(Calendar.getInstance().getTimeInMillis(), 24) + ",";
				sndCurTime = sCurTime.getBytes();
				//nCurTimeLen = sCurTime.length();
				//for (k=0; k<nCurTimeLen; k++) {
				for (k=0; k<25; k++) {
					sndFinalBuf[k+nIndexLen] = sndCurTime[k];
				}
				
				
//				sndPkt = new DatagramPacket(sndBuf, nBytesAvailable, destAddr, DEST_PORT);
				sndPkt = new DatagramPacket(sndFinalBuf, nBytesAvailable + nOffset, destAddr, DEST_PORT);
				
				//Send: index,time,data
				sndSock.send(sndPkt);  //Send out packet
				//Utilities.m_nSendIndex = Utilities.m_nSendIndex + 1;
				Message msg = new Message();
				msg.what = Utilities.SEND_MSG;
				msg.arg1 = i;
				msg.arg2 = nBytesAvailable;
				VanetBroadcast.m_Handler.sendMessage(msg);
				
				Thread.sleep(m_nSendInterval);
			}
			
			Log.e("Communication", "Packet was sent.");
			
		} catch (Exception e) {
			// nothing
			Log.e("Communication", "Can't send to destination: " + e.toString());
		}
		
	}
}
