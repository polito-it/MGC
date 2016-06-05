
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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by dyf on 6/8/15.
 */
public class ContentFileServer {
    public final static int UDP_SERVER_PORT = 55555;
    public final static int UDP_SERVER_BUFFER_LEN = 1500; // cambio hecho el 19/02/14

    private final int megaByteSizeT2 = 2*1048576+1; // int byte, equals to 2MB
    private final int numberPacketsT2;
    private final int packetSize = 1400;

    private MainActivity activity;
    private Bundle udpMsgBundle;
    private Handler udpTextHandler;
    private Message udpMsg;
    //private File logFile; // --- Not used in Demo
    //private BufferedWriter buf; // --- Not used in Demo
    private int count;
    private long requestTime;
    private long firstPacketTime;
    private long lastPacketTime;
    private long t1Time;
    private long t2Time;
    private long t3Time;
    private double throughput;


    public ContentFileServer(MainActivity activity) {
        super();
        this.activity = activity;

        udpTextHandler = this.activity.getUdpTextHandler();
        //this.messageText = activity.getString(R.string.numberPacketsText);

        this.numberPacketsT2 = megaByteSizeT2/packetSize + 1; //  2MB/1400B = 1428

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
        this.throughput = 0;

        /* --- For the demo, useless
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
        */
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

        /* --- Not used in Demo
        try {
            buf.append(String.valueOf(timeArrived));
            buf.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        if((count+1) % 200 == 0) {
            udpMsgBundle = new Bundle(3);
            udpMsgBundle.putInt(MainActivity.PACKET_COUNT, count);
            udpMsgBundle.putLong(MainActivity.T1_TIME, t1Time);
            udpMsgBundle.putLong(MainActivity.T2_TIME, t2Time);
            udpMsgBundle.putLong(MainActivity.T3_TIME, t3Time);
            udpMsg = udpTextHandler.obtainMessage(MainActivity.MESSAGE_UDP_SERVER);
            udpMsg.setData(udpMsgBundle);
            udpTextHandler.sendMessage(udpMsg);
        }
    }

    /* --- Not used in Demo
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
    */

    public void closeFile() {
        this.reset();
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
        // byte * 8 / 1000000 / (t2Time / 1000)
        this.throughput = ((long)this.megaByteSizeT2) * 8 / this.t2Time / 1000;
        updateStatusTextView("Throughput: " + String.valueOf(throughput) + " Mbit/s\n");
    }

    private void updateStatusTextView(String text){
        Handler handler = activity.getStatusTextHandler();
        Bundle data = new Bundle(1);
        data.putString(MainActivity.STATUS_MESSAGE, text);
        Message msg = handler.obtainMessage();
        msg.setData(data);
        handler.sendMessage(msg);
    }
}
