
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;

public class SystemInfoJson {
	public JSONObject jobj;
	public String FinalString;

	@SuppressLint("NewApi")
	public SystemInfoJson(MainActivity activity) {
		// Whole new json obj
		jobj = new JSONObject();
		// float batteryPct;

		JSONArray SInfoArray = new JSONArray();
		JSONObject SInfoObj = new JSONObject();
		JSONArray LocationArray = new JSONArray();
		JSONObject LocationObj = new JSONObject();
		JSONArray InterfacesArray = new JSONArray();
		JSONObject IfaceObj1 = new JSONObject();
		JSONObject IfaceObj2 = new JSONObject();
		JSONObject IfaceObj3 = new JSONObject();
		JSONObject IfaceObj4 = new JSONObject(); // p2p

		try {
			// ---"system_info"
			SInfoObj.put("battery", activity.batteryPct);
			SInfoObj.put("disk", activity.sysInfoGether.getInternalDiskSize());
			SInfoObj.put("available_disk", activity.sysInfoGether.getAvailableDiskSize());
			SInfoObj.put("ram_available", activity.sysInfoChanged.GetRamAvailable());
			SInfoObj.put("cpu_usage", activity.sysInfoChanged.getCpuUsage());
			SInfoObj.put("CPU", activity.sysInfoChanged.getCpuUsage());
			SInfoArray.put(SInfoObj); // System info array
			jobj.put("system_info", SInfoArray);// "system_info"

			// ---"location"
			// Process the location
			Location location = activity.sysInfoChanged.GetLocation();
			long obTime = 0;// obsolete time, unit:(ms)
			// If we did not receive a location call back
			if (location == null) {
				String locType = "gps";
				Location lastKnownLocation = activity.locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (lastKnownLocation == null) {
					lastKnownLocation = activity.locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					locType = "Network";
				}
				if (lastKnownLocation == null) {
					LocationObj.put("type", locType);
					LocationObj.put("latitude", 0);
					LocationObj.put("longitude", 0);
					LocationObj.put("speed", 0);
					LocationObj.put("accuracy", 0);
					LocationObj.put("bearing", 0);
					LocationObj.put("obsolete_time", -1);
				} else {
					location = lastKnownLocation;
					obTime = (SystemClock.elapsedRealtimeNanos() - location
							.getElapsedRealtimeNanos()) / 1000000;
					LocationObj.put("type", locType);
					LocationObj.put("latitude", location.getLatitude());
					LocationObj.put("longitude", location.getLongitude());
					LocationObj.put("speed", location.getSpeed());
					LocationObj.put("accuracy", location.getAccuracy());
					LocationObj.put("bearing", location.getBearing());
					LocationObj.put("obsolete_time", obTime);
				}
			} else {
				obTime = (SystemClock.elapsedRealtimeNanos() - location
						.getElapsedRealtimeNanos()) / 100000;
				LocationObj.put("type", "gps");
				LocationObj.put("latitude", location.getLatitude());
				LocationObj.put("longitude", location.getLongitude());
				LocationObj.put("speed", location.getSpeed());
				LocationObj.put("accuracy", location.getAccuracy());
				LocationObj.put("bearing", location.getBearing());
				LocationObj.put("obsolete_time", obTime);
			}

			LocationArray.put(LocationObj);// Location Array
			jobj.put("location", LocationArray);// "location"

			// ---"interfaces"
			IfaceObj1.put("type", "BT");
			IfaceObj1.put("available",
					activity.sysInfoGether.getBtAvailability());
			InterfacesArray.put(IfaceObj1);

			// Process the phone state and the signal strength

			IfaceObj2.put("type", "3G");
			IfaceObj2.put("state", activity.sysInfoChanged.get3GState());
			IfaceObj2.put("service_state",
					activity.sysInfoChanged.getServiceState());
			IfaceObj2.put("cell_id", "");
			IfaceObj2.put("signalstrength",
					activity.sysInfoChanged.getSignalStrength());
			IfaceObj2.put("address", activity.sysInfoChanged.get3GIpAddr());
			IfaceObj2.put("netmask", "255.255.0.0");
			InterfacesArray.put(IfaceObj2);

			// Wifi interface
			IfaceObj3.put("type", "wifi");
			IfaceObj3.put("address", activity.sysInfoChanged.getWifiIpAddr());
			IfaceObj3.put("netmask", activity.sysInfoChanged.getWifiNetmask());
			IfaceObj3.put("MAC", activity.sysInfoChanged.getMacAddr());
			InterfacesArray.put(IfaceObj3);
			
			// P2P interface
			IfaceObj4.put("type", "p2p");
			IfaceObj4.put("MAC", activity.sysInfoChanged.getP2pMac());
			InterfacesArray.put(IfaceObj4);
			
			jobj.put("interfaces", InterfacesArray);// "intefaces"

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getJsonString() {
		try {
			FinalString = jobj.toString(1);
			return FinalString;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}