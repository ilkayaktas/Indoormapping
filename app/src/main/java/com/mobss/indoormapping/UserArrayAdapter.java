package com.mobss.indoormapping;

import java.util.Vector;

import com.ssia.sticknfind.sdk.LeDevice;
import com.ssia.sticknfind.sdk.LeSnfDevice;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

public class UserArrayAdapter extends ArrayAdapter<LeDevice> {
	private Vector<LeDevice> mList;
	private Context mContext;

	public UserArrayAdapter(Context context, Vector<LeDevice> list) {
		super(context, R.layout.snf_scan_list_item, list);
		this.mList=list;
		this.mContext = context;
	}
 
	private class StickerContentItem
	{
		private Button mButton;
		private TextView mRssi, mBatt, mTemp;
	
		public StickerContentItem(LeDevice dev, Button but, TextView rssi, TextView batt, TextView temp){
			mButton = but;
			mRssi = rssi;
			mBatt = batt;
			mTemp = temp;
		}
		
	}
	
	private void changeBgdColor(LeDevice dev, View view, Button name){
		if (dev.isConnected()){
			if (dev.isDiscoveryDone()){
				view.setBackgroundColor(Color.argb(125, 0, 0, 150));
			}else{
				view.setBackgroundColor(Color.argb(125, 0, 0, 50));
			}
		}else if (dev.isBonded()){
			view.setBackgroundColor(Color.argb(125, 0, 150, 0)); 
		}else if (dev.isBonding()){
			view.setBackgroundColor(Color.argb(125, 0, 15, 0));
		}else if (dev.isConnecting()){
			view.setBackgroundColor(Color.argb(125, 0, 0, 255));
		}else{
			view.setBackgroundColor(Color.argb(125, 30, 0, 0));
		}
		
		view.postInvalidate();
	}
	
	private View displayStickers(LeDevice dev, int position, View convertView, final ViewGroup parent) {
		Button name;
		TextView temp, batt, rssi;
				
		if (convertView == null){
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
			convertView = inflater.inflate(R.layout.snf_scan_list_item, parent, false);

			name = (Button) convertView.findViewById(R.id.name);
			name.setText(dev.mName + "/" + dev.getBtDevice().getAddress().substring(0,5));
						
			changeBgdColor(dev, convertView, name);
			
			name.setEnabled(true);
			name.setTag(dev);
			dev.setBroadcastRate(100, 100);
			name.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					PopupMenu popupMenu = new PopupMenu(parent.getContext(), v);
					final LeDevice dev = (LeDevice)v.getTag();
					popupMenu.getMenuInflater().inflate(R.menu.popupmenu, popupMenu.getMenu());
					popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							switch (item.getItemId()){
							case R.id.lemenu:
								Singleton.updateMainHandlerDevice(Singleton.MH_MSG_BEACON_UPDATE, dev);
								dev.setFirmwareLoadActive(true);
								dev.startFirmwareUpdate();
								break;
							case R.id.connect:
								Singleton.updateMainHandlerDevice(Singleton.MH_MSG_CONNECT_DEVICE, dev);
								break;
							case R.id.disconnect:
								Singleton.updateMainHandlerDevice(Singleton.MH_MSG_DISCONNECT_DEVICE, dev);
								break;
							case R.id.alert:
								Singleton.updateMainHandlerDevice(Singleton.MH_MSG_ALERT_DEVICE, dev);
								break;
							case R.id.rbond:
								Singleton.updateMainHandlerDevice(Singleton.MH_MSG_DEVICE_RBOND, dev);
								break;
							}
							return false;
						}
					});
					popupMenu.show();
				}
				
			});
			////////////////////////////////////////////////////////////////
			temp = (TextView) convertView.findViewById(R.id.temp);
			temp.setText("Tmp: " + ((LeSnfDevice)dev).mTemperature);

			batt = (TextView) convertView.findViewById(R.id.battery);
			batt.setText("Btry: " + ((LeSnfDevice)dev).mBatteryLevel);

			rssi = (TextView) convertView.findViewById(R.id.rssi);
			rssi.setText("Rssi: " + dev.mRssi);
			convertView.setTag(new StickerContentItem(dev, name, rssi, batt, temp));
			
			
		}else if (convertView.getTag() != null){
			StickerContentItem item;
			if (convertView.getTag().getClass().equals(StickerContentItem.class))
				item = (StickerContentItem)convertView.getTag();
			else{
				return displayStickers(dev, position, null, parent);
			}

			name = item.mButton;
			////////////////////////////////////////////////////////////////
			temp = item.mTemp;
			temp.setText("Temprature: " + ((LeSnfDevice)dev).mTemperature);
			
			batt = item.mBatt;
			batt.setText("Battery Level: " + ((LeSnfDevice)dev).mBatteryLevel);
 
			rssi = item.mRssi;
			rssi.setText("Rssi: " + dev.mRssi);
			
			////////////////////////////////////////////////////////////////
			
			changeBgdColor(dev, convertView, name);
			name.setText(dev.mName + "/" + dev.getBtDevice().getAddress().substring(0,5));
			name.setTag(dev);
		}else{
			Log.i("Result", "Unkown");
		}

		return convertView; 
	}
	
	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {

		final LeDevice dev=mList.get(position);
				
		return displayStickers(dev, position, convertView, parent);		
	}
}
