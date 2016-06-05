
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
//import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
//import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ContentRoutingLayer {

	private final static String TAG = "ContentRoutingLayer";
	//public final static byte GENERIC_TEST_DATA = 0;
	public final static byte SERVICE_APPLICATION_REQUEST = 1;
	public final static byte NEW_JOINING_CLIENT_NOTIFICATION = 2;
	public final static byte SERVICE_ADVERTISE_NOTIFICATION_RESPONSE = 3;
	public final static byte SERVICE_ADVERTISE_LIST_REQUEST = 4;
	public final static byte CLIENT_DOWN_NOTIFICATION_REQUEST = 5;
	public final static byte SERVICE_ADVERTISE_BEACON = 6;
	public final static byte SERVICE_NOTIFICATION_MESSAGE = 7;
	public final static byte SERVICE_ADVERTISE_LIST_REPLY = 104;
	public final static byte SERVICE_APPLICATION_REPLY = 101;
	public final static byte NOT_FOUND_SERVICE = 100;
	//public final static byte CLIENT_DOWN_NOTIFICATION_REPLY = 105;
	public final static byte SERVICE_NOTIFICATION_MESSAGE_ACK = 107;

	public final static String ELECTED_CLIENT_TEXT_ROLE = "Elected Client";

    /*
	public final static String LEGACY_CLIENT_TEXT_ROLE = "Legacy Client";
	public final static String GROUP_OWNER_TEXT_ROLE = "Group Owner";
	public final static String CLIENT_TEXT_ROLE = "Client";
	*/

	private final static int BEACON_DELAY = 30000;
	private final static int BEACON_PERIOD = 30000;



	private Random random;

	private ContentRoutingDualTable contentDualTable;
	private Map<String, List<Pair<InetAddress, Integer>>> pit;


    /*
	private DnsSdTxtRecordListener txtListener;
	private DnsSdServiceResponseListener servListener;
	private Boolean isServiceRequested;
    */

	private MainActivity mActivity;
	//private Channel channel;
	//private WifiP2pManager manager;

	private GroupRoutingLayer grLayer;
	private boolean groupFormed;

	//used by the legacy client only
	private WifiP2pClient electedClient;
	private InetAddress electedClientAddress;
	private InetAddress externalElectedClientAddress;
	
	private InetAddress wiFiAddress;

	//used by the elected client only
	private List<InetAddress> legacyClientAddresses;

	//flags for elected client
	//private boolean listSent;
	private boolean listReceived;

	//flags for group owner
	//private boolean notificationSent;
	private boolean notificationReceived;

	//private boolean beaconSent;
	//private boolean beaconReceived;

	private NotificationQueue notificationQueue;

	//private Timer beaconTimer;
	//private TimerTask beaconTimerTask;
	private boolean beaconing;

	private ContentFileServer fileServer;

	////////////////////// TEMP - TO BE REMOVED LATER
	private List<String> ownProvidedServices;

	private byte serviceBuffer[];
	private byte dataBuffer[];
	
	private FileWriter logContent;


    /*
	public ContentRoutingLayer(MainActivity mact, Channel c, WifiP2pManager wm) {

		this.electedClient = null;
		this.contentDualTable = new ContentRoutingDualTable();
		this.pit = new HashMap<String, List<Pair<InetAddress, Integer>>>();

		this.electedClientAddress = null;
		this.externalElectedClientAddress = null;
		this.wiFiAddress = null;
		this.legacyClientAddresses = Collections.synchronizedList(new ArrayList<InetAddress>());

		this.listReceived = false;
		this.listSent = false;
		this.notificationReceived = false;
		this.notificationSent = false;

		this.notificationQueue = new NotificationQueue(this);

		this.mActivity = mact;
		this.channel = c;
		this.manager = wm;

		this.groupFormed = false;

		//this.isServiceRequested = false;
		this.random = new Random();

		this.beaconing=false;

		this.ownProvidedServices = Collections.synchronizedList(new ArrayList<String>());
		
		this.fileServer = new FileServer(mActivity);
		this.serviceBuffer = new byte[256];
		this.dataBuffer = new byte[1500];

		File f = new File(mActivity.getExternalFilesDir(null),"receivedTimeStamp.txt");
		try {
			logContent = new FileWriter(f);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// grLayer = new GroupRoutingLayer(this);
	}
    */

    public ContentRoutingLayer(MainActivity mact) {

        this.electedClient = null;
        this.contentDualTable = new ContentRoutingDualTable();
        this.pit = new HashMap<String, List<Pair<InetAddress, Integer>>>();

        this.electedClientAddress = null;
        this.externalElectedClientAddress = null;
        this.wiFiAddress = null;
        this.legacyClientAddresses = Collections.synchronizedList(new ArrayList<InetAddress>());

        this.listReceived = false;
        //this.listSent = false;
        this.notificationReceived = false;
        //this.notificationSent = false;

        this.notificationQueue = new NotificationQueue(this);

        this.mActivity = mact;
        //this.channel = c;
        //this.manager = wm;

        this.groupFormed = false;

        //this.isServiceRequested = false;
        this.random = new Random();

        this.beaconing=false;

        this.ownProvidedServices = Collections.synchronizedList(new ArrayList<String>());

        this.fileServer = new ContentFileServer(mActivity);
        this.serviceBuffer = new byte[256];
        this.dataBuffer = new byte[1500];

        File f = new File(mActivity.getExternalFilesDir(null),"receivedTimeStamp.txt");
        try {
            logContent = new FileWriter(f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // grLayer = new GroupRoutingLayer(this);
    }

	public void startBeaconing(){
		
		if(!mActivity.isGO())
			return;
		
		if(!this.beaconing){
            Timer beaconTimer = new Timer();
            TimerTask beaconTimerTask = new TimerTask() {
				@Override
				public void run() {
					sendAdvertiseBeacon();
				}
			};
			beaconTimer.schedule(beaconTimerTask, ContentRoutingLayer.BEACON_DELAY,
					ContentRoutingLayer.BEACON_PERIOD);
			beaconing=true;
		}
	}



	// TODO MUST BE IMPLEMENTED IN A SEPARATE THREAD (SEE ASYNC SERVICE)
	// *************
	public void deliverPacket(DatagramPacket packet, Boolean arrived) {
		ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(),
				0, packet.getLength());
		DataInputStream dis = new DataInputStream(bais);
		Byte packetType = null;
		try {
			packetType = dis.readByte();
		} catch (IOException ioe) {
            Log.d("WiFi2", ioe.toString());
        }

		try {
			switch (packetType.intValue()) {
			case SERVICE_APPLICATION_REQUEST:
				manageApplicationRequest(packet, dis);
				break;
			case SERVICE_APPLICATION_REPLY:
				manageApplicationReply(packet, dis);
				break;
			case NEW_JOINING_CLIENT_NOTIFICATION:
				manageAdvertiseNotification(dis);
				break;
			case SERVICE_ADVERTISE_NOTIFICATION_RESPONSE:
				manageAdvertiseNotificationResponse();
				break;
			case SERVICE_ADVERTISE_LIST_REQUEST:
				manageAdvertiseListRequest(packet, dis);
				break;
			case SERVICE_ADVERTISE_LIST_REPLY:
				manageAdvertiseListReply(packet, dis);
				break;
			case CLIENT_DOWN_NOTIFICATION_REQUEST:
				manageClientDownNotification(dis);
				break;
			case SERVICE_ADVERTISE_BEACON:
				manageAdvertiseBeacon(packet, dis);
				break;
			case SERVICE_NOTIFICATION_MESSAGE:
				manageServiceNotificationMessage(packet, dis);
				break;
			case SERVICE_NOTIFICATION_MESSAGE_ACK:
				manageServiceNotificationMessageAck(packet, dis);
				break;
			case NOT_FOUND_SERVICE:
				break;
			}
			
			bais.close();
			dis.close();
			
		} catch (NoPitEntryException npe) {
			// a reply message doesn't have a match in the pit table
			Log.w(TAG, npe.getMessage(), npe);
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}





	//#####################################################################
	// MANAGE METHODS
	//#####################################################################


	private void manageAdvertiseNotificationResponse() {
		//this.notificationSent = false;
		this.notificationReceived = true;
	}


	private void manageAdvertiseNotification(DataInputStream dis) {
		// Process the notification packet, get the IP
        // Actually it is managing new joining legacy client notification (received from GO)

		byte[] byteIP = new byte[4];
		InetAddress ip;
		try {
            if(dis.read(byteIP, 0, 4) != 4) return;
            ip = InetAddress.getByAddress(byteIP);
			if (ip.equals(grLayer.getP2pAddress())) {
                // If the packet is toward me, I become the elected client
				this.mActivity.setIsElectedClient(true);
                // Show "Elected Client" on UI
				Handler mainActivityRoleHandler = mActivity.getRoleTextHandler();
				Bundle data = new Bundle(1);
				data.putString(MainActivity.ROLE_TEXT_VIEW_MESSAGE, ELECTED_CLIENT_TEXT_ROLE);
				Message msg = mainActivityRoleHandler.obtainMessage();
				msg.setData(data);
				mainActivityRoleHandler.sendMessage(msg);
                // Send SANResp to my GO
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(baos);
				try {
					dos.write(SERVICE_ADVERTISE_NOTIFICATION_RESPONSE);
					byte[] payload = baos.toByteArray();
					grLayer.forwardPacket(payload,
							InetAddress.getByName("192.168.49.1"), null);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				// Send SALReqt
                // To spread the info within this group in broadcast
				sendAdvertiseListRequest();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void manageAdvertiseListRequest(DatagramPacket packet, DataInputStream dis) {
		//------ DYF ------ If I am not the legacy client, I do not respond to the SALQ
		if(!mActivity.isLegacyClient())
			return;
		// Send advertisement reply
		InetAddress relayAddress = packet.getAddress();

        // Now I know the external elected client, and send back the ADVERTISE_LIST_REPLY
		externalElectedClientAddress = relayAddress;
		sendAdvertiseReply(relayAddress);

		WifiP2pClient client = contentDualTable.getByClient(relayAddress
				.getHostAddress());
		if (client == null) {
			client = new WifiP2pClient(relayAddress, null, null, false);
			contentDualTable.putClient(client);
		}
		else{
			client.setSameGroup(false);
		}

		// Process advertisement list request
		try {
			int reqServiceNumber = dis.read();
			int serviceLen;

			byte[] bufName = new byte[256];

			// Update the table
			for (int i = 0; i < reqServiceNumber; i++) {

				// Sevice Name
				serviceLen = dis.read();
				if(dis.read(bufName, 0, serviceLen) != serviceLen) return; // ---Modified
				// Check if the service is already in the table
				String serviceName = new String(bufName, 0, serviceLen);
				DnsServiceInfo dnsServiceInfo;

				if (contentDualTable.containsServiceKey(serviceName)) {
					dnsServiceInfo = contentDualTable.getByService(serviceName);
				} else {
					dnsServiceInfo = new DnsServiceInfo(serviceName,
							relayAddress);
					contentDualTable.putService(dnsServiceInfo);
				}

				dnsServiceInfo
				.addProvidingClient(relayAddress.getHostAddress());
				client.addService(serviceName);
			}

			this.updateServicesNumber();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void manageAdvertiseListReply(DatagramPacket packet,
			DataInputStream dis) {

		InetAddress relayAddress = packet.getAddress();
		if(!this.legacyClientAddresses.contains(relayAddress))
			this.legacyClientAddresses.add(relayAddress);

		this.listReceived = true;
		//this.listSent = false;

		WifiP2pClient client = contentDualTable.getByClient(relayAddress
				.getHostAddress());

		if (client == null) {
			client = new WifiP2pClient(relayAddress, null, null, false);
			contentDualTable.putClient(client);
		}
		else{
			client.setSameGroup(false);
		}

		// Process advertisement list request
		try {
			int reqServiceNumber = dis.read();
			int serviceLen;

			byte[] bufName = new byte[256];

			// Update the table
			for (int i = 0; i < reqServiceNumber; i++) {

				// Service Name
				serviceLen = dis.read();
				if(dis.read(bufName, 0, serviceLen) != serviceLen) return; //---Modified
				// Check if the service is already in the table
				String serviceName = new String(bufName, 0 , serviceLen);
				DnsServiceInfo dnsServiceInfo;

				if (contentDualTable.containsServiceKey(serviceName)) {
					dnsServiceInfo = contentDualTable.getByService(serviceName);
				} else {
					dnsServiceInfo = new DnsServiceInfo(serviceName,
							relayAddress);
					contentDualTable.putService(dnsServiceInfo);
				}

				dnsServiceInfo
				.addProvidingClient(relayAddress.getHostAddress());
				client.addService(serviceName);
			}

			Handler handler = mActivity.getNumberServicesTextHandler();
			Bundle data = new Bundle(1);
			data.putInt(MainActivity.NUMBER_DISCOVERED_SERVICES, contentDualTable.numberOfServices());
			Message msg = handler.obtainMessage();
			msg.setData(data);
			handler.sendMessage(msg);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void manageApplicationRequest(DatagramPacket packet, DataInputStream dis) {
		
		byte[] addBuffer = new byte[4];
		InetAddress inetAddBuffer;
		byte[] serviceBuffer = null, dataBuffer;
		String service;
		int serviceLength = 0, dataLength;
		int nonce = 0;

		try {
			if(dis.read(addBuffer) != 4) return; // Try to extract the ip address ---Modified
			inetAddBuffer = InetAddress.getByAddress(addBuffer);
			
			if(!inetAddBuffer.equals(grLayer.getP2pAddress()) && !inetAddBuffer.equals(wiFiAddress)
					&& !inetAddBuffer.getHostAddress().equals("192.168.49.255"))
				return;
			
			nonce = dis.readInt();
			serviceLength = dis.readByte();
			if (serviceLength > 0){
				serviceBuffer = new byte[serviceLength];
				if(dis.read(serviceBuffer, 0, serviceLength)!=serviceLength) return; // ---Modified
			}
			dataLength = dis.readByte();
			if(dataLength > 0){
				dataBuffer = new byte[dataLength];
				if(dis.read(dataBuffer, 0, dataLength) != dataLength) return; // ---Modified
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(serviceBuffer!=null)
			service = new String(serviceBuffer, 0, serviceLength);
		else
			return;

        /* --- Old version
		if(this.ownProvidedServices.contains(service) && service.equals("file")){
			grLayer.forwardFile(packet.getAddress(), nonce);
			return;
		}
		*/

        if(this.ownProvidedServices.contains(service)){
            // update UI
            updateStatusTextView("Sending reply: content name <" + service + ">...", true);
            // TODO: Error in the following method
            grLayer.forwardFile(packet.getAddress(), nonce, service);
            return;
        }

		DnsServiceInfo dsinfo = contentDualTable.getByService(service);

		if (dsinfo == null) {
			// notify back. Send a 102 packet to packet.getAddress()
			this.sendBackNotification(packet.getData(), packet.getAddress(), null);
			Log.d(TAG, "No route for service " + service);
			return;
		}

		List<Pair<InetAddress, Integer>> list = this.pit.get(service);
		if (list == null) {
			list = new ArrayList<Pair<InetAddress, Integer>>();
			list.add(new Pair<InetAddress, Integer>(packet.getAddress(),
					nonce)); // ---Modified
			this.pit.put(service, list);
		} else {
			list.add(new Pair<InetAddress, Integer>(packet.getAddress(),
					nonce));
		}
		
		if(this.ownProvidedServices.contains(service)){
			
			mActivity.onPacketArrived(SERVICE_APPLICATION_REQUEST, nonce, service.getBytes(), null);
			return;
		}

		InetAddress add = dsinfo.getNextHopIP();
		byte[] temp = packet.getData();
		System.arraycopy( add.getAddress(), 0, temp, 1, 4);
		//packet.setData(temp);
		
		grLayer.forwardPacket(packet.getData(), add, null);

	}


	private void manageApplicationReply(DatagramPacket packet, DataInputStream dis)
			throws NoPitEntryException {

		byte[] addBuffer = new byte[4];
		InetAddress inetAddBuffer;
		//byte[] dataBuffer = null;
		String service = null; // data = null;
		int serviceLength = 0, dataLength = 0;
		int nonce = 0;

		try {
			if(dis.read(addBuffer) != 4) return; // Try to extract IP address ---Modified
			inetAddBuffer = InetAddress.getByAddress(addBuffer);
			
			if(!inetAddBuffer.equals(grLayer.getP2pAddress()) && !inetAddBuffer.equals(wiFiAddress)
					&& !inetAddBuffer.getHostAddress().equals("192.168.49.255"))
				return;
			
			nonce = dis.readInt();
			serviceLength = dis.readByte();
			if (serviceLength > 0){
				if(dis.read(serviceBuffer, 0, serviceLength) != serviceLength) return; //---Modified
			}
			dataLength = dis.readByte();
			if(dataLength > 0){
				if(dis.read(dataBuffer, 0, dataLength) != dataLength) return; //---Modified
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		if(serviceBuffer!=null)
			service = new String(serviceBuffer, 0, serviceLength);
		
		//if(dataBuffer!=null)
		//	data = new String(dataBuffer, 0, dataLength);

		List<Pair<InetAddress, Integer>> list = this.pit.get(service);
		if (list == null) {
			throw new NoPitEntryException("Pit entry not found");
		}

		// send the packet to all the address of the list
		// ############################################### ->
		for (Pair<InetAddress, Integer> pair : list) {
			if (nonce == pair.second) {
				InetAddress rec = pair.first;
				
				if(dataLength==-1) {//last packet of the stream
					list.remove(pair);
					return;
				}
				
				if(rec == null){ // If the request is sent by myself
					//mActivity.onPacketArrived(SERVICE_APPLICATION_REPLY, nonce, serviceBuffer, dataBuffer);
                    /* --- Old version
					if(service!=null && service.equals("file")){
						//InetAddress inettemp = packet.getAddress();
						//int ntemp = packet.getData().length;
						fileServer.manageFilePacket();
					}
					*/

                    if(service!=null){
                        //InetAddress inettemp = packet.getAddress();
                        //int ntemp = packet.getData().length;
                        fileServer.manageFilePacket();
                    }
				}
				else{
					InetAddress add = pair.first;
					byte[] temp = packet.getData();
					System.arraycopy( add.getAddress(), 0, temp, 1, 4);
					//packet.setData(temp);
					
					grLayer.forwardPacket(packet.getData(), add, null);
					
				}
			}
		}

		// list.clear();
		// this.pit.remove(service);

	}

	private void manageClientDownNotification(DataInputStream dis) {
		byte[] ipbuffer = new byte[4];
		try {
			if(dis.read(ipbuffer) != 4) return; //*---Modified
		} catch (IOException e) {
			e.printStackTrace();
		}
		contentDualTable.removeClient(ipbuffer);
		// TODO if client elected, notify the legacy client about changes in
		// services
	}



	private void manageAdvertiseBeacon(DatagramPacket packet, DataInputStream dis){

		if(mActivity.isLegacyClient())
			return;
		
		if(mActivity.isGO())
			return;

		// Process advertisement list request
		try {
			//long tstamp = System.currentTimeMillis();
            long tstamp;
			int servicesNumber = dis.read();
			byte[] bufName = new byte[256];
			byte[] bufClient = new byte[4];
			int serviceLen;
			int clientsNumber;
			WifiP2pClient client;
			String serviceName, clientIP; 

			// Update the table
			for (int i = 0; i < servicesNumber; i++) {

				// Service Name
				serviceLen = dis.read();
				if(dis.read(bufName, 0, serviceLen) != serviceLen) return; //---Modified
				// Check if the service is already in the table
				serviceName = new String(bufName, 0, serviceLen);
				DnsServiceInfo dnsServiceInfo;
				if (contentDualTable.containsServiceKey(serviceName)) {
					dnsServiceInfo = contentDualTable.getByService(serviceName);
				} else {
					dnsServiceInfo = new DnsServiceInfo(serviceName);
					contentDualTable.putService(dnsServiceInfo);
				}

				clientsNumber = dis.read();

				//parsing all providing clients
				for(int j=0; j<clientsNumber; j++){

					if(dis.read(bufClient) != 4) return; // Try to extract the IP address ---Modified
					InetAddress clientInet = InetAddress.getByAddress(bufClient);

					clientIP = clientInet.getHostAddress();
					
					//if(clientInet.equals(grLayer.getP2pAddress()))
					//	continue;
					
					client = contentDualTable.getByClient(clientIP);

					if (client == null) { //if it is a unknown client, create it
						client = new WifiP2pClient(InetAddress.getByAddress(bufClient), null, null, true);
						contentDualTable.putClient(client);
					}

					boolean first = dnsServiceInfo.addProvidingClient(client);
					boolean second = client.addService(serviceName);
					if(first && second){
						
						tstamp = System.currentTimeMillis();
						logContent.append(String.valueOf(tstamp) + "\n");
						logContent.flush();

                        // If I am the elected client, forward the notification
						if(mActivity.isElectedClient())
							this.forwardNotification(serviceName, clientInet, packet.getAddress());
					}
				}
			}

			this.updateServicesNumber();

			//if(this.mActivity.isElectedClient()){
			//	this.sendAdvertiseBeaconAck(packet.getAddress());
			//}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}




	private void manageServiceNotificationMessage(DatagramPacket packet, DataInputStream dis){

		int seq, nserv, serviceLen;
		long tstamp;
		byte[] destAdd = new byte[4];
		byte[] provAdd = new byte[4];
		byte[] bufName = new byte[256];
		InetAddress destInetAdd, provInetAdd;
		boolean update = false;

		try {
			seq = dis.readInt();
			this.sendServiceNotificationMessageAck(packet.getAddress(), seq);
			
			dis.read(destAdd);
			destInetAdd = InetAddress.getByAddress(destAdd);
			if(!destInetAdd.equals(this.wiFiAddress) && !destInetAdd.equals(this.grLayer.getP2pAddress()))
				return;
			nserv = dis.read();
			for(int i = 0; i<nserv; i++){
				serviceLen = dis.read();
				dis.read(bufName, 0, serviceLen);
				dis.read(provAdd);
				provInetAdd = InetAddress.getByAddress(provAdd);
				String serviceName = new String(bufName,0 ,serviceLen);
				if(this.contentDualTable.putService(serviceName, provInetAdd)){
                    // If it is a new service
					update = true;
					this.forwardNotification(serviceName, provInetAdd, packet.getAddress());
					
					tstamp = System.currentTimeMillis();
					logContent.append(String.valueOf(tstamp) + "\n");
					logContent.flush();
					//this.addEntryToQueue(new String(bufName,0 ,serviceLen), provInetAdd, 
					//		InetAddress.getByName("192.168.49.1"));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		this.updateServicesNumber();
		if(update && mActivity.isGO())
			sendAdvertiseBeacon();
	}

	private void manageServiceNotificationMessageAck(DatagramPacket packet, DataInputStream dis){
		try {
			this.notificationQueue.ackReceived(dis.readInt());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	//###############################################################################
	// SEND METHODS
	//###############################################################################


	public void sendApplicationRequest(byte[] source,
			byte[] service, byte[] data) {

		if (source == null || service == null)
			throw new MissingFieldsException(
					"Source and service parameters of sendApplicationPacket function cannot be null!");

		int srclen, srvlen, datalen;

		srclen = source.length;
		srvlen = service.length;
		datalen = (data == null) ? 0 : data.length;
		int nonce = this.random.nextInt();

		String strService = new String(service);
		DnsServiceInfo dsinfo = contentDualTable.getByService(strService);

		if (dsinfo == null) {
			return;
		}

		InetAddress add = dsinfo.getNextHopIP();

		List<Pair<InetAddress, Integer>> list = this.pit.get(strService);
		if (list == null) {
			list = new ArrayList<Pair<InetAddress, Integer>>();
			list.add(new Pair<InetAddress, Integer>(null, Integer
					.valueOf(nonce)));
			this.pit.put(strService, list);
		} else {
			list.add(new Pair<InetAddress, Integer>(null, Integer
					.valueOf(nonce)));
		}
		
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(2 + srclen + 4 + 1 + srvlen + 1 + datalen);
		DataOutputStream dos = new DataOutputStream(baos);

		try {
			dos.write(ContentRoutingLayer.SERVICE_APPLICATION_REQUEST);
			dos.write(add.getAddress());
			dos.writeInt(nonce);
			dos.write(srvlen);
			dos.write(service);
			dos.write(datalen);
			if (datalen > 0)
				dos.write(data);

		} catch (IOException e) {
			e.printStackTrace();
		}

        /* --- Old version
		if((new String(service)).equals("file"))
			fileServer.setRequestTime();
		*/
        fileServer.setRequestTime();
		
		byte[] packetdata = baos.toByteArray();
		grLayer.forwardPacket(packetdata, add, null);
		try {
			dos.close();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendApplicationRequest(String source, String service, String data) {
		sendApplicationRequest(source.getBytes(), service==null? null : service.getBytes(), 
				data==null ? null : data.getBytes());
	}

	public void sendApplicationReply(byte[] source, int nonce,
			byte[] service, byte[] data) throws NoPitEntryException {

		if (source == null || service == null)
			throw new MissingFieldsException(
					"Source and service parameters of sendApplicationPacket function cannot be null!");

		int srclen, srvlen, datalen;

		srclen = source.length;
		srvlen = service.length;
		datalen = (data == null) ? 0 : data.length;
		
		String strService = new String(service);

		List<Pair<InetAddress, Integer>> list = this.pit.get(strService);
		if (list == null) {
			throw new NoPitEntryException("Pit entry not found");
		}		

		ByteArrayOutputStream baos = new ByteArrayOutputStream(2 + srclen + 4 + 1 + srvlen + 1 + datalen);
		DataOutputStream dos = new DataOutputStream(baos);

		try {
			dos.write(ContentRoutingLayer.SERVICE_APPLICATION_REPLY);
			dos.writeInt(0);
			dos.writeInt(nonce);
			dos.write(srvlen);
			dos.write(service);
			dos.write(datalen);
			if (datalen > 0)
				dos.write(data);

		} catch (IOException e) {
			e.printStackTrace();
		}

		
		byte[] datapacket = baos.toByteArray();

		// send the packet to all the address of the list
		for (Pair<InetAddress, Integer> pair : list) {
			if (nonce == pair.second.intValue()) {
				System.arraycopy(pair.first.getAddress(), 0, datapacket, 1, 4);
				grLayer.forwardPacket(datapacket, pair.first, null);
				list.remove(pair);
				break;
			}
		}

		try {
			dos.close();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendApplicationReply(String source, int nonce,
			String service, String data) throws NoPitEntryException {
		sendApplicationReply(source.getBytes(), nonce,
				service.getBytes(), data.getBytes());
	}

	private void sendBackNotification(byte[] data, InetAddress dest,
			Boolean relayNeeded) {

		int srclen = data[1];
		int dstlen = data[2];

		ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
		DataOutputStream dos = new DataOutputStream(baos);

		try {
			dos.write(ContentRoutingLayer.NOT_FOUND_SERVICE);
			dos.write(data[2]);
			dos.write(data[1]);
			dos.write(data, 3, srclen);
			dos.write(data, 3 + srclen, dstlen);
			dos.write(data, 3 + srclen + dstlen, data.length - 3 - srclen
					- dstlen);
			byte[] newdata = baos.toByteArray();
			grLayer.forwardPacket(newdata, dest, null);
			dos.close();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// Send advertise notification sendanq
    // Actually here we are selecting the Elected Client!
    // Only sent by GO. In WiFiDirectBroadcastReceiver, only GO calls this method
	public void sendElectingClientNotification() {

        // If the Elected Client has not been selected
		if (this.electedClient == null) {

			// choose the elected client (for the moment choose the first)
			for (WifiP2pClient wpc : contentDualTable.getClients()) {
				if (wpc.getP2pIPAddress() != null && !wpc.getP2pIPAddress().getHostAddress().equals("192.168.49.1")) {
					this.electedClient = wpc;
					this.electedClientAddress = this.electedClient.getP2pIPAddress();
					break;
				}
			}

            // ---v2
            // If for some reason, the GO cannot find a proper client to be the elected client, return
            if(this.electedClient == null) return;

            /* Useless
			if (this.electedClient == null) // there are no eligible clients
			{
				//------ DYF ------ For degbug, should be removed later
				String notifIP = mActivity.getNotifIP();
				if(notifIP.length() <= 11)
					notifIP += "0";
				this.electedClient = new WifiP2pClient(notifIP);
				//return;
			}
			*/
		}

        // As long as we have an Elected Client, we send this message to it,
        // to ask it to broadcast the services
		ByteArrayOutputStream baos = new ByteArrayOutputStream(5);
		DataOutputStream dos = new DataOutputStream(baos);

		try {
			dos.write(ContentRoutingLayer.NEW_JOINING_CLIENT_NOTIFICATION);
			dos.write(this.electedClient.getP2pIPAddress().getAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}

		grLayer.forwardPacket(baos.toByteArray(), this.electedClient.getP2pIPAddress(), null);

		//this.notificationSent = true;
		this.notificationReceived = false;

		Timer toTimer = new Timer();
		TimerTask toTimerTask = new TimerTask() {
			@Override
			public void run() {
				if(!notificationReceived)
                    sendElectingClientNotification();
			}
		};

		toTimer.schedule(toTimerTask, 5000);

	}
	
	
//	public void sendAdvertiseNotification(String destIP) {
//
//		if (this.electedClient == null) {
//
//			// choose the elected client (for the moment choose the first)
//			for (WifiP2pClient wpc : contentDualTable.getClients()) {
//				if (wpc.getP2pIPAddress() != null) {
//					this.electedClient = wpc;
//					break;
//				}
//			}
//
//			if (this.electedClient == null) // there are no eligible clients
//				//------ DYF ------ For degbug, should be removed later
//				this.electedClient = new WifiP2pClient(destIP);
//			//return;
//		}
//
//		ByteArrayOutputStream baos = new ByteArrayOutputStream(5);
//		DataOutputStream dos = new DataOutputStream(baos);
//
//		try {
//			dos.write(ContentRoutingLayer.SERVICE_ADVERTISE_NOTIFICATION);
//			dos.write(this.electedClient.getP2pIPAddress().getAddress());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		grLayer.forwardPacket(baos.toByteArray(),
//				this.electedClient.getP2pIPAddress(), null);
//
//	}


	public void sendAdvertiseListRequest() {
		// Advertise all the services, in BROADCAST, only by the elected client
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			int replyServiceNumber = 0;
			// Type & no. services
			dos.write(SERVICE_ADVERTISE_LIST_REQUEST);
			baos.write(replyServiceNumber); // number of services
			for (DnsServiceInfo info : contentDualTable.serviceValues()) {
				if (info.containsSameGroup(this.contentDualTable)) {
					// Service name length & service name
					dos.write(info.getServiceName().length());
					dos.write(info.getServiceName().getBytes());
					replyServiceNumber++;
				}
			}
			for (String s : this.ownProvidedServices) {
				// Service name length & service name
				dos.write(s.length());
				dos.write(s.getBytes());
			}

			replyServiceNumber+=ownProvidedServices.size();
			byte[] payload = baos.toByteArray();
			payload[1] = (byte) replyServiceNumber;
			grLayer.forwardPacket(payload,
					InetAddress.getByName("192.168.49.255"), null);

			//this.listSent = true;
			this.listReceived = false;

			Timer toTimer = new Timer();
			TimerTask toTimerTask = new TimerTask() {
				@Override
				public void run() {
					if(!listReceived)
						sendAdvertiseListRequest();
				}
			};

			toTimer.schedule(toTimerTask, 5000);

		} catch (IOException ex) {

		}
	}

	private void sendAdvertiseReply(InetAddress dest) {
		// Send advertisement reply
		int replyServiceNumber = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(SERVICE_ADVERTISE_LIST_REPLY); // packet type
		baos.write(replyServiceNumber); // number of services
		for (DnsServiceInfo info : contentDualTable.serviceValues()) {
			// If this service can be provided by at least one client in the
			// same group
			if (info.containsSameGroup(this.contentDualTable)) {
				try {
					// baos.write(grLayer.getP2pAddress().getAddress());
					baos.write(info.getServiceName().length());
					baos.write(info.getServiceName().getBytes());
					replyServiceNumber++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		for (String s : this.ownProvidedServices) {
			// Service name length & service name
			baos.write(s.length());
			try {
				baos.write(s.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		replyServiceNumber+=ownProvidedServices.size();
		byte[] payload = baos.toByteArray();
		payload[1] = (byte) replyServiceNumber;
		grLayer.forwardPacket(payload, dest, null);
	}

    // This method is never used
	public void sendDownNotification(WifiP2pClient client) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		try {
			dos.write(client.getP2pIPAddress().getAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}




	public void sendAdvertiseBeacon(){
		
		if(!mActivity.isGO())
			return;

		int replyServiceNumber = this.contentDualTable.numberOfServices();
		if(replyServiceNumber==0)
			return;

		// Advertise the service
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			// Type & no. services
			dos.write(SERVICE_ADVERTISE_BEACON);
			baos.write(replyServiceNumber); // number of services
			for (DnsServiceInfo serv : this.contentDualTable.getServices()) {

				dos.write(serv.getServiceName().length());
				dos.write(serv.getServiceName().getBytes());
				dos.write(serv.getProvidingClients().size());
				// Service name length & service name
				for(String pc : serv.getProvidingClients()){
					if(this.externalElectedClientAddress!=null)
						if(mActivity.isLegacyClient() && pc.equals(this.externalElectedClientAddress.getHostAddress()))
							pc = "192.168.49.1";
					dos.write(InetAddress.getByName(pc).getAddress());
				}
			}

			byte[] payload = baos.toByteArray();
			//payload[1] = (byte)replyServiceNumber;
			grLayer.forwardPacket(payload, InetAddress.getByName("192.168.49.255"), null);

		}catch (IOException e) {
			e.printStackTrace();
		}
	}




	public void sendServiceNotificationMessage(byte[] data, InetAddress dest){
		grLayer.forwardPacket(data, dest, null);
	}

	public void sendServiceNotificationMessageAck(InetAddress dest, int seq){

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		try {
			dos.write(SERVICE_NOTIFICATION_MESSAGE_ACK);
			dos.writeInt(seq);
		} catch (IOException e) {
			e.printStackTrace();
		}

		grLayer.forwardPacket(baos.toByteArray(), dest, null);
	}





	//###############################################################################
	// INNER CLASSES AND INTERFACES
	//###############################################################################


	public interface ContentRoutingInterface {
		public void onPacketArrived(byte type, int nonce, byte[] service,
				byte[] data);
	}

	public class NoPitEntryException extends Exception {

		private static final long serialVersionUID = 1L;

		public NoPitEntryException(String message) {
			super(message);
		}
	}

	private class MissingFieldsException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public MissingFieldsException(String message) {
			super(message);
		}
	}


	//###############################################################################
	// OTHER METHODS
	//###############################################################################


	private void forwardNotification(String service, InetAddress provider, InetAddress source){
		if(mActivity.isElectedClient()){
            // If I am the elected client and the notification if from the GO, forward it to
            // all the legacy clients
			if(source.getHostAddress().equals("192.168.49.1")){
				InetAddress ownAdd = grLayer.getP2pAddress();
				for(InetAddress legAdd : this.legacyClientAddresses){
					this.addEntryToQueue(service, ownAdd, legAdd);
				}
			}
			else if(this.legacyClientAddresses.contains(source)){
                // If I am the elected client and I received it from a legacy client,
                // forward it to the GO and the other legacy clients
				InetAddress ownAdd = grLayer.getP2pAddress();
				try {
					this.addEntryToQueue(service, ownAdd, InetAddress.getByName("192.168.49.1"));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				for(InetAddress legAdd : this.legacyClientAddresses){
					if(!legAdd.equals(source))
						this.addEntryToQueue(service, ownAdd, legAdd);
				}
				
			}
		}
		else if(mActivity.isGO() && mActivity.isLegacyClient()){
			InetAddress ownAdd = grLayer.getP2pAddress();
			
			if(this.externalElectedClientAddress==null)
				return;
			
			if(!this.externalElectedClientAddress.equals(source)) {
                // If I am a legacy client and GO, and the notification is NOT from the external
                // elected client, send it to the external elected client
				this.addEntryToQueue(service, this.wiFiAddress, this.externalElectedClientAddress);
			}
			else{
                // If I am a legacy client and GO, and the notification IS from the external
                // elected client, send it to the elected client
				if(this.electedClientAddress!=null){
					this.addEntryToQueue(service, ownAdd, this.electedClientAddress);
				}
			}
		}
		else if(mActivity.isGO()){
            // If I am a normal GO, and the notification is not from the elected client,
            // send it to the elected client
			if(this.electedClientAddress!=null && !this.electedClientAddress.equals(source))
				this.addEntryToQueue(service, provider, this.electedClientAddress);
		}
	}
	
	
	private void addEntryToQueue(String service, InetAddress provider, InetAddress dest){
		NotificationEntry ne = new NotificationEntry(service, provider, dest);
		this.notificationQueue.add(ne);
	}
	
	
	
	
	
	public void addOwnService(String s){

		InetAddress ownAdd = this.grLayer.getP2pAddress();
		
		synchronized (this.ownProvidedServices) {
			if(this.ownProvidedServices.contains(s))
				return;

			this.ownProvidedServices.add(s);
			this.contentDualTable.putService(s, ownAdd!=null ? ownAdd : wiFiAddress);
		}
		

		if(mActivity.isElectedClient()){
			
			for(InetAddress legAdd : this.legacyClientAddresses)
					this.addEntryToQueue(s, ownAdd, legAdd); // Provider is me, the destination is the legacy client
			
			try {
				this.notificationQueue.add(new NotificationEntry(s, ownAdd, 
						InetAddress.getByName("192.168.49.1"))); // Provider is me, the destination is the GO
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		
		else if(mActivity.isGO() && mActivity.isLegacyClient()){
			if(this.electedClientAddress!=null)
				this.addEntryToQueue(s, ownAdd, this.electedClientAddress);
			
			if(this.externalElectedClientAddress!=null)
				this.addEntryToQueue(s, this.wiFiAddress, this.externalElectedClientAddress);
			//TODO the address must be the wifi one
			
			sendAdvertiseBeacon();
			
		}
		
		else if(mActivity.isGO()){
			sendAdvertiseBeacon();
			
			if(this.electedClientAddress!=null)
				this.addEntryToQueue(s, ownAdd, this.electedClientAddress);
			
		}
		
		else if(mActivity.isLegacyClient()){
			if(this.externalElectedClientAddress!=null)
				this.addEntryToQueue(s, this.wiFiAddress, this.externalElectedClientAddress);
		}
		
		else{ // If I am a simple P2P Client
			try {
				this.notificationQueue.add(new NotificationEntry(s, grLayer.getP2pAddress(), 
						InetAddress.getByName("192.168.49.1")));
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
        // Update services number on UI
		updateServicesNumber();
	}



	private void updateServicesNumber(){
		Handler handler = mActivity.getNumberServicesTextHandler();
		Bundle data = new Bundle(1);
		data.putInt(MainActivity.NUMBER_DISCOVERED_SERVICES, contentDualTable.numberOfServices());
		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}

    private void updateStatusTextView(String text, boolean toast){
        Handler handler = mActivity.getStatusTextHandler();
        Bundle data = new Bundle(1);
        data.putString(MainActivity.STATUS_MESSAGE, text);
        data.putBoolean(MainActivity.STATUS_IF_TOAST, toast);
        Message msg = handler.obtainMessage();
        msg.setData(data);
        handler.sendMessage(msg);
    }
	
	
	
	public void startNotificationQueue(){
		this.notificationQueue.start();
	}
	
	public void resetFileServer(){
		this.fileServer.closeFile();
	}



	//###############################################################################
	// GET & SET METHODS
	//###############################################################################

	public InetAddress getWiFiAddress(){
		return wiFiAddress;
	}

	public void setWiFiAddress(InetAddress add){
		this.wiFiAddress = add;
	}
	
	public void setGroupRoutingLayer(GroupRoutingLayer grl) {
		this.grLayer = grl;
	}

	public ContentRoutingDualTable getContentRoutingDualTable() {
		return this.contentDualTable;
	}

	public InetAddress getElectedClientAddress() {
		return electedClientAddress;
	}
	
	public InetAddress getexternalElectedClientAddress() {
		return externalElectedClientAddress;
	}

	public List<InetAddress> getLegacyClientAddresses() {
		return legacyClientAddresses;
	}

	public void setGroupFormed(boolean b) {
		if(this.groupFormed==false && b==true) {
			this.notificationQueue.start();
		}
		else if(this.groupFormed==true && b==false){
			this.notificationQueue.stop();
		}
		this.groupFormed=b;
	}

	public boolean isGroupFormed(){
		return groupFormed;
	}

}
