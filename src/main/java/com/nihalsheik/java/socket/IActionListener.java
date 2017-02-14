package com.nihalsheik.java.socket;

public interface IActionListener {

	void onMessageReceive(Client client, Message message);

	void onMessageSend(Client client, Message message);

	void onClientDisconnect(Client client);

	void onClientJoin(Client client);

}
