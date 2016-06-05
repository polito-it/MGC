
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;

public class DisplayPeerJsonInfoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_peer_json_info);
		
		//Get the intent including the path of the json file
		Intent intent = getIntent();
	    String fileName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_FILE);
	    String clientIP = intent.getStringExtra(WifiDirectConnectionInfoListener.EXTRA_MESSAGE_IP);
	    //Read the Json file into a string
		BufferedReader br;
		StringBuilder sbl = new StringBuilder();
		String line;
		
		
		// Read the file into a string
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				sbl.append(line);
			}
			// Close the BufferedReader
			br.close();
			// Parse the Json string
			JSONObject json = new JSONObject(sbl.toString());
			JSONArray interfacesArray = json.getJSONArray("interfaces");
			JSONObject json3g = interfacesArray.getJSONObject(3);
			String str3g = json3g.getString("MAC");
		
			JSONArray systemInfoArray = json.getJSONArray("system_info");
			JSONObject jsonSys = systemInfoArray.getJSONObject(0);
			//Float bat = Float.valueOf(jsonBattery.getString("battery"));
			String bat = jsonSys.getString("battery");
			//Show the string
			((TextView) findViewById(R.id.peer_json_info)).setText(str3g);
			((TextView) findViewById(R.id.peer_ip)).setText(clientIP);
			((TextView) findViewById(R.id.peer_battery)).setText(bat);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_peer_json_info, menu);
		return true;
	}

}
