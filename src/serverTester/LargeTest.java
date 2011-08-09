package serverTester;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JOptionPane;

public class LargeTest {
	
	private GraphServerTester tester;
	
	private List<String> gnutellaIDs;
	private Hashtable<String, DatagramSocket> peerSocket;
	
	private Hashtable<String, String> onlineMessages;
	
	private Hashtable<String, String> connectMessages;
	
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
		connectMessages = new Hashtable<String, String>();
		
		generateOnlineMessages();
		generateConnectMessages();
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
		for(int i=0;i<gnutellaIDs.size()-2;i++) {
			String time = Long.toString(System.currentTimeMillis());
			connectMessages.put(gnutellaIDs.get(i), time+"     "+gnutellaIDs.get(i)+"     CONNECT     "+gnutellaIDs.get(i+1)+"\n");
		}
	}
	
	public void sendOnlineMessages() {
		try { 
			for(String peer : peerSocket.keySet()) {
				tester.sendMessage(peerSocket.get(peer), onlineMessages.get(peer));
				Thread.sleep(100); //sleep .1 second between sending again
				peerSocket.get(peer).close();
			}
		} catch (InterruptedException ignored) {
			//e.printStackTrace();
		}
	}
	
	public void sendConnectMessages() {
		/*int i=0;
		for(String message : connectMessages) {
			try { 
				tester.sendMessage(new DatagramSocket(initialPort+i), message);
				Thread.sleep(100); //sleep .1 second between sending again
			} catch(SocketException se) {
				System.out.print("Not Sent: "+message);
				System.out.println("\t"+se.getMessage());
			} catch (InterruptedException ignored) {
				//e.printStackTrace();
			}
			i++;
		}*/
	}
	
}
