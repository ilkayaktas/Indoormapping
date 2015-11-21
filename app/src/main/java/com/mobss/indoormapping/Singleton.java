package com.mobss.indoormapping;

import android.os.Message;

import com.ssia.sticknfind.sdk.LeDevice;


public class Singleton {
	
	public final static int MH_MSG_CONNECTION_SPEED = 2;
	public final static int MH_MSG_AVOID_FIRMWARE_UPDATE = 3;
	
	public final static int MH_MSG_REGISTER_WATCHER = 50;
	public final static int MH_MSG_UNREGISTER_WATCHER = 51;
	public final static int MH_MSG_MSG_DESTROY = 52;
	
	public final static int MH_MSG_CONNECT_DEVICE = 100;
	public final static int MH_MSG_REMOVEALL = 101;
	public final static int MH_MSG_FORCE_DISCONNECT = 102;
	public final static int MH_MSG_DISCONNECT_DEVICE = 103;
	public final static int MH_MSG_START_SCAN = 104;
	public final static int MH_MSG_STOP_SCAN = 105;
	public final static int MH_MSG_ALERT_DEVICE = 106;
	public final static int MH_MSG_DEVICE_LATENCY = 107;
	public final static int MH_MSG_DEVICE_RBOND = 108;
	
	public final static int MH_MSG_CONNECTIONSTATE = 200;
	
	public final static int MH_MSG_PHONE_NOT_SUPPORTED = 400;
	public final static int MH_MSG_BEACON_UPDATE = 401;
	public final static int MH_MSG_STICKER_UPDATE = 402;
	
    public final static int MSG_REGISTER_CLIENT = 998;
    public final static int MSG_UNREGISTER_CLIENT = 999;
    public final static int MSG_REGISTER_SUCCESS = 1000;
	
	public float FOUND_THRESHOULD= 50;//50000;
	public float MAX_SIGNAL=(float)60;//100000.0;
	
	public static MainActivity uiContext;
	public static MainManager context;
	public static boolean connectionMaintainerIsRunning = false;

///////////////////////////////////////////////////////////////////////////////////

	public synchronized static void updateMainHandlerStatus(int msgId, int value){
		Message msg = Message.obtain(null, msgId,value, 0);
		uiContext.send(msg);
	}
	
	public synchronized static void updateMainHandlerWatcher(int msgId, String value){
		Message msg = Message.obtain(null, msgId, value);
		uiContext.send(msg);
	}
	
	public synchronized static void updateMainHandlerDevice(int msgId, LeDevice value){
		Message msg = Message.obtain(null, msgId, value);
		uiContext.send(msg);
	}
	
	public synchronized static void updateMainHandlerDevice(int msgId, LeDevice value, int arg){
		Object[] bundle = {value, Integer.valueOf(arg)};
		Message msg = Message.obtain(null, msgId, bundle);
		uiContext.send(msg);
	}

	public synchronized static void incomingMessage(Message msg){
		switch (msg.what){
		case MH_MSG_PHONE_NOT_SUPPORTED:
			MainActivity.showSdkSupportProblem();
			break;
		case MH_MSG_STICKER_UPDATE:
			Singleton.uiContext.updateStickerList((LeDevice)msg.obj);
			break;
		case MH_MSG_CONNECTIONSTATE:
			Singleton.uiContext.updateConnectionState((LeDevice)msg.obj);
			break;
		}
	}
}
