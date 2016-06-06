# Multi-Group Content (MGC)

## Introduction
This an Android application that enables content delivery on a multi-group communication network based on WiFi-Direct. Rooting the device is NOT required.

The protocols and mechanisms behind this application are fully described in our paper:
> Claudio Casetti, Carla Fabiana Chiasserini, Luciano Curto Pelle, Carolina Del Valle, Yufeng Duan, Paolo Giaccone, *Content-centric Routing in Wi-Fi Direct Multi-group Networks*, IEEE WoWMoM, Boston, USA, 14 June 2015 

MGC enables users content transfer (e.g., images, videos and texts) and network services provisioning (e.g., remote printing, HTTP-socks proxy) in a multi-group Wi-Fi Direct network. Once a network using Wi-Fi Direct technology is established, the following threes phases can be implemented through MGC :

- **Content  publishing** The user publishes the content items it wishes to share. Such items are advertised to all groups with the help of Relay Clients and GOs. A summary information (holder, title, etc.) of registered content is then disseminated throughout the network. Each device lists all the items available locally and
remotely.
- **Content  request** The user requests a content. If the content is remotely available, the request is forwarded to the content provider, with the cooperation of the Relay Clients and of the GOs.

- **Content delivery** Once a content provider receives a request, it transfers the content toward the requester. The nodes on the forward path traveled by the request will cooperate to transfer the content in the reverse direction and eventually deliver it to the requester node.

## Usage
### Pre-requisites
- Android API level >= 19 or Android Version >= 4.2

### Install on Android
- Download the entire code into a local directory
- Using Eclipse or Android Studio to import the code
- Run the code on an Android device

*Certain SDK will be required to compile this code.*

## License
Copyright [2015] [Telecommunication Network Group, Politecnico di Torino]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Acknowledgement
This code is developed by [Telecommunication Network Group](http://www.telematica.polito.it/) of [Politecnico di Torino](http://www.polito.it/).
