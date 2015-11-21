package com.mobss.indoormapping;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import com.ssia.sticknfind.sdk.*;
import com.ssia.sticknfind.sdk.nexus.NexusDeviceManager;
import com.ssia.sticknfind.sdk.nexus.NexusSnfDevice;
import com.ssia.sticknfind.sdk.samsung.SamsungDeviceManager;

import java.util.ArrayList;


public class MainManager extends Service{
	private static String TAG="MainHandler";
	
	static MainManager context;
	static LeDeviceManager leDeviceManager = null;
	

	public MainManager(){
		super();
	}
	

	LogDelegate logDelegate = new LogDelegate(LogDelegate.VERBOSE_ALL){ 

		@Override
		public void log(long currentTime, String dateFormat, int pid,
				String tag, String content) {}
		
	};

    LeDeviceManagerDelegate leDeviceManagerDelegate = new LeDeviceManagerDelegate(){

		@Override
		public void didAddNewDevice(LeDeviceManager mgr, LeDevice dev) {
			if (dev.mName==null || dev.mName.length() == 0){
				mgr.mDevList.add(dev);
				dev.mName = findGoodName(mgr);
				try
				{
					((LeSnfDevice)dev).setAuthenticationKey(new byte [] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
				}catch (Exception e){};
			}
		}

		@Override
		public void didStartService(LeDeviceManager mgr) {
			try 
			{
				Thread.sleep(3000);				
			}catch (Exception e){}
			mgr.startScan();
		}
    };
    
    LeSnfDeviceDelegate leSnfDeviceDelegate = new LeSnfDeviceDelegate(){

    	@Override
    	public void didReceiveRemoteRssi(LeDevice dev, int rssi, int channel){
    		send(Message.obtain(null, Singleton.MH_MSG_BEACON_UPDATE, dev));
    	}
    	
		@Override
		public void didUpdateBroadcastData(LeDevice dev, int rssi, byte[] scanRecord) {
			send(Message.obtain(null, Singleton.MH_MSG_STICKER_UPDATE, dev));
			dev.mRssi = rssi;
		}
		
		@Override
		public void didReadTemperature(LeDevice dev){
			Log.i(TAG, "didReadTemperature: " + dev.mName + " :: " + ((LeSnfDevice)dev).mTemperature);
		}
		
		@Override
		public void didReadBattery(LeDevice dev){
			Log.i(TAG, "didReadBattery: " + dev.mName + " :: " + ((LeSnfDevice)dev).mBatteryLevel);
		}
		
		@Override
		public void didSetAuthenticationKey(LeDevice dev){
			Log.i(TAG, "didSetAuthenticationKey:" + dev.mName);
		}
		
		@Override
		public void didReadRemoteRevision(LeDevice dev){
			Log.i(TAG, "didReadRemoteRevision: " + dev.mName + " :: " + ((LeSnfDevice)dev).mRemoteRevision);
		}
		
		@Override
		public void didReadBroadcastRate(LeDevice dev){
			Log.i(TAG, "didReadBroadcastRate: " + dev.mName + " :: " + ((LeSnfDevice)dev).mBroadcastRate);
		}

		@Override
		public void didDiscoverLeSnfDevice(LeDevice dev) {
			Log.i(TAG, "didDiscoverLeSnfDevice: " + dev.mName);
		}
		
		@Override
		public void didDiscoverLeSnfDevice(LeDevice dev, Object [] list) {
			Log.i(TAG, "didDiscoverLeSnfDevice: " + dev.mName + " :: " + list.toString());
		}

		@Override
		public void didChangeState(LeSnfDevice dev, int state) {
			Log.i(TAG, "didChangeState: " + dev.mName + " :: " + state);
			send(Message.obtain(null, Singleton.MH_MSG_CONNECTIONSTATE, dev));
		}

		@Override
		public void didSetTemperatureCalibrationForLeSnfDevice(LeDevice dev,
				boolean success) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void didSetPairingRssiForLeSnfDevice(LeDevice dev,
				boolean success) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void didSetBroadcastRateForLeSnfDevice(LeDevice dev,
				boolean success) {
			Log.i(TAG, "didSetBroadcastRateForLeSnfDevice: " + dev.mName);
		}

		@Override
		public void didSetBroadcastKeyForLeSnfDevice(LeDevice dev,
				boolean success) {
			Log.i(TAG, "didSetBroadcastKeyForLeSnfDevice: " + dev.mName);	
		}

		@Override
		public void didSetBroadcastDataForLeSnfDevice(LeDevice dev, int index,
				boolean success) {
			Log.i(TAG, "didSetBroadcastDataForLeSnfDevice: " + dev.mName + " :: " + index);
		}

		@Override
		public void didReadTemperateLog(int[] log, LeSnfDevice dev) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void didEnableConnectionLossAlertForLeSnfDevice(LeSnfDevice dev,
				boolean success) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void didEnableAlertForLeSnfDevice(LeDevice dev,
				boolean success) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void didRequestFirmwareUpdate(LeDevice dev) {
			if (0 != (((LeSnfDevice)dev).mDeviceFlags & 0x40)) dev.setPowerOffTimer(0);
			dev.startFirmwareUpdate();
		}

		@Override
		public void didTimeoutFirmwareUpdate(LeDevice dev) {
			//NOT used at the moment
		}

		@Override
		public void didStartFirmwareUpdate(LeDevice dev) {
			Singleton.uiContext.showUpdateDialog((LeSnfDevice) dev, true);
			Singleton.uiContext.setProgressUpdateDialog((LeSnfDevice) dev, 0);
		}

		@Override
		public void didChangeFirmwareUpdateProgress(LeDevice dev, float mFirmwareProgress) {
			Singleton.uiContext.setProgressUpdateDialog((LeSnfDevice) dev, mFirmwareProgress);			
		}

		@Override
		public void didFinishFirmwareUpdate(LeDevice dev) {
			Singleton.uiContext.setProgressUpdateDialog((LeSnfDevice) dev, -1);
		}
    	
		@Override
		public void didReadDeviceFlags(LeDevice dev) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void didFailAuthentication(LeDevice dev) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void internalError(LeDevice dev, int verbose, final String err){
			Singleton.uiContext.runOnUiThread(new Runnable(){

				@Override
				public void run() {
					if (Singleton.uiContext != null)
						Toast.makeText(Singleton.uiContext, err, Toast.LENGTH_LONG).show();
				}
			});
			leDeviceManager.disconnect(dev);
			dev.reset();
		}

		@Override
		public void requireFirmwareUpdate(LeDevice dev) {
			Log.e(TAG, "Device: " + dev.mName + " needs firmware update");
			dev.mOldSticker = true;
		}

		@Override
		public void didScanForDevices(boolean start) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void didReadDeviceId(LeDevice dev, byte[] deviceId) {
			int interval = 1800;
			((LeSnfDevice) dev).setAdvertisementSettings(interval, 3000, interval, 0, interval, 0x76, 0x76);
		}

		@Override
		public void didReadBroadcastData(LeDevice dev, byte[] data) {
			// TODO Auto-generated method stub
			
		}

    };
    
	///////////////////////////////////////////////////////////////////////////////////

    private String findGoodName(LeDeviceManager mgr) {
    	return "SNF " + mgr.mDevList.size();
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	static ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    final Messenger mMessenger = new Messenger(new IncomingHandler());

	protected boolean mDiscoveryRunning = false;
	private static boolean mSdkFailed = false;
    
    private static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case Singleton.MSG_REGISTER_CLIENT:
                Log.i(TAG, "Client registered: "+msg.replyTo);
                mClients.add(msg.replyTo);
                send(Message.obtain(null, Singleton.MSG_REGISTER_SUCCESS));
                if (mSdkFailed)
                	send(Message.obtain(null, Singleton.MH_MSG_PHONE_NOT_SUPPORTED));
                break;
            case Singleton.MSG_UNREGISTER_CLIENT:
                Log.i(TAG, "Client un-registered: "+msg.replyTo);
                mClients.remove(msg.replyTo);
                break;
            case Singleton.MH_MSG_MSG_DESTROY:
            	leDeviceManager.destroy();
            	if (MainManager.context != null)
            		MainManager.context.stopSelf();
            	android.os.Process.killProcess(android.os.Process.myPid());
            	break;
            default:
                onReceiveMessage(msg);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////
 	
	protected BroadcastReceiver receiverAdapter;
	protected BroadcastReceiver receiverDevice;
	
	
	public void startConnection(){	
		
		Log.d(TAG,"Initial OnCreate");
		
		if (Build.VERSION.SDK_INT >= 18){
			//NEXUS
			leDeviceManager = new NexusDeviceManager(getApplicationContext(), leDeviceManagerDelegate, leSnfDeviceDelegate, logDelegate, LeDeviceManager.TYPE_BEACON);
			
			registerReceiver(receiverAdapter, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		}else if (Build.VERSION.SDK_INT == 17){
			//SAMSUNG
			leDeviceManager = new SamsungDeviceManager(getApplicationContext(), leDeviceManagerDelegate, leSnfDeviceDelegate, logDelegate, LeDeviceManager.TYPE_BEACON);
			
			registerReceiver(receiverDevice, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
			registerReceiver(receiverDevice, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
		}else{
			//NOT SUPPORTED
			MainActivity.showSdkSupportProblem();
		}
	}
	
	public void broadcastReceiver(){
		receiverDevice = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
	        	String action = intent.getAction();
	        	
	        	Log.d(TAG,"ActionDevice    "+action);
				if (leDeviceManager != null)
					leDeviceManager.receiveDeviceIntent(context, intent);
			}
		};
		receiverAdapter = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				Log.d(TAG,"ActionAdapter    "+action);
				if (leDeviceManager != null)
					leDeviceManager.receiveAdapterIntent(context, intent);
			}
			
		};
    }
	
    ///////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		Singleton.connectionMaintainerIsRunning = false;		
			
		leDeviceManager.destroy();
		
		if (receiverAdapter != null)
			unregisterReceiver(receiverAdapter);
		if (receiverDevice != null)
			unregisterReceiver(receiverDevice);
		
		try{
			android.os.Process.killProcess(android.os.Process.myPid());
		}catch(Exception e){}
	}
	
		@Override
	public void onCreate(){
		Log.i(TAG, "onCreate");
		
		Singleton.context=this;
		
		broadcastReceiver();
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		MainManager.context = this;
		
		startConnection();
		
	    return Service.START_STICKY;
	}

//////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public IBinder onBind(Intent arg0) {
        return mMessenger.getBinder();
	};
	
    protected static void send(Message msg) {
         for (int i=mClients.size()-1; i>=0; i--) {
        	 try {
               mClients.get(i).send(msg);
            }
            catch (RemoteException e) {
                Log.e(TAG, "Client is dead. Removing from list: "+i);
                mClients.remove(i);
            }
        }       
    }
    
	public static void onReceiveMessage(Message msg) {
		if (leDeviceManager == null) return;
		if (leDeviceManager.mBta == null) return;
		switch (msg.what){
		case Singleton.MH_MSG_AVOID_FIRMWARE_UPDATE:
			break;
		case Singleton.MH_MSG_CONNECT_DEVICE:
			leDeviceManager.connect((LeDevice)msg.obj);
			break;
		case Singleton.MH_MSG_DISCONNECT_DEVICE:
			leDeviceManager.disconnect((LeDevice)msg.obj);
			break;
		case Singleton.MH_MSG_CONNECTION_SPEED:
			break;
		case Singleton.MH_MSG_REMOVEALL:
			break;
		case Singleton.MH_MSG_FORCE_DISCONNECT:
			break;
		case Singleton.MH_MSG_START_SCAN:
			leDeviceManager.mShouldScan = true;
			break;
		case Singleton.MH_MSG_STOP_SCAN:
			leDeviceManager.mShouldScan = false;
			break;
		case Singleton.MH_MSG_ALERT_DEVICE:
			((LeDevice)msg.obj).setAlertLevel(3);
			break;
		case Singleton.MH_MSG_DEVICE_LATENCY:
			Object [] value = (Object[])msg.obj;
			((LeDevice)value[0]).setLatency((Integer)value[1]);
			break;
		case Singleton.MH_MSG_DEVICE_RBOND:
			((NexusSnfDevice)msg.obj).removeBond();
			break;
		default:
			Log.i(TAG, "Default Msg Received: " + msg.toString());
		}
	}

    
/////////////////////////////////////////////////////////////////////////////////////
    
    
}
