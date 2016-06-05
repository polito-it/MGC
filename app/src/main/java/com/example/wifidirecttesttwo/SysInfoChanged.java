
/*
 * Copyright 2013 Yufeng Duan, Politecnico di Torino, Turin, Italy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.wifidirecttesttwo;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.ActivityManager;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SysInfoChanged {
	
	private static final int RAM_WEIGHT = 1;
	
	private long RamAvailable;
	private Location GpsLocation;
	private String stateOf3G;
	private TelephonyManager telManager;
	private ServiceState telServiceState;
	private SignalStrength telSignalStrength;
	private String ipAddr3G;
	private String wifiIpAddr;
	private String wifiNetmask;
	private float cpuUsage;
	private String macAddr;
	private String p2pMac;
	

	public SysInfoChanged(Context context) {
		
		p2pMac = "";
		// ---Get the available ram
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);
		RamAvailable = mi.availMem;

		// ---Get the 3G state
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (networkInfo != null)
			stateOf3G = networkInfo.getState().toString();
		else
			stateOf3G = "not available";

		// ---Initialize telephone call back
		telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		PhoneStateListener psListener = new PhoneStateListener() {
			@Override
			public void onServiceStateChanged(ServiceState serviceState) {
				telServiceState = serviceState;
			}

			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				telSignalStrength = signalStrength;
			}
		};
		telManager.listen(psListener, PhoneStateListener.LISTEN_SERVICE_STATE
				| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		// Get the ip address
		ipAddr3G = getLocalIpAddress();

		// Get the Wifi info
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
		if (wifiInfo == null || dhcpInfo == null) {
			wifiIpAddr = "unknow";
			wifiNetmask = "unknow";
			macAddr = "unknown";
		} else {
			wifiIpAddr = intToIpAddr(wifiInfo.getIpAddress());
			wifiNetmask = intToIpAddr(dhcpInfo.netmask);
			macAddr = wifiInfo.getMacAddress();
		}
		
		// Get the p2p MAC address
		try {
			NetworkInterface p2pNi = NetworkInterface.getByName("p2p0");
			byte[] hwAddr = p2pNi.getHardwareAddress();
			for(int i=0; i < hwAddr.length; i++)
				p2pMac += (String.format("%02x", hwAddr[i]) + ':');
			p2pMac = p2pMac.substring(0, 17);
				
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		//Get CPU usage
		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
			String load = reader.readLine();
			String[] toks = load.split(" ");

			long idle1 = Long.parseLong(toks[5]);
			long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])
					+ Long.parseLong(toks[4]) + Long.parseLong(toks[6])
					+ Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
			
			try{
				Thread.sleep(360);
			}catch(Exception e){};
			
			reader.seek(0);
			load = reader.readLine();
			reader.close();
			toks = load.split(" ");
			
			long idle2 = Long.parseLong(toks[5]);
			long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])
					+ Long.parseLong(toks[4]) + Long.parseLong(toks[6])
					+ Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

			cpuUsage = (float) (cpu2-cpu1)/((cpu2+idle2) - (cpu1+idle1));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			cpuUsage = 0;
			e.printStackTrace();
		}

	}

	public long GetRamAvailable() {
		return RamAvailable;
	}

	public void SetLocation(Location location) {
		GpsLocation = location;
	}

	public Location GetLocation() {
		return GpsLocation;
	}

	public String get3GState() {
		return stateOf3G;
	}

	public String getServiceState() {
		String s = "cannot";
		if (telServiceState != null) {
			switch (telServiceState.getState()) {
			case 0:
				s = "in service";
				break;
			case 1:
				s = "out of service";
				break;
			case 2:
				s = "power of";
				break;
			default:
				s = "not known";
				break;
			}
		}
		return s;
	}

	public int getSignalStrength() {
		if (telSignalStrength != null)
			return telSignalStrength.getGsmSignalStrength();
		else
			return 0;
	}

	public String get3GIpAddr() {
		return ipAddr3G;
	}

	public String getWifiIpAddr() {
		return wifiIpAddr;
	}

	public String getWifiNetmask() {
		return wifiNetmask;
	}
	

	public float getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(float cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public String getMacAddr() {
		return macAddr;
	}
	
	public String getP2pMac(){
		return p2pMac;
	}

	// ---Auxiliary functions - get ip address
	public String getLocalIpAddress() {
		try {
			// --- TT:
			String niIp;
			NetworkInterface tni;
			//Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			for(Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements();)
			{
				
				tni = nis.nextElement();
				niIp = tni.getInetAddresses().toString();
			}
			// TT END
			String niName = "eth0";
			// --Get the eth0 interface
			NetworkInterface ni = NetworkInterface.getByName(niName);
			if (ni == null)
				return "unknow";
			// --There are more than one address attached to the interface, get
			// the IPV4 one
			for (Enumeration<InetAddress> enumIpAddr = ni.getInetAddresses(); enumIpAddr
					.hasMoreElements();) {
				InetAddress inetAddress = enumIpAddr.nextElement();
				if (inetAddress.getAddress().length == 4) {
					return inetAddress.getHostAddress().toString();
				}
			}
		} catch (SocketException ex) {
			Log.e("Getting Ip Address", ex.toString());
		}
		return "unknown";
	}

	public static String intToIpAddr(int i) {
		return( i        & 0xFF) + "." +
				((i >>  8 ) & 0xFF) + "." +
				((i >> 16 ) & 0xFF) + "." +
				((i >> 24 ) & 0xFF);
		/*return ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
				+ ((i >> 8) & 0xFF) + "." + (i & 0xFF);*/
	}
	
	public static int lookupHost(String hostname) {
	    InetAddress inetAddress;
	    try {
	        inetAddress = InetAddress.getByName(hostname);
	    } catch (Exception e) {
	        return -1;
	    }
	    byte[] addrBytes;
	    int addr;
	    addrBytes = inetAddress.getAddress();
	    addr = ((addrBytes[3] & 0xff) << 24)
	            | ((addrBytes[2] & 0xff) << 16)
	            | ((addrBytes[1] & 0xff) << 8)
	            |  (addrBytes[0] & 0xff);
	    String a = intToIpAddr(addr);
	    String b = a;
	    return addr;
	}
	public static int lookupHost2(String hostname) {
	    InetAddress inetAddress;
	    try {
	        inetAddress = InetAddress.getByName(hostname);
	    } catch (Exception e) {
	        return -1;
	    }
	    byte[] addrBytes;
	    int addr;
	    addrBytes = inetAddress.getAddress();
	    addr = ((addrBytes[0] & 0xff) << 24)
	            | ((addrBytes[1] & 0xff) << 16)
	            | ((addrBytes[2] & 0xff) << 8)
	            |  (addrBytes[3] & 0xff);
	    String a = intToIpAddr(addr);
	    String b = a;
	    return addr;
	}
}