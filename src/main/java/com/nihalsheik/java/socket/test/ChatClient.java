package com.nihalsheik.java.socket.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import com.nihalsheik.java.socket.Message;
import com.nihalsheik.java.socket.Message.MessageType;

/*
 * The Client that can be run both as a console or a GUI
 */
public class ChatClient {

	private ObjectInputStream sInput; // to read from the socket
	private ObjectOutputStream sOutput; // to write on the socket
	private Socket socket;
	private String server, username;
	private int port;

	/*
	 * Constructor call when used from a GUI in console mode the ClienGUI
	 * parameter is null
	 */
	ChatClient(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}

	/*
	 * To start the dialog
	 */
	public boolean start() {
		// try to connect to the server
		try {
			socket = new Socket(server, port);
			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			display("Error connectiong to server:" + ex.getMessage());
			return false;
		}

		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);

		// creates the Thread to listen from the server
		new ListenFromServer().start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try {
			sOutput.writeObject(username);
		} catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	/*
	 * To send a message to the console or the GUI
	 */
	private void display(String msg) {
		System.out.println(msg);
	}

	/*
	 * To send a message to the server
	 */
	void sendMessage(Message msg) {
		try {
			sOutput.writeObject(msg);
		} catch (IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	/*
	 * When something goes wrong Close the Input/Output streams and disconnect
	 * not much to do in the catch clause
	 */
	private void disconnect() {
		try {
			if (sInput != null)
				sInput.close();
		} catch (Exception e) {
		} // not much else I can do
		try {
			if (sOutput != null)
				sOutput.close();
		} catch (Exception e) {
		} // not much else I can do
		try {
			if (socket != null)
				socket.close();
		} catch (Exception e) {
		} // not much else I can do

	}

	/*
	 * To start the Client in console mode use one of the following command >
	 * java Client > java Client username > java Client username portNumber >
	 * java Client username portNumber serverAddress at the console prompt If
	 * the portNumber is not specified 1500 is used If the serverAddress is not
	 * specified "localHost" is used If the username is not specified
	 * "Anonymous" is used > java Client is equivalent to > java Client
	 * Anonymous 1500 localhost are eqquivalent
	 * 
	 * In console mode, if an error occurs the program simply stops when a GUI
	 * id used, the GUI is informed of the disconnection
	 */
	public static void main(String[] args) {
		// default values
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonymous";

		// depending of the number of arguments provided we fall through
		switch (args.length) {
		// > javac Client username portNumber serverAddr
		case 3:
			serverAddress = args[2];
			// > javac Client username portNumber
		case 2:
			try {
				portNumber = Integer.parseInt(args[1]);
			} catch (Exception e) {
				System.out.println("Invalid port number.");
				System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
				return;
			}
			// > javac Client username
		case 1:
			userName = args[0];
			// > java Client
		case 0:
			break;
		// invalid number of arguments
		default:
			System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
			return;
		}
		// create the Client object
		ChatClient client = new ChatClient(serverAddress, portNumber, userName);
		if (!client.start())
			return;

		Scanner scan = new Scanner(System.in);
		while (true) {

			System.out.print("> ");
			String msg = scan.nextLine();

			if (msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new Message(MessageType.LOGOUT, ""));
				break;
			} else if (msg.equalsIgnoreCase("WHOISIN")) {
				client.sendMessage(new Message(MessageType.WHO_IS_IN, ""));
			} else { // default to ordinary message
				client.sendMessage(new Message(MessageType.MESSAGE, msg));
			}
		}
		client.disconnect();
	}

	/*
	 * a class that waits for the message from the server and append them to the
	 * JTextArea if we have a GUI or simply System.out.println() it in console
	 * mode
	 */
	class ListenFromServer extends Thread {

		public void run() {
			while (true) {
				try {
					String msg = (String) sInput.readObject();
					System.out.println(msg);
					System.out.print("> ");
				} catch (IOException e) {
					display("Server has close the connection: " + e);
					break;
				} catch (ClassNotFoundException e2) {
				}
			}
		}
	}
}
