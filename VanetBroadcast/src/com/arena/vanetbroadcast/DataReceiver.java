package com.arena.vanetbroadcast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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

public class DataReceiver implements Runnable {
	
	private static final int RECV_PORT = 8888;
	private static final int BUF_SIZE = 50*1024;  //50KB
	private static FileWriter m_fwRecvData = null;
	private static boolean m_blnFileExist = false;
	//private static int m_nPicIndex = 0;
	
	
	public void saveRecvData(String sContent) {
		String sDataDir = "";
		File flDataFolder = null;	
		String sFullPathFile = "";
		String sLine = "";
/*
		if (m_blnFileExist == false) {
			// Check whether SD Card has been plugged in
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				sDataDir = Environment.getExternalStorageDirectory().getAbsolutePath() + 
										File.separator +  Resources.getSystem().getString(R.string.recvdatafolder);
				flDataFolder = new File(sDataDir);
				//Check whether /mnt/sdcard/VanetRecvData/ exists
				if (!flDataFolder.exists()) {
					//Does not exist, create it
					if (flDataFolder.mkdir()) {
						     						
					} else {
						//Failed to create
						Log.e("Communication", "Can't create VanetRecvData folder.");
						return;
					}
					
				} else {
					     					
				} 
			} else {        				
				//NO SD Card
				Log.e("Communication", "No SD card.");
				return;
			}
		}
*/		
		//sFullPathFile = sDataDir + File.separator + "RecvData.txt";
		sFullPathFile = Environment.getExternalStorageDirectory().getAbsolutePath() + 
				File.separator + "RecvData.txt";
		
		//Write into file
		
		try {
			sLine = sContent + System.getProperty("line.separator");

			m_fwRecvData = new FileWriter(sFullPathFile,true);

			m_fwRecvData.write(sLine);
			m_fwRecvData.flush();
			m_fwRecvData.close();
			m_fwRecvData = null;
			
		} catch (IOException e) {
			Log.e("Communication", "Failed to save into file.");
			return;
		} 
		
		m_blnFileExist = true;
		
	}
	
	
	public void saveRecvPic(String sFilePath, DatagramPacket recvPkt, int nOffset) {
//	public void saveRecvPic(String sFilePath, String rcv) {

		FileWriter recWriter = null;
		BufferedWriter recFout = null;
		FileOutputStream fileOutputStream = null;
		
		try {
			
			fileOutputStream = new FileOutputStream(new File(sFilePath));
//			fileOutputStream.write(rcv.getBytes());
			fileOutputStream.write(recvPkt.getData(), nOffset, recvPkt.getLength());
			fileOutputStream.flush();
			fileOutputStream.close();
			fileOutputStream = null;
			//recWriter = new FileWriter(sFilePath);
			//recFout = new BufferedWriter(recWriter);
			//rcv = new String(recvPkt.getData(), 0, recvPkt.getLength());
			
			//recWriter.write(rcv);
			//recWriter.close();

		} catch (Exception e) {
			
		}

	}
	
	// @Override
	public void run() {
		byte[] recvBuf = new byte[BUF_SIZE];  //50KB
		DatagramPacket recvPkt = null;
		DatagramSocket recvSock = null;
		boolean done = false;
		SimpleDateFormat spdCurDateTime;
        //final String DATE_FORMAT = "yyyyMMddHHmmss";
		final String DATE_FORMAT_S = "yyMMddHHmmssSSS"; //"yyyyMMddHHmmssSSS"
    	Date dtCurDate;
    	String sTimeField = "";
    	String sSavedData = "";
    	long lnCount = 0;
    	String rcv = "";
    	String sPicFile = "";
    	int nRecvSize = 0;
    	String sIndex = "";
    	long lnRecvTime;
    	long lnSendTime;
    	long lnCommTime;
    	long lnCommSpeed;
    	int nEndPosOfCurTime;
    	String sSentTime = "";


		//VanetBroadcast.m_tvShowInfo.setText("Test from DataReceiver");

		//Utilities.m_nTestCount = Utilities.m_nTestCount + 15;
		
		Looper.prepare();
		
		while (!done && !Thread.currentThread().isInterrupted()) {
			done = true;
			try {
				recvSock = new DatagramSocket(RECV_PORT);
				recvSock.setReceiveBufferSize(BUF_SIZE);
			}
			catch (Exception e) {
				e.printStackTrace();
				done = false;
			}
			if (!done) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

		while (!Thread.currentThread().isInterrupted()) {
			
			//VanetBroadcast.m_tvShowInfo.setText("Test from DataReceiver1:");
			try {
				//VanetBroadcast.m_tvShowInfo.setText("Receiving...");
				//Thread.sleep(30);
				//byte[] recvBuf = new byte[BUF_SIZE];  //50KB
				recvPkt = new DatagramPacket(recvBuf, BUF_SIZE);
				recvSock.receive(recvPkt);  //Receiver packet
				
				lnRecvTime = Calendar.getInstance().getTimeInMillis();
				
				// Timestamp for the record
				dtCurDate = new Date();
				spdCurDateTime = new SimpleDateFormat(DATE_FORMAT_S);
				sTimeField = spdCurDateTime.format(dtCurDate);

				//String rcv = new String(recvPkt.getData(), 0, recvPkt.getLength());                    

				Log.d("Communication", "Packet was received from " + recvPkt.getAddress().toString());

				String myLocalIP = Utilities.getIPAddress(true);

				if (recvPkt.getAddress().getHostAddress().compareTo(myLocalIP) == 0) {
					Log.d("Communication", "Packet was received from myself");       
				} else {
					Utilities.m_nRecvIndex = Utilities.m_nRecvIndex + 1;
					//m_nPicIndex = m_nPicIndex + 1;
					//lnCount = lnCount + 1;
					//VanetBroadcast.m_tvShowInfo.setText("Data Received: " + lnCount);
					rcv = new String(recvPkt.getData(), 0, recvPkt.getLength());
					sIndex = rcv.substring(0, 5); //Including ","

					nEndPosOfCurTime = rcv.indexOf(",", 6);
					sSentTime = rcv.substring(5, nEndPosOfCurTime);  //Not including ","
					lnSendTime = Long.parseLong(sSentTime);
					
					lnCommTime = lnRecvTime - lnSendTime; //Communication time in ms
					nRecvSize = recvPkt.getLength() - sIndex.length() - sSentTime.length() - 1; 
					
					lnCommSpeed = (recvPkt.getLength()+28L)*1000L/lnCommTime;   //28=UDP+IP header, Unit: byte/s
					
					//Save received packet into a file with time stamp
					if (recvPkt.getLength() < 500) {   //Not a picture
						//Write into file
						
						//rcv = new String(recvPkt.getData(), 0, recvPkt.getLength());
						//sIndex = rcv.substring(0, 4);
						sSavedData = sIndex + sTimeField + ",[Speed: " + Long.toString(lnCommSpeed) + " Bps],"+ rcv.substring(nEndPosOfCurTime + 1);
						saveRecvData(sSavedData);
					} else {
						//Save as image
						//And write a file which only contains:  xxxxx(index) timestamp
						sPicFile = Environment.getExternalStorageDirectory().getAbsolutePath() + 
								File.separator + "RecvPic" + Utilities.m_nRecvIndex + ".jpg";
						//rcv = new String(recvPkt.getData(), 0, recvPkt.getLength());
						//sIndex = rcv.substring(0, 4);
						sSavedData = sIndex + sTimeField + ",[Speed: " + Long.toString(lnCommSpeed) + " Bps],[Time: " + Long.toString(lnCommTime) + " ms ],[image],size: " + nRecvSize;
						saveRecvData(sSavedData); //Save the index, timestamp, speed of this image
						saveRecvPic(sPicFile, recvPkt, nEndPosOfCurTime + 1);
						//saveRecvPic(sPicFile, rcv.substring(5));
					}
				
					//// Update UI
					Message msg = new Message();
					msg.what = Utilities.RECV_MSG;
					msg.arg1 = Utilities.m_nRecvIndex;
					msg.arg2 = nRecvSize;
					VanetBroadcast.m_Handler.sendMessage(msg);
				}
			} catch (Exception e) {
				// nothing
				Log.d("Communication", "Can't receive data : " + e.getMessage());
			}
		}
		

	}	

}
