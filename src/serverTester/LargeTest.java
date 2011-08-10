package serverTester;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

public class LargeTest {
	
	private GraphServerTester tester;
	
	private List<String> gnutellaIDs;
	private Hashtable<String, DatagramSocket> peerSocket;
	
	private Hashtable<String, String> onlineMessages;
	
	private Hashtable<String, String> connectMessages;
	
	private Hashtable<String, String> publishMessages;
	
	public LargeTest(int numberPeers, int initialPort, GraphServerTester tester) {
		this.tester = tester;
		
		gnutellaIDs = new ArrayList<String>(numberPeers);
		peerSocket = new Hashtable<String, DatagramSocket>(numberPeers);
		
		Object[] ids= GraphServerTester.generateGnutellaIDs(numberPeers);
		for(int i=0;i<ids.length;i++) {
			if(ids[i].getClass().equals(String.class)) { //don't remember why this is needed
				try {
					String peer = (String) ids[i];
					gnutellaIDs.add(peer);
					peerSocket.put(peer, new DatagramSocket(initialPort+i));
				} catch (SocketException e) {
					System.out.println("Could not bind port: "+initialPort+i);
					//e.printStackTrace();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, ex.getCause(), "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
		//Arrays.asList(ids)); //set initial capacity and makes a list from the generated ids
		
		onlineMessages = new Hashtable<String, String>(numberPeers);
		connectMessages = new Hashtable<String, String>(numberPeers);
		publishMessages = new Hashtable<String, String>(numberPeers);
		
		generateOnlineMessages();
		generateConnectMessages();
		generatePublishMessages();
	}
	
	private void generateOnlineMessages() {
		int i=0;
		for(String id : gnutellaIDs) {
			String time = Long.toString(System.currentTimeMillis());
			String prefix = "134.117.60.25:635"+i+"[up2p_"+i+"]";
			
			onlineMessages.put(id, time+"     "+id+"     ONLINE     "+prefix+"\n");
			
			i++;
		}
	}
	
	private void generateConnectMessages() {
		Random rand = new Random(System.currentTimeMillis());
		int i=0;
		for(String id : gnutellaIDs) {
			String time = Long.toString(System.currentTimeMillis());
			
			String otherPeer;
			
			do {
				otherPeer = gnutellaIDs.get(rand.nextInt(gnutellaIDs.size()-1));
			}while(otherPeer.equals(id)); //don't connect to themself
			
			connectMessages.put(id, time+"     "+id+"     CONNECT     "+otherPeer+"\n");
			
			i++;
		}
	}
	
	private void generatePublishMessages() {
		Random rand = new Random(System.currentTimeMillis());
		int i=0;
		for(String id : gnutellaIDs) {
			String time = Long.toString(System.currentTimeMillis());
			
			int communityID;
			int documentID;
			
			communityID = rand.nextInt(10)+500;
			documentID = rand.nextInt(10)+800;
			
			publishMessages.put(id, time+"     "+id+"     PUBLISH     "+Integer.toHexString(communityID)+
					"     "+Integer.toHexString(documentID)+"\n");
			
			i++;
		}
	}
	
	private void generateLinkDocumentMessages() {
		Random rand = new Random(System.currentTimeMillis());
		int i=0;
		for(String id : gnutellaIDs) {
			String time = Long.toString(System.currentTimeMillis());
			
			int communityID;
			int documentID;
			
			communityID = rand.nextInt(10)+500;
			documentID = rand.nextInt(10)+800;
			
			publishMessages.put(id, time+"     "+id+"     PUBLISH     "+Integer.toHexString(communityID)+
					"     "+Integer.toHexString(documentID)+"\n");
			
			i++;
		}
	}
	
	public void sendOnlineMessages() {
		try { 
			for(String peer : peerSocket.keySet()) {
				tester.sendMessage(peerSocket.get(peer), onlineMessages.get(peer));
				Thread.sleep(100); //sleep .1 second between sending again
			}
		} catch (InterruptedException ignored) {
			//e.printStackTrace();
		}
	}
	
	public void sendConnectMessages() {
		try { 
			for(String peer : peerSocket.keySet()) {
				tester.sendMessage(peerSocket.get(peer), connectMessages.get(peer));
				Thread.sleep(100); //sleep .1 second between sending again
			}
		} catch (InterruptedException ignored) {
			//e.printStackTrace();
		}
	}
	
	public void sendPublishMessages() {
		try { 
			for(String peer : peerSocket.keySet()) {
				tester.sendMessage(peerSocket.get(peer), publishMessages.get(peer));
				Thread.sleep(100); //sleep .1 second between sending again
			}
		} catch (InterruptedException ignored) {
			//e.printStackTrace();
		}
	}
	
	/**
	 * close all sockets for later use
	 */
	public void end() {
		for(String peer : peerSocket.keySet()) {
			peerSocket.get(peer).close();
		}
	}
	
}
