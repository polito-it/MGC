
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DnsServiceInfo {

	private String nextHopMac;
	private InetAddress nextHopIP;
	private String serviceName;
	private List<String> providingClients;

	public DnsServiceInfo(String serviceName, InetAddress add) {
		this.nextHopIP = add;
		this.nextHopMac = null;
		this.serviceName = serviceName;
		this.providingClients = Collections.synchronizedList(new ArrayList<String>());
	}
	
	public DnsServiceInfo(String serviceName) {
		this.nextHopIP = null;
		this.nextHopMac = null;
		this.serviceName = serviceName;
		this.providingClients = Collections.synchronizedList(new ArrayList<String>());
	}

	public InetAddress getNextHopIP() {
		return this.nextHopIP;
	}

	public String getNextHopMac() {
		return this.nextHopMac;
	}

	public String getServiceName() {
		return this.serviceName;
	}
	
	public List<String> getProvidingClients()
	{
		return this.providingClients;
	}
	
	public boolean addProvidingClient(WifiP2pClient wpc){
		if(this.nextHopIP == null){
			this.nextHopIP = wpc.getP2pIPAddress();
			this.nextHopMac = wpc.getP2pMacAddress();
		}
		return addProvidingClient(wpc.getP2pIPAddress().getHostAddress());
	}
	
	public boolean addProvidingClient(String ip){
		if(!this.providingClients.contains(ip)){
			this.providingClients.add(ip);
			return true;
		}
		return false;
	}
	
	public void removeProvidingClient(WifiP2pClient wpc){
		providingClients.remove(wpc.getP2pIPAddress().getHostAddress());
	}
	public void removeProvidingClient(String ip){
		providingClients.remove(ip);
	}
	
	public boolean isProvided(){
		return (providingClients.size() > 0);
	}
	
	public boolean containsSameGroup(ContentRoutingDualTable table)
	{
		for(String ip : this.providingClients)
		{
			if(table.getByClient(ip).isSameGroup())
				return true;
		}
		return false;
	}
	
	public boolean containsOtherGroup(ContentRoutingDualTable table)
	{
		for(String ip : this.providingClients)
		{
			if(!table.getByClient(ip).isSameGroup())
				return true;
		}
		return false;
	}

}