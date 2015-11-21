package com.mobss.indoormapping;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ssia.sticknfind.sdk.LeDevice;

public class FirmwareUpdateDialog extends Dialog{
	
	final static int STATE_UPDATE_IN_PRROGRESS = 1;
	final static int STATE_UPDATE_FINISHED = 2;
	final static int STATE_UPDATE_CLOSE = 3;
	
	private boolean dialogShown = false;
	public Activity activity;

	private TextView stickerName;
	private ImageView sKnob;
	private ProgressBar knobStatus;
	private TextView progressText;
	private ProgressBar progressBar;
	
	Button cancelButton;
	
	private TextView hintTV1,hintTV2;
	
	public FirmwareUpdateDialog(final Activity context, final LeDevice dev){
		super(context);
		
		this.setCancelable(false);
		this.activity=context;
		if ( !dialogShown) {
			dialogShown = true;

			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
			this.setContentView(R.layout.firmware_updating_layout);
		
			sKnob = (ImageView)this.findViewById(R.id.sKnob);
			knobStatus = (ProgressBar)this.findViewById(R.id.knobStatus);
			stickerName = (TextView)this.findViewById(R.id.stickerName);
			progressText = (TextView)this.findViewById(R.id.progressText);
			progressBar = (ProgressBar)this.findViewById(R.id.progressBar);
			progressBar.setMax(100);
			
			stickerName.setText(dev.mName);
									
			hintTV1=(TextView) this.findViewById(R.id.hintTV1);
			
			hintTV2=(TextView) this.findViewById(R.id.hintTV2);

			cancelButton = (Button) this.findViewById(R.id.cancelButton);
			cancelButton.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
						new AlertDialog.Builder(context)
			    	    .setTitle("Firmware Update...")
			    	    .setMessage("Are you sure you want to cancel the sticker update?")
			        	.setPositiveButton("Yes", new OnClickListener() {
			    	        public void onClick(DialogInterface dialog, int which) {
			    	        	Singleton.updateMainHandlerDevice(Singleton.MH_MSG_DISCONNECT_DEVICE, dev);
			    	        	dialog.cancel();
			    	        	dismiss();
			    	        }
			    	     })
			    	    .setNegativeButton("No", new OnClickListener() {
			    	        public void onClick(DialogInterface dialog, int which) {
			    	        	dialog.cancel();
			    	        }
			    	     })
			    	     .show();
				}
			});
						
			this.show();
			this.getWindow().setLayout(activity.getWindow().getDecorView().findViewById(android.R.id.content).getWidth()*7/8,activity.getWindow().getDecorView().findViewById(android.R.id.content).getHeight()*5/8);
		}
		
		applyLanguage();
	}
	
	
	public void applyLanguage(){
		try{
			hintTV1.setText("Updating Sticker");
			hintTV2.setText("Name: ");
			cancelButton.setText("Cancel");        
		}catch(Exception e){}
	}
	
	public void changeProgess(LeDevice dev, float firmwareProgress){	
		progressText.setVisibility(TextView.VISIBLE);
		progressText.setText(String.format( "%.2f", firmwareProgress ) + "%");
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setProgress((int) firmwareProgress);
		
		
		if(!dev.isConnected()){
			sKnob.setImageResource(R.drawable.gray_circle);
			knobStatus.setVisibility(View.VISIBLE);
		}
		else{
			sKnob.setImageResource(R.drawable.blue_circle);
			knobStatus.setVisibility(View.INVISIBLE);
		}
		
		if (-1 == (int)firmwareProgress){
			progressText.setText("Sticker successful updated!");
			progressBar.setProgress(0);
		
			Singleton.uiContext.runOnUiThread(new Runnable() {
	            public void run(){
	            	new AlertDialog.Builder(Singleton.uiContext)
	        	    .setTitle("Sticker successful updated!")
	        	    .setMessage("You can use the sticker with your regular StickNFind App now")
	        	    .setNeutralButton("Dismiss", new OnClickListener() {
	        	        public void onClick(DialogInterface dialog, int which) {
	        	        	dialog.cancel();
	        	        	dismiss();
	        	        }
	        	     })
	        	     .show();
	            }
	        });
		
			try 
			{
				Thread.sleep(5000);				
			}catch (Exception e){}
			dismiss();
		}
	}
}
