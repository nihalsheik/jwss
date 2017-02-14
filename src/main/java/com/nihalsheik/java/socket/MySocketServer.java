package com.nihalsheik.java.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MySocketServer implements IActionListener {

	private int port;
	private boolean keepGoing;
	private SimpleDateFormat sdf;
	private long uid = 0;
	private Map<Long, Client> clients = null;

	public MySocketServer(int port) {
		this.port = port;
		clients = new HashMap<Long, Client>();
		this.sdf = new SimpleDateFormat("HH:mm:ss");
	}

	public void start() {
		keepGoing = true;
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			display("Server waiting for Clients on port " + port + ".");

			while (keepGoing) {

				Socket socket = serverSocket.accept(); // accept connection
				if (!keepGoing) {
					break;
				}

				++uid;

				Client client = new Client(socket, uid); // make a thread of it
				client.setActionListener(this);
				client.start();

				clients.put(uid, client);

				this.onClientJoin(client);
			}
			// I was asked to stop
			try {
				for (int i = 0; i < clients.size(); ++i) {
					clients.get(i).close();
				}
				serverSocket.close();
			} catch (Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}

	public void stop() {
		keepGoing = false;
		// connect to myself as Client to exit statement
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		} catch (Exception e) {
			// nothing I can really do
		}
	}

	/*
	 * Display an event (not a message) to the console or the GUI
	 */
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		System.out.println(time);
	}

	/*
	 * to broadcast a message to all Clients
	 */
	private synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf = time + " " + message + "\n";
		// display message on console or GUI
		System.out.print(messageLf);

	}

	// for a client who logoff using the LOGOUT message
	public synchronized void removeClient(long id) {
		System.out.println(id);
		Client ct = clients.get(id);
		ct.close();
		clients.remove(id);
	}

	public void onMessageReceive(Client client, Message cm) {
		// Switch on the type of message receive
		// the messaage part of the ChatMessage

		switch (cm.getType()) {

		case MESSAGE:
			broadcast(client.getName() + ": " + cm.getMessage());
			break;
		case LOGOUT:
			this.removeClient(client.getId());
			break;
		case WHO_IS_IN:
			Iterator<Entry<Long, Client>> t = clients.entrySet().iterator();
			StringBuilder sb = new StringBuilder();
			while (t.hasNext()) {
				Client ct = t.next().getValue();
				sb.append(ct.getName() + "\r\n");
			}
			client.sendMessage(sb.toString());
			break;
		}

	}

	public void onMessageSend(Client client, Message message) {
		System.out.println("onMessageSend");
	}

	public void onClientDisconnect(Client client) {
		System.out.println("onClientDisconnect");
	}

	public void onClientJoin(Client client) {
		System.out.println("onClientJoin");
	}

	/**
	 * For garbage collection
	 */
	@Override
	protected void finalize() throws Throwable {
		System.out.println("GC...");
		super.finalize();
		Iterator<Entry<Long, Client>> t = clients.entrySet().iterator();
		while (t.hasNext()) {
			Client ct = t.next().getValue();
			// Time 10 min
			if (new Date().getTime() - ct.getTimestamp() > 600000) {
				this.removeClient(ct.getId());
			}
		}
	}

}
