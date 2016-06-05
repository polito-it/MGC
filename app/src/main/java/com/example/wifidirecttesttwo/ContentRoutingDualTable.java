
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ContentRoutingDualTable {

	private Map<String, DnsServiceInfo> serviceIndexedTable;
	private Map<String, WifiP2pClient> clientIndexedTable;
	
	public ContentRoutingDualTable() {
		this.serviceIndexedTable = new ConcurrentHashMap<String, DnsServiceInfo>();
		this.clientIndexedTable = new ConcurrentHashMap<String, WifiP2pClient>();
	}
	
	public class ServicesSet {
        private ArrayList<String> names;
        private ArrayList<String> ips;

        public ServicesSet(int servicesNumber){
            this.names = new ArrayList<String>(servicesNumber);
            this.ips = new ArrayList<String>(servicesNumber);
        }

        public ArrayList<String> getNames() {
            return this.names;
        }

        public ArrayList<String> getIps(){
            return this.ips;
        }
    }
	
	// By SERVICE methods
	public DnsServiceInfo getByService(String serviceName){
		return this.serviceIndexedTable.get(serviceName);
	}
	
	public void putService(DnsServiceInfo dsi){
		serviceIndexedTable.put(dsi.getServiceName(), dsi);
	}
	
	public boolean containsServiceKey(String serv)
	{
		return serviceIndexedTable.containsKey(serv);
	}
	
	public boolean putService(String serviceName, InetAddress ip){
		if (ip==null)
			return false;
		if(serviceName==null)
			return false;
		DnsServiceInfo dsi = this.serviceIndexedTable.get(serviceName);
		String stemp = ip.getHostAddress();
		WifiP2pClient ctemp = this.clientIndexedTable.get(stemp);
			
		if(dsi==null){
			dsi = new DnsServiceInfo(serviceName, ip);
			serviceIndexedTable.put(serviceName, dsi);
		}
		
		if(ctemp==null){
			ctemp = new WifiP2pClient(ip, null, null, false);
			clientIndexedTable.put(stemp, ctemp);
		}
			
		if(ctemp.addService(serviceName) && dsi.addProvidingClient(ctemp))
			return true;
		else
			return false;
	}
	
	public int numberOfServices(){
		return serviceIndexedTable.size();
	}
	
	public Collection<DnsServiceInfo> serviceValues(){
		return serviceIndexedTable.values();
	}
	
	
	
	// By CLIENT methods
	public WifiP2pClient getByClient(String clientP2pIP){
		return this.clientIndexedTable.get(clientP2pIP);
	}
	
	public boolean containsClientKey(String key){
		return clientIndexedTable.containsKey(key);
	}
	
	public int numberOfClients(){
		return clientIndexedTable.size();
	}
	
	public Collection<WifiP2pClient> getClients(){
		return clientIndexedTable.values();
	}
	
	public Collection<DnsServiceInfo> getServices(){
		return serviceIndexedTable.values();
	}
	
	public void putClient(WifiP2pClient wpc){
		clientIndexedTable.put(wpc.getP2pIPAddress().getHostAddress(), wpc);
	}
	
	public void removeClient(WifiP2pClient wpc){
		if(wpc == null)
			return; 
		
		for(String name : wpc.getProvidedServices()){
			DnsServiceInfo service = this.serviceIndexedTable.get(name);
			if (service==null)
				continue;
			service.removeProvidingClient(wpc);
			if(!service.isProvided())
				this.serviceIndexedTable.remove(service.getServiceName());
		}
		clientIndexedTable.remove(wpc.getP2pIPAddress().getHostAddress());
	}
	
	public void removeClient(byte[] ipadd){
		 try {
			InetAddress iadd = InetAddress.getByAddress(ipadd);
			clientIndexedTable.remove(iadd.getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

    public ServicesSet getServicesNames(){
        ServicesSet set = new ServicesSet(this.serviceIndexedTable.size());
        Iterator iterator = this.serviceIndexedTable.entrySet().iterator();
        Map.Entry<String, DnsServiceInfo> entry;
        while(iterator.hasNext()){
            entry = (Map.Entry<String, DnsServiceInfo>) iterator.next();
            set.names.add(entry.getKey());
            set.ips.add(entry.getValue().getNextHopIP().getHostAddress());
        }
        return set;
    }
}
