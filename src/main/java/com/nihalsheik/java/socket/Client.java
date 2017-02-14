package com.nihalsheik.java.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.nihalsheik.java.socket.Message.MessageType;

public class Client extends Thread {

	private Socket socket;
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	private long id;
	private Message cm;
	private SimpleDateFormat sdf;
	private long timestamp;

	private IActionListener actionListener = null;

	public void setActionListener(IActionListener actionListener) {
		this.actionListener = actionListener;
	}

	Client(Socket socket, long uniqueId) {

		this.id = uniqueId;
		this.sdf = new SimpleDateFormat("HH:mm:ss");

		this.socket = socket;
		System.out.println("Thread trying to create Object Input/Output Streams");
		try {
			// create output first
			sOutput = new ObjectOutputStream(socket.getOutputStream());
			sInput = new ObjectInputStream(socket.getInputStream());
			String username;
			username = (String) sInput.readObject();
			display(username + " just connected.");
			setName(username);

		} catch (IOException e) {
			display("Exception creating new Input/output Streams: " + e);
			return;
		} catch (ClassNotFoundException e) {

		}

	}

	// what will run forever
	public void run() {

		boolean keepGoing = true;
		while (keepGoing) {
			// read a String (which is an object)
			try {
				cm = (Message) sInput.readObject();
				this.timestamp = new Date().getTime();
				this.actionListener.onMessageReceive(this, cm);

				switch (cm.getType()) {
				case MESSAGE:
					this.sendMessage(getName() + ":" + cm.getMessage());
					break;
				case LOGOUT:
					display(getName() + " disconnected with a LOGOUT message.");
					keepGoing = false;
					break;
				default:
					break;
				}

			} catch (IOException e) {
				display(getName() + " Exception reading Streams: " + e);
				this.actionListener.onClientDisconnect(this);
				break;
			} catch (ClassNotFoundException e2) {
				System.out.println(e2.getMessage());
				this.actionListener.onClientDisconnect(this);
				break;
			}

		}

		close();
	}

	// try to close everything
	public void close() {
		// try to close the connection
		try {
			if (sOutput != null)
				sOutput.close();
		} catch (Exception e) {
		}
		try {
			if (sInput != null)
				sInput.close();
		} catch (Exception e) {
		}
		;
		try {
			if (socket != null)
				socket.close();
		} catch (Exception e) {
		}
	}

	/*
	 * Write a String to the Client output stream
	 */
	public boolean sendMessage(String message) {
		// if Client is still connected send the message to it
		if (!socket.isConnected()) {
			this.actionListener.onClientDisconnect(this);
			return false;
		}
		// write the message to the stream
		try {
			sOutput.writeObject(message);
			this.timestamp = new Date().getTime();
			this.actionListener.onMessageSend(this, new Message(MessageType.MESSAGE, message));
		}
		// if an error occurs, do not abort just inform the user
		catch (IOException e) {
			display("Error sending message to " + getName());
			display(e.toString());
		}
		return true;
	}

	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		System.out.println(time);
	}

	public long getId() {
		return this.id;
	}

	public long getTimestamp() {
		return timestamp;
	}

}
