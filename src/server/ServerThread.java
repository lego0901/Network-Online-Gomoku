package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class ServerThread extends Thread {
	public static final int QUERY_TIMEOUT_SECONDS = 70;

	Socket socket = null;
	Player player = null;

	BufferedReader reader;
	PrintWriter writer;

	LocalDateTime lastQueryTime;

	public ServerThread(Socket socket) {
		this.socket = socket;
		lastQueryTime = LocalDateTime.now();
	}

	void write(String str) {
		writer.println(str);
		writer.flush();
	}

	void debug(String str) {
		if (Server.printLog)
			System.out.println(str);
	}
	
	void setTimer() {
		lastQueryTime = LocalDateTime.now();
	}

	boolean isThreadTimeout() {
		if (Duration.between(lastQueryTime, LocalDateTime.now()).getSeconds() >= QUERY_TIMEOUT_SECONDS)
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		String str = "<" + socket.getInetAddress().toString();
		if (player != null) {
			str += ", player: " + player.id;
		}
		str += ", last query time: " + lastQueryTime.toString();
		str += ">";
		return str;
	}

	public void run() {
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

			boolean closeConnection = false;

			while (player == null && !closeConnection) {
				if (reader.ready()) {
					String query = reader.readLine().strip();
					setTimer();
					Server.debug();

					if (query.equals("close")) {
						closeConnection = true;
						break;
					} else if (query.equals("player")) {
						String playerID = reader.readLine();
						if (Server.addPlayer(playerID, this)) {
							player = Server.fetchPlayer(playerID);
							debug("Player " + playerID + " successfully created!");
							write("success");
						} else {
							debug("Player " + playerID + " is duplicated");
							write("fail");
						}
					} else {
						debug("Invalid query received");
						write("invalid");
					}
				} else if (isThreadTimeout()) {
					debug("Client query timeout");
					write("query timeout");
					closeConnection = true;
				}
				sleep(10);
			}

			while (!closeConnection) {
				if (reader.ready()) {
					String query = reader.readLine().strip();
					setTimer();
					Server.debug();

					if (query.equals("close")) {
						closeConnection = true;
						break;
					}

					switch (player.state) {
					case SEARCH_ROOM:
						if (query.equals("create")) {
							String roomID = reader.readLine().strip();
							if (Server.addRoom(roomID)) {
								player.enterRoom(roomID);
								debug("Player " + player.id + " created and joined the room " + roomID);
								write("success");
							} else {
								debug("Room " + roomID + " is duplicated");
								write("fail");
							}
						} else if (query.equals("join")) {
							String roomID = reader.readLine().strip();
							if (Server.fetchRoom(roomID) != null) {
								if (player.enterRoom(roomID)) {
									debug("Player " + player.id + " joined the room " + roomID);
									write("success");
								} else {
									debug("Room " + roomID + " is now full");
									write("fail");
								}
							} else {
								debug("Room " + roomID + " is not found");
								write("fail");
							}
						} else {
							debug("Invalid query received");
							write("invalid");
						}
						break;
					case ENTER_ROOM:
						if (query.equals("ready")) {
							debug("Player " + player.id + " is now ready");
							player.ready();
							write("success");
						} else if (query.equals("leave")) {
							player.leaveRoom();
							debug("Player " + player.id + " leaved the room");
							write("success");
						} else {
							debug("Invalid query received");
							write("invalid");
						}
						break;
					case READY_ROOM:
						if (query.equals("cancel")) {
							player.cancelReady();
							debug("Player " + player.id + " isn't ready");
							write("success");
						} else if (query.equals("leave")) {
							player.leaveRoom();
							debug("Player " + player.id + " leaved the room");
							write("success");
						} else {
							debug("Invalid query received");
							write("invalid");
						}
					case MY_TURN:
						if (query.equals("stone")) {
							// TODO: implement this
						} else if (query.equals("surrender")) {
							// TODO: implement this
						} else {
							// TODO: implement this
						}
					case NOT_MY_TURN:
						debug("Query from NOT_MY_TURN is ignored");
						write("ignored");
						// ignore all signals
					}
				} else if (isThreadTimeout()) {
					closeConnection = true;
					debug("Client query timeout");
					write("query timeout");
				} else {
					switch (player.state) {
					case READY_ROOM:
						if (player.isMyRoomReady()) {
							player.room.initalize();
							if (player.isMyTurn()) {
								player.setTimer();
							}
						}
						break;
					case MY_TURN:
						if (player.isTimeout()) {
							// TODO: implement this
						} else if (player.isMyGameTerminated()) {
							// TODO: implement this
						}
						break;
					case NOT_MY_TURN:
						if (player.isMyGameTerminated()) {
							// TODO: implement this
						}
						break;
					default:
						break;
					}
				}
				sleep(10);
			}

			write("bye");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} finally {
			debug("try to close");
			if (player != null) {
				Server.erasePlayer(player.id);
				if (player.room != null) {
					player.leaveRoom();
				}
			}
			
			Server.eraseThread(this);

			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}
}
