
/*
 * Copyright 2014 Yufeng Duan, Politecnico di Torino, Turin, Italy
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import android.os.SystemClock;
import android.util.Log;


public class AdvertisementTest extends Thread {

	private int number;
	private ContentRoutingLayer crl;
	private MainActivity activity;

	public AdvertisementTest(int number, ContentRoutingLayer crl, MainActivity activity) {
		super("AdvertisementTest");
		this.number = number;
		this.crl = crl;
		this.activity = activity;
	}

	public void run(){

		Random r = new Random();
		int serviceName = r.nextInt();
		long tstamp;
		FileWriter file = null;

		try {
			File f = new File(activity.getExternalFilesDir(null),"sentTimeStamp.txt");
			file = new FileWriter(f, true);

			for(int i=0; i<this.number; i++){
				tstamp = System.currentTimeMillis();
				Log.d("ADVERTISEMENT TEST", "Packet number " + i + ", timestamp " + System.currentTimeMillis());
				crl.addOwnService(String.valueOf(serviceName+i));
				SystemClock.sleep(1000);
				if(file!=null)
					file.append(String.valueOf(tstamp) + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(file!=null)
				try {
					file.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}


}
