package com.nihalsheik.java.socket.test;


import com.nihalsheik.java.socket.Message;
import com.nihalsheik.java.socket.MySocketServer;
import com.nihalsheik.java.socket.Client;

/*
 * The server that can be run both as a console application or a GUI
 */
public class ChatServer extends MySocketServer {

	public ChatServer(int port) {
		super(port);
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

	public static void main(String[] args) {
		// start server on port 1500 unless a PortNumber is specified
		int portNumber = 1500;
		switch (args.length) {
		case 1:
			try {
				portNumber = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Invalid port number.");
				System.out.println("Usage is: > java Server [portNumber]");
				return;
			}
		case 0:
			break;
		default:
			System.out.println("Usage is: > java Server [portNumber]");
			return;

		}
		ChatServer server = new ChatServer(portNumber);
		server.start();
	}
}
