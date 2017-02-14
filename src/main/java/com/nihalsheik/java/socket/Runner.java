package com.nihalsheik.java.socket;

import com.nihalsheik.java.socket.chat.ChatClient;
import com.nihalsheik.java.socket.chat.ChatServer;

public class Runner {

	public void start() {
		_print("Starting");
	}

	private void _print(String msg) {
		System.out.println(msg);
	}

	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("Argument error");
			return;
		}

		String type = args[0];

		String[] p = new String[args.length - 1];
		for (int i = 1; i < args.length; i++) {
			p[i - 1] = args[i];
		}

		if (type.toLowerCase().equals("server")) {
			ChatServer.main(p);
		} else if (type.toLowerCase().equals("client")) {
			ChatClient.main(p);
		}

		new Runner().start();
	}

}
