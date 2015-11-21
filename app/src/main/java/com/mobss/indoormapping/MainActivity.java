package com.mobss.indoormapping;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.ssia.sticknfind.sdk.LeDevice;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see
 */
public class MainActivity extends Activity {

	static private String TAG = "MainActivity";
	private ArrayList<Sticker> rssiRecords = new ArrayList<Sticker>();
	
	
    public static boolean  mActivityIsVisible = false;
    
    ListView listView = null;
    UserArrayAdapter listAdapter = null;
	Button forceClose = null;
	Button saveToFile = null;
	Button startRecording = null;
	boolean isRecordStarted = false;
	
	FirmwareUpdateDialog firmwareDialog = null;
	long updateWaitTime = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main_activity);

		Singleton.uiContext=this;
		
		forceClose = (Button) this.findViewById(R.id.force_close);
		forceClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				send(Message.obtain(null, Singleton.MH_MSG_STOP_SCAN, null));
				new AlertDialog.Builder(arg0.getContext())
            	.setTitle("Force Stop")
         	    .setMessage("Are you sure?")
             	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
         	        public void onClick(DialogInterface dialog, int which) {
         	        	send(Message.obtain(null, Singleton.MH_MSG_MSG_DESTROY, null));
         	        	BluetoothAdapter.getDefaultAdapter().disable();
         	        }
         	     })
        	     .setNegativeButton("No", new DialogInterface.OnClickListener() {
        	        public void onClick(DialogInterface dialog, int which) {
        	        	dialog.cancel();
        	        	send(Message.obtain(null, Singleton.MH_MSG_START_SCAN, null));
        	        }
        	     })
         	     .show();
			}
			
		});

		saveToFile = (Button) this.findViewById(R.id.saveTofile);
		saveToFile.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(isRecordStarted){
					startRecording.setText("Start Record");
					isRecordStarted = false;
					
					showToast("Recording is stopped!!!");
				}
				
				
				String filename = "";
				if(rssiRecords.size() != 0){
				    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");//dd/MM/yyyy
				    Date now = new Date();
				    String strDate = sdfDate.format(now);
				    
					filename = rssiRecords.get(0).getmId()+"_rssi_"+strDate+".txt";
				}
				else{
					showToast("No rssi data is found!!!");
					return;
				}
				
				String root = Environment.getExternalStorageDirectory().toString();
			    File myDir = new File(root);   
			    File file = new File (myDir, filename);
			    
			    
			   Log.d("", file.getAbsolutePath());
			
				try {

					FileOutputStream outputStream = new FileOutputStream(file);
					String string = "";
					for (int i = 0; i < rssiRecords.size(); i++) {
						Sticker stk = rssiRecords.get(i);
						string += stk.getmId()+";"+stk.getmName()+";"+stk.getmRssi()+";"+stk.getTime()+"\n";
					}
					outputStream.write(string.getBytes());

					outputStream.flush();
					outputStream.close();

					showToast("RSSI data is saved to file " + filename + ".");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		
		
		startRecording = (Button) this.findViewById(R.id.record);
		startRecording.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(isRecordStarted){
					startRecording.setText("Start Record");
					isRecordStarted = false;
					showToast("Recording is stopped!!!");
				}
				else{
					
					rssiRecords.clear();
					final Spinner spinner1 = (Spinner) findViewById(R.id.time_spinner);
					
					showToast("Recording will start in 10 seconds and record "+spinner1.getSelectedItem().toString()+".");
					int time = 0;
					
					
					
					final Handler handler = new Handler(); 
			        Timer t = new Timer(); 
			        // wait 10 second
			        t.schedule(new TimerTask() { 
			                public void run() { 
			                        handler.post(new Runnable() { 
			                                public void run() { 
			                					startRecording.setText("Stop Record");
			                					isRecordStarted = true;
			                					showToast("Recording is started!!!");
			                					
			                					waitAndStopRecording((int)spinner1.getSelectedItemId());
			                			        
			                                } 
			                        }); 
			                } 
			        }, 10000); 
			        
				}
			}
			
		});
		
		
        listView = (ListView) this.findViewById(R.id.listView);

        listAdapter= new UserArrayAdapter(this, new Vector<LeDevice>());
		listView.setAdapter(listAdapter);
		
		int retry = 1;
		int timeout = 30;
		while (!BluetoothAdapter.getDefaultAdapter().isEnabled()){
			if (--timeout == 0){
				break;
			}
			if (--retry == 0){
				retry = 10;
				BluetoothAdapter.getDefaultAdapter().enable();
			}
			try 
			{
				Thread.sleep(1000);				
			}catch (Exception e){}
		}

		if (!BluetoothAdapter.getDefaultAdapter().isEnabled())
			Toast.makeText(Singleton.uiContext, "Please enable bluetooth manual", Toast.LENGTH_LONG).show();
		
		Log.i(TAG, "Initial Services");
		mActivityIsVisible = true;
		Intent serviceClass = new Intent(this, MainManager.class);
		this.startService(serviceClass);

    	ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        
    	for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MainManager.class.getName().equals(service.service.getClassName())) {
            	this.bindService(new Intent(this, MainManager.class), mConnection, Context.BIND_AUTO_CREATE);
                mIsBound = true;
            }
        }
    	Log.d("", "");
	}
	
	private void waitAndStopRecording(int spinnerId){
		final Handler handler = new Handler(); 
		Timer t = new Timer(); 
		switch ( spinnerId ) {
		case 0:
	        t.schedule(new TimerTask() { 
                public void run() { 
                        handler.post(new Runnable() { 
                                public void run() { 
                					isRecordStarted = false;
                					showToast("Recording is stopped!!!");
                					startRecording.setText("Start Record");
                                } 
                        }); 
                } 
	        }, 5000);
			break;
		case 1:
	        t.schedule(new TimerTask() { 
                public void run() { 
                        handler.post(new Runnable() { 
                                public void run() { 
                					isRecordStarted = false;
                					showToast("Recording is stopped!!!");
                					startRecording.setText("Start Record");
                                } 
                        }); 
                } 
	        }, 10000);
			break;
		case 2:
	        t.schedule(new TimerTask() { 
                public void run() { 
                        handler.post(new Runnable() { 
                                public void run() { 
                					isRecordStarted = false;
                					showToast("Recording is stopped!!!");
                					startRecording.setText("Start Record");
                                } 
                        }); 
                } 
	        }, 15000);
			break;
		case 3:
	        t.schedule(new TimerTask() { 
                public void run() { 
                        handler.post(new Runnable() { 
                                public void run() { 
                					isRecordStarted = false;
                					showToast("Recording is stopped!!!");
                					startRecording.setText("Start Record");
                                } 
                        }); 
                } 
	        }, 20000);
			break;
		case 4:
	        t.schedule(new TimerTask() { 
                public void run() { 
                        handler.post(new Runnable() { 
                                public void run() { 
                					isRecordStarted = false;
                					showToast("Recording is stopped!!!");
                					startRecording.setText("Start Record");
                                } 
                        }); 
                } 
	        }, 25000);
			break;
		case 5:
	        t.schedule(new TimerTask() { 
                public void run() { 
                        handler.post(new Runnable() { 
                                public void run() { 
                					isRecordStarted = false;
                					showToast("Recording is stopped!!!");
                					startRecording.setText("Start Record");
                                } 
                        }); 
                } 
	        }, 30000);
			break;
		case 6:
	        t.schedule(new TimerTask() { 
                public void run() { 
                        handler.post(new Runnable() { 
                                public void run() { 
                					isRecordStarted = false;
                					showToast("Recording is stopped!!!");
                					startRecording.setText("Start Record");
                                } 
                        }); 
                } 
	        }, 60000);
			break;
		case 7:
	        t.schedule(new TimerTask() { 
                public void run() { 
                        handler.post(new Runnable() { 
                                public void run() { 
                					isRecordStarted = false;
                					showToast("Recording is stopped!!!");
                					startRecording.setText("Start Record");
                                } 
                        }); 
                } 
	        }, 120000);
			break;
		case 8:
	        t.schedule(new TimerTask() { 
                public void run() { 
                        handler.post(new Runnable() { 
                                public void run() { 
                					isRecordStarted = false;
                					showToast("Recording is stopped!!!");
                					startRecording.setText("Start Record");
                                } 
                        }); 
                } 
	        }, 300000);
			break;
		default:
			break;
		}
	}
	
	private void showToast(String str){
		Context context = getApplicationContext();
		CharSequence text = str;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	public void showUpdateDialog(final LeDevice dev, boolean show){
		try{
			if (show){
				Singleton.uiContext.runOnUiThread(new Runnable() {
		            public void run(){
		            	try{
		            		 firmwareDialog = new FirmwareUpdateDialog(Singleton.uiContext, dev);
			            	 firmwareDialog.show();
		            	}catch(Exception e){}
		            }
				});
			}
		}catch(Exception e){}
	}
	
	public void setProgressUpdateDialog(final LeDevice dev, final float mFirmwareProgress){
		try{
			Singleton.uiContext.runOnUiThread(new Runnable() {
	            public void run(){
	            	try{
	            		firmwareDialog.changeProgess(dev, mFirmwareProgress);
	            	}catch(Exception e){}
	            }
			});
		}catch(Exception e){}
	}

	public void updateConnectionState(final LeDevice dev){
		Log.i(TAG, "devState: " + dev.mName);
        runOnUiThread(new Runnable() {
            public void run() {
				listAdapter.notifyDataSetChanged();
            }
        });
	}
	
	public void updateStickerList(final LeDevice dev){
		if(isRecordStarted){
			Log.i(TAG, "Sticker: " + dev.mName + " :: " + dev.mRssi + " Time: "+System.currentTimeMillis());
			rssiRecords.add(new Sticker(dev.getBtDevice().getAddress(), dev.mName, dev.mRssi, System.currentTimeMillis()));
		}
        runOnUiThread(new Runnable() {
            public void run() {
				if (listAdapter.getPosition(dev) >= 0){
					if (updateWaitTime > System.currentTimeMillis()) return;
					updateWaitTime = System.currentTimeMillis() + 2500;
					listAdapter.notifyDataSetChanged();
				}else{
					listAdapter.add(dev);
				}
            }
        });
	}
	
	static public void showBtConnectionProblem(){
		Log.i(TAG, "BtConnectionProblem");
		try{
			if (mActivityIsVisible){
				Singleton.uiContext.runOnUiThread(new Runnable() {
		            public void run(){
		            	new AlertDialog.Builder(Singleton.uiContext)
		        	    .setTitle("Bluetooth Restart")
		        	    .setMessage("Back soon")
		        	    .setNeutralButton("Close", new DialogInterface.OnClickListener() {
		        	        public void onClick(DialogInterface dialog, int which) {
		        	        	dialog.cancel();
		        	        }
		        	     })
		        	     .show();
		            }
		        });
			}
		}catch(Exception e){}
	}
	
	static public void showSdkSupportProblem(){
		Log.i(TAG, "SdkSupportProblem");
		try{
			if (mActivityIsVisible){
				Singleton.uiContext.runOnUiThread(new Runnable() {
		            public void run(){
		            	new AlertDialog.Builder(Singleton.uiContext)
		            	.setTitle("Unsupported Phone Model")
		         	    .setMessage("Unfortunately, your phone build is not supported! Build.Model: " + Build.MODEL)
		             	.setPositiveButton("Close", new DialogInterface.OnClickListener() {
		         	        public void onClick(DialogInterface dialog, int which) {
		         	        	Singleton.uiContext.finish();
		         	        	dialog.cancel();
		         	        }
		         	     })
		         	     .show();
		            }
		        });
			}
		}catch(Exception e){}
	}
	
	private static boolean mIsBound;
    private static Messenger mService = null;
    static Vector<Message> msgList=new Vector<Message>();

    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    private static class IncomingHandler extends Handler {
        @Override
        public synchronized void handleMessage(Message msg) {
        	if (msg.what == Singleton.MSG_REGISTER_SUCCESS){
                Log.i(TAG, "Send missing messages");
                for (Message msgDev: msgList){
                	send(msgDev);
                }
            	msgList.removeAllElements();
        	}else{
                Singleton.incomingMessage(msg);
         	}
        }
    }
    
    public synchronized static void send(Message msg) {
    	try{
	        if ((mIsBound) && (mService != null)) {
                mService.send(msg);
	        }else if (msgList != null){
	        	msgList.add(msg);
	        }
    	}catch (RemoteException e){
    		Log.e(TAG, "Send Message: " + e.toString());
    	}
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.i(TAG, "Attached.");
            try {
                Message msg = Message.obtain(null, Singleton.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {}
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            Log.i(TAG, "Disconnected.");
        }
    };
    
    private void doUnbindService() {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, Singleton.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {}
            }

            this.unbindService(mConnection);
            mIsBound = false;
            Log.i(TAG, "Unbinding.");
        }
    }
    
    @Override
	public void onResume(){
		super.onResume();
		mActivityIsVisible = true;
	}
	
    @Override
	public void onPause(){
		super.onPause();
		mActivityIsVisible = false;
	}
	
    @Override
	public void onDestroy(){
		super.onDestroy();
		mActivityIsVisible = false;
		doUnbindService();
	}

}
