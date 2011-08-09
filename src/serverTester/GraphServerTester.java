package serverTester;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.DefaultComboBoxModel;

public class GraphServerTester extends JApplet {
	private static final long serialVersionUID = -8352887880930715549L; //eclipse generated serialUID

	public static final String[] MESSAGE_TYPES = {	"ONLINE", "OFFLINE", 
													"CONNECT", "DISCONNECT", 
													"PUBLISH", "REMOVE", 
													"LINKDOCUMENT", "DELINKDOCUMENT",
													"QUERY", "QUERYHIT", 
													"QUERY_REACHES_PEER" };

	public static final int UPDATE_SPEED = 1; //time in milliseconds between the visible time updates.
	public static final int STARTING_UDP_PORT = 60200;
	public static final int NUMBER_OF_PEERS = 10;
	private int currentUDPPort = STARTING_UDP_PORT;

	private Timer timeUpdater;
	private JLabel currentTime;

	private JLabel UDPPort;
	private JTextField serverINetAddress;
	private JTextField serverPort;
	
	private ArrayList<DatagramSocket> clientSockets;

	//[start] global info
	private JComboBox gnutellaID;
	private int gnutellaIndex;
	private JComboBox messageType;
	private JSplitPane messageInfoPanel;
	private JTabbedPane messageInfoTabs;
	private JButton sendButton;
	private JButton simulateButton;
	//[end] global info
	
	//[start] Online/Offline info
	private JLabel up2pPrefix;
	//[end] Online/Offline info
	
	//[start] Connect/Disconnect info
	private JComboBox otherGnutellaID;
	//[end] Connect/Disconnect info
		
	//[start] publish/remove info
	private JTextField documentCommunity;
	private JTextField documentID;
	//[end] publish/remove info
	
	//[start] link/delink info
	private JTextField linkDocumentOne;
	private JTextField linkDocumentTwo;
	//[end] link/delink info
	
	//[start] query info
	private JTextField queryID;
	private JTextField community;
	private JTextField queryString;
	//[end] query info
	
	//[start] query hit info
	private JTextField queryIDHasHit;
	private JTextField docCommunity;
	private JTextField docID;
	private JTextField docName;
	//[end] query hit info
	
	//[start] query reaches peer info
	private JTextField queryIDReachesPeer;
	//[end] query reaches peer info
	
	private Object[] gnutellaIDs;
	

	/**
	 * Create the applet.
	 */
	public GraphServerTester() {
		getContentPane().setSize(800, 600);
		getContentPane().setLayout(new BorderLayout(0, 0));

		gnutellaIDs = generateGnutellaIDs(NUMBER_OF_PEERS);
		try {
			clientSockets = new ArrayList<DatagramSocket>();
			for(int i=0;i<NUMBER_OF_PEERS;i++) {
				clientSockets.add(new DatagramSocket(STARTING_UDP_PORT+i));
			}
			
		} catch(SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

		getContentPane().add(getNetworkingPanel(), BorderLayout.NORTH);
		getContentPane().add(getMessageInfoPanel(), BorderLayout.CENTER);
		getContentPane().add(getSendPanel(), BorderLayout.SOUTH);

		timeUpdater = new Timer(UPDATE_SPEED,new TimeLabelUpdater());
		timeUpdater.start();

		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception ignored) {}//UI didn't change to nimbus.. no big deal
		
		this.validate();

	}

	/**
	 * helper for making the left panel with the gnutella ids and the message types
	 * @return
	 */
	private JSplitPane getMessageInfoPanel() {

		gnutellaID = new JComboBox(gnutellaIDs);
		gnutellaID.addActionListener(new GnutellaIDListener());
		gnutellaID.setToolTipText("Guntella ID");
		gnutellaID.setBorder(BorderFactory.createTitledBorder("Gnutella ID"));
		GridBagConstraints gnutellaIDConstraints = new GridBagConstraints();
		gnutellaIDConstraints.gridwidth = GridBagConstraints.REMAINDER;
		
		messageType = new JComboBox(MESSAGE_TYPES);
		messageType.setModel(new DefaultComboBoxModel(MESSAGE_TYPES));
		messageType.addActionListener(new MessageTypeListener());
		messageType.setToolTipText("Message Type");
		messageType.setBorder(BorderFactory.createTitledBorder("Message Type"));
		GridBagConstraints messageTypeConstraints = new GridBagConstraints();
		gnutellaIDConstraints.gridwidth = GridBagConstraints.REMAINDER;
		
		JPanel genericMessageInfoPanel = new JPanel();
		genericMessageInfoPanel.setLayout(new GridBagLayout());
		genericMessageInfoPanel.add(gnutellaID, gnutellaIDConstraints);
		genericMessageInfoPanel.add(messageType, messageTypeConstraints);
		
		messageInfoPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, genericMessageInfoPanel,getMessageInfoTabs());
		messageInfoPanel.setResizeWeight(0.5);
		messageInfoPanel.setOneTouchExpandable(true);
		
		messageInfoPanel.setBorder(BorderFactory.createTitledBorder("Message Information"));
		messageInfoPanel.setDividerLocation(0.5);

		return messageInfoPanel;
	}

	/**
	 * helper for making the online offline message information panel
	 * @return
	 */
	private JPanel getOnlineOfflineMessageInfoPanel() {
		
		up2pPrefix = new JLabel("134.117.60.25:635"+gnutellaIndex+"[up2p_"+gnutellaIndex+"]");
		up2pPrefix.setBorder(BorderFactory.createTitledBorder("UP2P Information"));
		up2pPrefix.setBackground(getContentPane().getBackground());

		JPanel onlineOfflineMessageInfoPanel = new JPanel();
		onlineOfflineMessageInfoPanel.setLayout(new BorderLayout());
		onlineOfflineMessageInfoPanel.add(up2pPrefix, BorderLayout.NORTH);


		return onlineOfflineMessageInfoPanel;
	}

	/**
	 * helper for making the connect and disconnect message information panel
	 * @return
	 */
	private JPanel getConnectDisconnectMessageInfoPanel() {
		
		otherGnutellaID = new JComboBox(gnutellaIDs);
		//otherGnutellaID.addActionListener(new GnutellaIDListener());
		otherGnutellaID.setToolTipText("Other Peer Guntella ID");
		otherGnutellaID.setBorder(BorderFactory.createTitledBorder("Other Peer Gnutella ID"));
		
		JPanel connectDisconnectMessageInfoPanel = new JPanel();
		connectDisconnectMessageInfoPanel.setLayout(new BorderLayout());
		connectDisconnectMessageInfoPanel.add(otherGnutellaID, BorderLayout.NORTH);

		return connectDisconnectMessageInfoPanel;
	}

	/**
	 * helper for making the connect and disconnect message information panel
	 * @return
	 */
	private JPanel getPublishRemoveMessageInfoPanel() {
		
		documentCommunity = new JTextField("root");
		documentCommunity.setBorder(BorderFactory.createTitledBorder("Document's Community"));
		documentCommunity.setBackground(getContentPane().getBackground());
		
		documentID = new JTextField("p2pdia");
		documentID.setBorder(BorderFactory.createTitledBorder("Document's Identifier"));
		documentID.setBackground(getContentPane().getBackground());
		
		JPanel publishRemoveMessageInfoPanel = new JPanel();
		publishRemoveMessageInfoPanel.setLayout(new BorderLayout());
		publishRemoveMessageInfoPanel.add(documentCommunity,BorderLayout.NORTH);
		publishRemoveMessageInfoPanel.add(documentID,BorderLayout.SOUTH);
		
		return publishRemoveMessageInfoPanel;
	}

	/**
	 * helper for making the connect and disconnect message information panel
	 * @return
	 */
	private JPanel getLinkDelinkDocumentMessageInfoPanel() {
		
		linkDocumentOne = new JTextField();
		linkDocumentOne.setBorder(BorderFactory.createTitledBorder("First Document Identifier"));
		linkDocumentOne.setBackground(getContentPane().getBackground());
		
		linkDocumentTwo = new JTextField();
		linkDocumentTwo.setBorder(BorderFactory.createTitledBorder("Second Document Identifier"));
		linkDocumentTwo.setBackground(getContentPane().getBackground());
		
		JPanel linkDelinkDocumentMessageInfoPanel = new JPanel();
		linkDelinkDocumentMessageInfoPanel.setLayout(new BorderLayout());
		linkDelinkDocumentMessageInfoPanel.add(linkDocumentOne, BorderLayout.NORTH);
		linkDelinkDocumentMessageInfoPanel.add(linkDocumentTwo, BorderLayout.SOUTH);
		
		return linkDelinkDocumentMessageInfoPanel;
	}

	/**
	 * helper for making the connect and disconnect message information panel
	 * @return
	 */
	private JPanel getQueryMessageInfoPanel() {
		
		queryID = new JTextField("Query1");
		queryID.setBorder(BorderFactory.createTitledBorder("Query identifier"));
		queryID.setBackground(getContentPane().getBackground());
		
		community = new JTextField("root");
		community.setBorder(BorderFactory.createTitledBorder("Community to query on"));
		community.setBackground(getContentPane().getBackground());
		
		queryString = new JTextField("");
		queryString.setBorder(BorderFactory.createTitledBorder("The String to query"));
		queryString.setBackground(getContentPane().getBackground());
		
		JPanel queryMessageInfoPanel = new JPanel();
		queryMessageInfoPanel.setLayout(new GridLayout(3,1));
		queryMessageInfoPanel.add(queryID);
		queryMessageInfoPanel.add(community);
		queryMessageInfoPanel.add(queryString);

		return queryMessageInfoPanel;
	}

	/**
	 * helper for making the connect and disconnect message information panel
	 * @return
	 */
	private JPanel getQueryHitMessageInfoPanel() {
		
		queryIDHasHit = new JTextField("Query1");
		queryIDHasHit.setBorder(BorderFactory.createTitledBorder("Query ID which has a hit"));
		queryIDHasHit.setBackground(getContentPane().getBackground());
		
		docCommunity = new JTextField("root");
		docCommunity.setBorder(BorderFactory.createTitledBorder("The document's community"));
		docCommunity.setBackground(getContentPane().getBackground());
		
		docID = new JTextField("p2pdia");
		docID.setBorder(BorderFactory.createTitledBorder("The document's Identifier"));
		docID.setBackground(getContentPane().getBackground());
		
		docName = new JTextField("UP2Pdia");
		docName.setBorder(BorderFactory.createTitledBorder("The document's name"));
		docName.setBackground(getContentPane().getBackground());
		
		JPanel queryHitMessageInfoPanel = new JPanel();
		queryHitMessageInfoPanel.setLayout(new GridLayout(4,1));
		queryHitMessageInfoPanel.add(queryIDHasHit);
		queryHitMessageInfoPanel.add(docCommunity);
		queryHitMessageInfoPanel.add(docID);
		queryHitMessageInfoPanel.add(docName);

		return queryHitMessageInfoPanel;
	}

	/**
	 * helper for making the connect and disconnect message information panel
	 * @return
	 */
	private JPanel getQueryReachesPeerMessageInfoPanel() {
		
		queryIDReachesPeer = new JTextField("Query1");
		queryIDReachesPeer.setBorder(BorderFactory.createTitledBorder("QueryID to reach peer"));
		queryIDReachesPeer.setBackground(getContentPane().getBackground());
		
		JPanel queryReachesPeerMessageInfoPanel = new JPanel();
		queryReachesPeerMessageInfoPanel.setLayout(new BorderLayout());
		queryReachesPeerMessageInfoPanel.add(queryIDReachesPeer,BorderLayout.NORTH);

		return queryReachesPeerMessageInfoPanel;
	}

	/**
	 * helper for making the specific message information tabbed pane (contains tabs to different message types)
	 * @return
	 */
	private JTabbedPane getMessageInfoTabs() {
		messageInfoTabs = new JTabbedPane(JTabbedPane.TOP);

		messageInfoTabs.addTab("Online/Offline", null, getOnlineOfflineMessageInfoPanel(),"Online/Offline Message Information");
		messageInfoTabs.addTab("Connect/Disconnect", null, getConnectDisconnectMessageInfoPanel(),"Connect/Disconnect Message Information");
		messageInfoTabs.addTab("Publish/Remove", null, getPublishRemoveMessageInfoPanel(),"Publish/Remove Message Information");
		messageInfoTabs.addTab("Link/Delink", null, getLinkDelinkDocumentMessageInfoPanel(),"Link/Delink Message Information");
		messageInfoTabs.addTab("Query", null, getQueryMessageInfoPanel(),"Query Message Information");
		messageInfoTabs.addTab("Query Hit", null, getQueryHitMessageInfoPanel(),"Query Hit Message Information");
		messageInfoTabs.addTab("Query Reaches Peer", null, getQueryReachesPeerMessageInfoPanel(),"Query Reaches Peer Message Information");

		messageInfoTabs.setEnabled(false);
		return messageInfoTabs;

	}

	/**
	 * helper for making bottom panel with the message sent information
	 * @return
	 */
	private JPanel getSendPanel() {
		currentTime = new JLabel("Current Time: ");
		currentTime.setToolTipText("The current time in milliseconds");
		
		simulateButton = new JButton("Simulate");
		simulateButton.setToolTipText("Send multiple messages to test the robustness of the graph");
		simulateButton.addActionListener(new SimulateButtonListener(this));
		
		JButton previewButton = new JButton("Preview Message");
		previewButton.setToolTipText("Preview the message to be sent");
		previewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(rootPane, buildSendMessage());
			}
		});

		sendButton = new JButton("Send Message");
		sendButton.setToolTipText("Send the message with the current parameters");
		sendButton.addActionListener(new SendButtonListener());
		
		
		JPanel sendPanel = new JPanel();
		sendPanel.setBorder(BorderFactory.createTitledBorder("Send"));
		sendPanel.add(currentTime);
		sendPanel.add(simulateButton);
		sendPanel.add(previewButton);
		sendPanel.add(sendButton);

		return sendPanel;
	}

	/**
	 * helper for making the top panel with networking information
	 * @return
	 */
	private JPanel getNetworkingPanel() {		
		UDPPort = new JLabel();
		UDPPort.setText(""+currentUDPPort);
		UDPPort.setToolTipText("UDP Port to send message from");
		UDPPort.setBorder(BorderFactory.createTitledBorder("UDP Port"));
		UDPPort.setBackground(getContentPane().getBackground());

		serverINetAddress = new JTextField();
		serverINetAddress.setText("134.117.60.66");
		serverINetAddress.setToolTipText("Server to send UDP Message to");
		serverINetAddress.setBorder(BorderFactory.createTitledBorder("Server IP"));
		serverINetAddress.setBackground(getContentPane().getBackground());


		serverPort = new JTextField();
		serverPort.setText("8888");
		serverPort.setToolTipText("Port on server to send to");
		serverPort.setBorder(BorderFactory.createTitledBorder("Server Port"));
		serverPort.setBackground(getContentPane().getBackground());

		JPanel networkingPanel = new JPanel();
		networkingPanel.setBorder(BorderFactory.createTitledBorder("Network Info"));
		networkingPanel.setLayout(new GridLayout(1,3));

		networkingPanel.add(UDPPort);
		networkingPanel.add(serverINetAddress);
		networkingPanel.add(serverPort);

		return networkingPanel;
	}


	/**
	 * helper for building the message to send to the server with the send button
	 * @return
	 */
	private String buildSendMessage() {

		String time = System.currentTimeMillis()+"     ";
		String peerID = gnutellaID.getSelectedItem()+"     ";
		String eventType = messageType.getSelectedItem()+"     ";
		LinkedList<String> parameters = new LinkedList<String>();

		if(eventType.equals("ONLINE     ") || eventType.equals("OFFLINE     ")) {
			parameters.add(up2pPrefix.getText()+"     ");
		} 
		else if(eventType.equals("CONNECT     ") || eventType.equals("DISCONNECT     ")) {
			parameters.add(otherGnutellaID.getSelectedItem()+"     ");
		}
		else if(eventType.equals("PUBLISH     ") || eventType.equals("REMOVE     ")) {
			parameters.add(documentCommunity.getText()+"     ");
			parameters.add(documentID.getText()+"     ");
		} 
		else if(eventType.equals("LINKDOCUMENT     ") || eventType.equals("DELINKDOCUMENT     ")) {
			parameters.add(linkDocumentOne.getText()+"     ");
			parameters.add(linkDocumentTwo.getText()+"     ");
		}
		else if(eventType.equals("QUERY     ") ) {
			parameters.add("["+queryID.getText()+"]     ");
			parameters.add("Community:"+community.getText()+"     ");
			parameters.add("Query:/article[title[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"+queryString.getText()+"')]]     extent:0.");
		}
		else if(eventType.equals("QUERYHIT     ")) {
			parameters.add(queryIDHasHit.getText()+"     ");
			parameters.add(docCommunity.getText()+"     ");
			parameters.add(docID.getText()+"     ");
			parameters.add(docName.getText()+"     ");
		} 
		else if(eventType.equals("QUERY_REACHES_PEER     ")) {
			parameters.add(queryIDReachesPeer.getText()+"     ");
		}

		StringBuffer message = new StringBuffer();
		message.append(time);
		message.append(peerID);
		message.append(eventType);

		for(String param : parameters) {
			message.append(param);
		}
		message.append("\n");

		return message.toString();
	}
	
	
	private class TimeLabelUpdater implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent ae) {
			currentTime.setText("Current Time: "+System.currentTimeMillis());
		}
	}

	private class SendButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			String message = buildSendMessage();
			
			sendMessage(clientSockets.get(gnutellaIndex), message);
			
		}
	}
	
	private class SimulateButtonListener implements ActionListener {
		
		private GraphServerTester tester;
		
		public SimulateButtonListener(GraphServerTester tester) {
			this.tester = tester;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final String result = JOptionPane.showInputDialog(rootPane, "How Many Peers would you like to simulate",
															"Number of Peers",JOptionPane.QUESTION_MESSAGE);
			if(result!=null) {
				if(!result.equals("")) {
					Thread UDPThread = new Thread(new Runnable() {
						
						public void run() {
							int numberPeers = Integer.parseInt(result);
							int startingPort = STARTING_UDP_PORT+gnutellaIDs.length;
							LargeTest sim = new LargeTest(numberPeers, startingPort, tester);
							sim.sendOnlineMessages();
							sim.sendConnectMessages();
							JOptionPane.showMessageDialog(rootPane, "Messages Sent", "Sent", JOptionPane.INFORMATION_MESSAGE);
						}
					});
					
					UDPThread.start();
					
				}
				
			}
		}
		
	}
	
	public void sendMessage(DatagramSocket socket, String messageToSend) {
		byte[] sendData = messageToSend.getBytes();
		String inetAddress = serverINetAddress.getText();
		int port = Integer.parseInt(serverPort.getText());
		//int result = JOptionPane.showConfirmDialog(rootPane, "Send Message:\n"+message, "Confirmation", JOptionPane.YES_NO_OPTION);
		//if(result == 0) {
			try {
				InetAddress inet = InetAddress.getByName(inetAddress); // get host
			    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, inet, port);
			    socket.send(sendPacket); // send UDP packet to host
			    //JOptionPane.showMessageDialog(rootPane, "Message Sent","Completed",JOptionPane.INFORMATION_MESSAGE);
			} catch (UnknownHostException e) {
				JOptionPane.showMessageDialog(rootPane, "Unknown Host", "ERROR", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(rootPane, "Output Exception", "ERROR", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		//}
	}
	
	private class GnutellaIDListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			gnutellaIndex = gnutellaID.getSelectedIndex();

			currentUDPPort = STARTING_UDP_PORT + gnutellaIndex;
			UDPPort.setText(""+currentUDPPort);

			up2pPrefix.setText("134.117.60.25:635"+gnutellaIndex+"[up2p_"+gnutellaIndex+"]");
		}
	}

	private class MessageTypeListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object type = messageType.getSelectedItem();

			if(type.equals("ONLINE") || type.equals("OFFLINE")) {
				messageInfoTabs.setSelectedIndex(0);
			} 
			else if(type.equals("CONNECT") || type.equals("DISCONNECT")) {
				messageInfoTabs.setSelectedIndex(1);
			}
			else if(type.equals("PUBLISH") || type.equals("REMOVE")) {
				messageInfoTabs.setSelectedIndex(2);
			} 
			else if(type.equals("LINKDOCUMENT") || type.equals("DELINKDOCUMENT")) {
				messageInfoTabs.setSelectedIndex(3);
			}
			else if(type.equals("QUERY") ) {
				messageInfoTabs.setSelectedIndex(4);
			}
			else if(type.equals("QUERYHIT")) {
				messageInfoTabs.setSelectedIndex(5);
			} 
			else if(type.equals("QUERY_REACHES_PEER")) {
				messageInfoTabs.setSelectedIndex(6);
			}
		}
	}

	/**
	 * Generates an an array containing <code>count</code> number of Gnutella IDs.
	 * 
	 * Each gnutella ID is composed of 16 shorts parsed together with '.' seperators
	 * @param count The number of IDs to generate.
	 * @return A String array of the different Gnutella IDs
	 */
	public static Object[] generateGnutellaIDs(int count) {
		ArrayList<String> ids = new ArrayList<String>(count);
		Random rand = new Random(System.currentTimeMillis()); //seed with time

		for(int i=0;i<count;i++) {
			StringBuffer gnutellaID = new StringBuffer(); //because we will be adding to the string time after time
			for(int j=0;j<16;j++) {//gnutella IDs are composed of 16 shorts parsed together with '.' seperators
				int idPart = rand.nextInt(255);
				gnutellaID.append(idPart+".");//add to the string and seperate each number with a period
			}
			ids.add(gnutellaID.toString());
		}
		return ids.toArray();
	}

}
