package com.arena.vanetbroadcast;

import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.os.SystemClock;

import android.app.*;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;

import android.content.*;
import android.content.pm.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.BroadcastReceiver;

import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.GeomagneticField;

import android.net.wifi.*;

import android.location.*;
import android.util.*;

import java.text.SimpleDateFormat;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Calendar;

import java.net.*;

import java.util.Observable;
import java.util.Observer;  /* this is Event Handler */
import android.os.Message;
import java.io.DataOutputStream;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.GeomagneticField;
import android.util.*;
import android.net.wifi.*;
import android.content.BroadcastReceiver;
import android.location.*;
import android.text.format.Time;
import android.app.AlarmManager;


public class VanetBroadcast extends Activity {
		
	private EditText m_edtSendCount;
	private EditText m_edtSendInterval;
	
	private RadioGroup m_rdgpDataSizeType;
	private RadioButton m_rdDataSizeSmall,m_rdDataSizeMedian,m_rdDataSizeBig, m_rdDataSizeLarge;
	
	private Button m_btnStart;
	private Button m_btnReset;
	private static TextView m_tvSendInfo = null;
	private static TextView m_tvReceiveInfo = null;
	private TextView m_tvShowInfo = null;
	private TextView m_tvShowIP = null;
	
	private int m_nDataSizeType = 1;  //1--small size; 2--median size;  3--big size
	private int m_nSendCount = 0;  //How many times to send
	private int m_nSendInterval = 0;  //Interval between sending,  unit: ms
	
	private boolean blnStarted = false;
	
	private LocationManager m_locManager = null;
	private String m_sGPSProvider = LocationManager.GPS_PROVIDER; //GPS provider
	private boolean m_blnGPSSignalEnabled = false;
//	private static boolean m_blnSetTime = false;
	private static int m_nSetTime = 0;
	
	public static Handler m_Handler = new Handler() {
        public void handleMessage(Message msg) {
            // process incoming messages here
        	
        	switch (msg.what) {
        		case Utilities.RECV_MSG:
        			m_tvReceiveInfo.setText("#" + Integer.toString(msg.arg1) + ",  Size: " + Integer.toString(msg.arg2));
        			break;
        		case Utilities.SEND_MSG:
        			m_tvSendInfo.setText("#" + Integer.toString(msg.arg1) + ",  Size: " + Integer.toString(msg.arg2));
        			break;
        		default:
        			break;	
        	}
        	
        	super.handleMessage(msg);
        }
    };	
	
    	
	private String intToIp(int i) {
        return (i & 0xFF ) + "." +         
        		((i >> 8 ) & 0xFF) + "." +         
        		((i >> 16 ) & 0xFF) + "." +         
        		( i >> 24 & 0xFF) ;   
    }

	
    /* Get GPS provider */
//    private boolean getGPSProvider() {
    private void getGPSProvider() {

    	//Location location = null;
		Criteria crit = new Criteria();
		//float fLat,fLng,fAlt;
		//crit.setAccuracy(Criteria.ACCURACY_FINE);
		//crit.setAltitudeRequired(false);
		//crit.setBearingRequired(false);
		//crit.setCostAllowed(false);
		//crit.setPowerRequirement(Criteria.POWER_LOW);

		m_sGPSProvider = m_locManager.getBestProvider(crit, true); //false?
		if (m_sGPSProvider != null) {
			m_blnGPSSignalEnabled = true;
//			location = m_locManager.getLastKnownLocation(m_sGPSProvider);
//			if (location != null ) {

//			}
//			return true;
//		} else {
//			return false;
		} 
    }
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String myLocalIP = "";
		Location location = null;
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_vanet_broadcast);
		
		m_edtSendCount = (EditText)findViewById(R.id.SendCount);
		
		m_edtSendInterval = (EditText)findViewById(R.id.SendInterval);
		
		m_rdgpDataSizeType = (RadioGroup)findViewById(R.id.RdGpDataSizeType);
		m_rdgpDataSizeType.setOnCheckedChangeListener(m_rdgpDataSizeListener);
		
		m_rdDataSizeSmall = (RadioButton)findViewById(R.id.RdDataSizeSmall);
		m_rdDataSizeMedian = (RadioButton)findViewById(R.id.RdDataSizeMedian);
		m_rdDataSizeBig = (RadioButton)findViewById(R.id.RdDataSizeBig);
		m_rdDataSizeLarge = (RadioButton)findViewById(R.id.RdDataSizeLarge);
		
		m_rdDataSizeSmall.setChecked(true); 

		m_btnStart = (Button)findViewById(R.id.Start);		
		m_btnStart.setOnClickListener(m_btnStartListener);
		
		m_btnReset = (Button)findViewById(R.id.Reset);		
		m_btnReset.setOnClickListener(m_btnResetListener);
		
		m_tvShowIP = (TextView)findViewById(R.id.ShowIP);
		m_tvSendInfo = (TextView)findViewById(R.id.SendInfo);
		m_tvReceiveInfo = (TextView)findViewById(R.id.ReceiveInfo);		
		m_tvShowInfo = (TextView)findViewById(R.id.ShowInfo);
		
		//myLocalIP = Utilities.getLocalIpAddress();
		myLocalIP = Utilities.getIPAddress(true);
		m_tvShowIP.setText(myLocalIP); 
		
		//getGPSProvider();
		
		m_locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		m_blnGPSSignalEnabled = m_locManager.isProviderEnabled(m_sGPSProvider);

		//if ((m_locManager != null) && (m_blnGPSSignalEnabled == true)) {
			//location = m_locManager.getLastKnownLocation(m_sGPSProvider);
			//if (location != null) {
			//}
		//}
		
//		if (m_blnGPSSignalEnabled == true) {
		if ((m_locManager != null) && (m_blnGPSSignalEnabled == true)) {
			//m_locManager.requestLocationUpdates(m_sGPSProvider, 0L, 0.0f, m_locListener);
 		}		
		
		WifiManager wMan = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if(!wMan.isWifiEnabled()){
            wMan.setWifiEnabled(true);
        }
        
        WifiInfo wifiInfo = wMan.getConnectionInfo();
        m_tvShowInfo.setText(intToIp(wifiInfo.getIpAddress()));		
		
	}
	
	
    private LocationListener m_locListener = new LocationListener() {
	   	public void onLocationChanged(Location location) {
	   		//if (m_blnSetTime == false) {
	   		if (m_nSetTime < 3) {
	   			if (location != null) {    		
	   				recordLocation(location);
	   			}
	   		}
	   	}
	
	   	public void onProviderDisabled(String provider) { 
	   		if (provider.equals(m_sGPSProvider)) {
	   			m_blnGPSSignalEnabled = false;
	   		}
	   	}
	   	 
	   	public void onProviderEnabled(String provider) {
	   		if (provider.equals(m_sGPSProvider)) {
	   			m_blnGPSSignalEnabled = true;
	   		} 
	    }
	   	 
	   	public void onStatusChanged(String provider, int status, Bundle extras) {
	   		if (provider.equals(m_sGPSProvider)) {
	   			if (status == LocationProvider.OUT_OF_SERVICE) {
	   				m_blnGPSSignalEnabled = false;
	   			} else {
	   				m_blnGPSSignalEnabled = true;
	   			}
	   		}	 
	   	}
   	 
    };
	
    public void recordLocation(Location location) {	
		//GPS Data
		if (location != null) {
			// Long.valueOf(location.getTime()).toString();
			//if (m_blnSetTime == false) {
			if (m_nSetTime < 3) {
				//AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
				//am.setTime(location.getTime());
	       		//Execute shell command
	    		try {
	 	    		Process p = Runtime.getRuntime().exec("su");
		    		DataOutputStream os = new DataOutputStream(p.getOutputStream());
		    		os.writeBytes("chmod 666 /dev/alarm\n");
		    		os.writeBytes("exit\n");
		    	    os.flush();
		    	    os.close();
		    	    SystemClock.setCurrentTimeMillis(location.getTime());
	    		} catch (Exception e) {
	    			
	    		}
				
				//m_blnSetTime = true;
				m_nSetTime = m_nSetTime + 1;
				if (m_nSetTime == 3) {
					m_tvShowInfo.setText("GPS time synced!");
				}
			}	 
		}	
    }

	
	class ResponseHandler implements Observer {
	    private String resp;
	    public void update(Observable obj, Object arg) {
        	Toast toast = Toast.makeText(getApplicationContext(), "Hello1", Toast.LENGTH_SHORT);
        	toast.show();

	    	if (arg instanceof String) {

	        	toast = Toast.makeText(getApplicationContext(), "Hello2", Toast.LENGTH_SHORT);
	        	toast.show();
	        	
	            resp = (String) arg;
	            m_tvShowInfo.setText("Sent: " + resp);
	        }
	    }
	}	
	
	@Override
	protected void  onStart() {
		super.onStart();
		
		//Start receive
	
		//new Thread(new DataReceiver()).start();
		
        final DataReceiver dataRecv = new DataReceiver();
        
        // create an observer
 //       final ResponseHandler responseHandler = new ResponseHandler();
 
        // subscribe the observer to the event source
//        dataRecv.addObserver(responseHandler);
 
        // starts the event thread
        Thread thread = new Thread(dataRecv);
        thread.start();
		
	}
	
	@Override
    protected void onResume() {
    	super.onResume();
    }
    
	@Override
    protected void onStop() {
    	super.onStop();    	
    }
	
    /* Event listener for Send button (Start/Stop) */
    private Button.OnClickListener m_btnStartListener = new Button.OnClickListener(){
    	public void onClick(View v) {
    		// Start broadcast
    		//if (blnStarted == false) {
	    		m_nSendCount = Integer.parseInt(m_edtSendCount.getText().toString());
	    		m_nSendInterval = Integer.parseInt(m_edtSendInterval.getText().toString());
	    		
	    		m_tvShowInfo.setText("Sending..." + m_nSendCount + " , " + m_nSendInterval);
	
	    		blnStarted = true;
	    		//new Thread(new DataSender(m_nDataSizeType, m_nSendCount, m_nSendInterval)).start();
	    		
	            final DataSender dataSent = new DataSender(m_nDataSizeType, m_nSendCount, m_nSendInterval);
	            
	            // create an observer
	            //final ResponseHandler responseHandler = new ResponseHandler();
	     
	            // subscribe the observer to the event source
	            //dataSent.addObserver(responseHandler);
	     
	            // starts the event thread
	            Thread thread = new Thread(dataSent);
	            thread.start();

    		//} else {
    		//	m_tvShowInfo.setText("Send Index:" + Utilities.m_nSendIndex + ", Receive Size:" + Utilities.m_lnRecvSize);
    		//}
    		
    	}
    };


    /* Event listener for Reset button) */
    private Button.OnClickListener m_btnResetListener = new Button.OnClickListener(){
    	public void onClick(View v) {
    		Utilities.m_nRecvIndex = 0;
    		m_tvSendInfo.setText("");
    		m_tvReceiveInfo.setText("");
    		m_tvShowInfo.setText("");

    		String sDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    		
       		//Execute shell command
    		try {
 	    		Process p = Runtime.getRuntime().exec("su");
	    		DataOutputStream os = new DataOutputStream(p.getOutputStream());
	    		os.writeBytes("cd " + sDir + "\n");
	    		os.writeBytes("rm RecvData.txt\n");
	    		os.writeBytes("rm RecvPic*.jpg\n");
	    		os.writeBytes("exit\n");
	    	    os.flush();
	    	    os.close();
    		} catch (Exception e) {
    			
    		}
    		
    		//m_blnSetTime = false;
    		m_nSetTime = 0;
    		
    	}
    };
    
    
    /* Event listener for data size radio group selection */
    private RadioGroup.OnCheckedChangeListener m_rdgpDataSizeListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			
			// TODO Auto-generated method stub
			
			if (m_rdDataSizeSmall.isChecked()) {
				m_nDataSizeType = 1;
			} else if (m_rdDataSizeMedian.isChecked()) {
				m_nDataSizeType = 2;
			} else if (m_rdDataSizeBig.isChecked()) {
				m_nDataSizeType = 3;
			} else if (m_rdDataSizeLarge.isChecked()) {
				m_nDataSizeType = 4;
			}
    
		}
    };
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.vanet_broadcast, menu);
		return true;
	}
	

}
