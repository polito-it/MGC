
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

package com.example.wifidirecttesttwo; //new version

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class FileServer {

	public final static int UDP_SERVER_PORT = 55555;
	public final static int UDP_SERVER_BUFFER_LEN = 1500; // cambio hecho el 19/02/14
	
	private final int megaByteSizeT2 = 2*1048576+1;
	private final int numberPacketsT2;
	private final int packetSize = 1400;
	
	private MainActivity activity;
	private Bundle udpMsgBundle;
	private Handler udpTextHandler;
	private Message udpMsg;
	private File logFile;
	private BufferedWriter buf;
	private int count;
	private long requestTime;
	private long firstPacketTime;
	private long lastPacketTime;
	private long t1Time;
	private long t2Time;
	private long t3Time;
	

	public FileServer(MainActivity activity) {
		super();
		this.activity = activity;
		
		udpTextHandler = this.activity.getUdpTextHandler();
		//this.messageText = activity.getString(R.string.numberPacketsText);

		this.numberPacketsT2 = megaByteSizeT2/packetSize + 1;
		
		this.reset();
	}


	private void reset() {

		this.count = 0;
		this.requestTime = 0;
		this.t1Time = 0;
		this.firstPacketTime = 0;
		this.lastPacketTime = 0;
		this.t2Time = 0;
		this.t3Time = 0;
		
		logFile = new File(activity.getExternalFilesDir(null),"RECEIVED_120.txt");
		
		if (!logFile.exists())
		{
			try
			{
				logFile.createNewFile();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		try {
			buf = new BufferedWriter(new FileWriter(logFile, true));
		} catch (IOException e2) {
			e2.printStackTrace();
		} 
		//String text = messageText + " " + String.valueOf(count);

		udpMsgBundle = new Bundle(3);
		udpMsgBundle.putInt(MainActivity.PACKET_COUNT, count);
		udpMsgBundle.putLong(MainActivity.T1_TIME, t1Time);
		udpMsgBundle.putLong(MainActivity.T2_TIME, t2Time);
		udpMsgBundle.putLong(MainActivity.T3_TIME, t3Time);
		udpMsg = udpTextHandler.obtainMessage(MainActivity.MESSAGE_UDP_SERVER);
		udpMsg.setData(udpMsgBundle);
		udpTextHandler.sendMessage(udpMsg);

	}


	public void manageFilePacket(){

		long timeArrived = System.currentTimeMillis();
		count++;
		
		if(count==1)
			this.computeT1Time();
			
		if(count == this.numberPacketsT2)
			this.computeT2Time();

		t3Time = timeArrived-this.firstPacketTime;
		
		try {
			buf.append(String.valueOf(timeArrived));
			buf.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		udpMsgBundle = new Bundle(3);
		udpMsgBundle.putInt(MainActivity.PACKET_COUNT, count);
		udpMsgBundle.putLong(MainActivity.T1_TIME, t1Time);
		udpMsgBundle.putLong(MainActivity.T2_TIME, t2Time);
		udpMsgBundle.putLong(MainActivity.T3_TIME, t3Time);
		udpMsg = udpTextHandler.obtainMessage(MainActivity.MESSAGE_UDP_SERVER);
		udpMsg.setData(udpMsgBundle);
		udpTextHandler.sendMessage(udpMsg);
	}


	public void closeFile(){
		try {
			this.buf.flush();
			//this.buf.close();
			this.buf = null;
			this.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void setRequestTime(){
		this.requestTime = System.currentTimeMillis();
	}

	private void computeT1Time(){
		this.firstPacketTime = System.currentTimeMillis();
		this.t1Time = this.firstPacketTime - this.requestTime;
	}

	private void computeT2Time(){
		this.lastPacketTime = System.currentTimeMillis();
		this.t2Time = this.lastPacketTime - this.firstPacketTime;
	}
}
