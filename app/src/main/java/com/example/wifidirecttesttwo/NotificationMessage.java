
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class NotificationMessage {

	List<NotificationEntry> entries;
	int sequenceNumber;
	InetAddress destAddress;

	public NotificationMessage(int n){
		this.entries = new ArrayList<NotificationEntry>();
		this.sequenceNumber = n;
		this.destAddress = null;
	}
	

	public boolean add(NotificationEntry entry){
		
		if (this.destAddress==null)
			this.destAddress = entry.getDestAddress();
		else
			if(!this.destAddress.equals(entry.getDestAddress()))
				//TODO throw some kind of exception
				return false;
		//if the destination address is not null and is compatible, add the new entry to the list
		entries.add(entry);
		return true;
	}

	
	
	
	public byte[] toByteArray(){ //returns a byte array containing the packet to be sent
		byte[] data;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(ContentRoutingLayer.SERVICE_NOTIFICATION_MESSAGE); //packet type
			dos.writeInt(this.sequenceNumber);
			dos.write(this.destAddress.getAddress()); //destination address
			dos.write(this.entries.size()); // number of services
			
			for(NotificationEntry entry : entries){ //add notification entries
				dos.write(entry.getServiceName().length());
				dos.write(entry.getServiceName().getBytes());
				dos.write(entry.getProviderAddress().getAddress());
			}
			
			data = baos.toByteArray();
			return data;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public int getSize(){
		return this.entries.size();
	}
	
	public InetAddress getDestAddress(){
		return this.destAddress;
	}
	
	public int getSequenceNumber(){
		return this.sequenceNumber;
	}
	
}
