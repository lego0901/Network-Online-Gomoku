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

import server.Player.PlayerState;

public class ServerThread extends Thread {
	public static final int QUERY_TIMEOUT_SECONDS = 70;

	Socket socket = null;
	Player player = null;

	ServerThread opponentThread = null;

	BufferedReader reader;
	PrintWriter writer;

	LocalDateTime lastQueryTime;

	public ServerThread(Socket socket) {
		this.socket = socket;
		lastQueryTime = LocalDateTime.now();
	}

	void debug(String str) {
		if (Server.printLog)
			System.out.println(str);
	}

	void write(String str) {
		writer.println(str);
		writer.flush();
	}
	
	void writeOpponent(String str) {
		if (opponentThread != null) {
			opponentThread.write("opponent\n" + str);
		}
	}
	
	void disconnectMeFromOpponentThread() {
		if (opponentThread != null) {
			opponentThread.opponentThread = null;
		}
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

	public static int[] parseCoordinates(String str) {
		String[] parsed = str.split(" ");
		if (parsed.length != 2)
			return null;

		int[] rc = new int[2];
		for (int i = 0; i < 2; i++) {
			try {
				rc[i] = Integer.parseInt(parsed[i]);
			} catch (NumberFormatException nfe) {
				return null;
			}
		}
		return rc;
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
						String playerID = reader.readLine().strip();
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
							Server.debug();
						} else if (query.equals("join")) {
							String roomID = reader.readLine().strip();
							if (Server.fetchRoom(roomID) != null) {
								if (player.enterRoom(roomID)) {
									opponentThread = Server.fetchThread(player.opponentID());
									opponentThread.opponentThread = this;

									debug("Player " + player.id + " joined the room " + roomID);
									write("success");
									write("opponent\njoin\n" + opponentThread.player.id);
									writeOpponent("join\n" + player.id);
									if (opponentThread.player.state == PlayerState.READY_ROOM) {
										write("opponent\nready");
									}
								} else {
									debug("Room " + roomID + " is now full");
									write("fail");
								}
							} else {
								debug("Room " + roomID + " is not found");
								write("fail");
							}
							Server.debug();
						} else if (query.equals("search")) {
							// TODO: implement this
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
							writeOpponent("ready");
						} else if (query.equals("leave")) {
							player.leaveRoom();
							debug("Player " + player.id + " leaved the room");
							write("success");
							writeOpponent("leave");
							disconnectMeFromOpponentThread();
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
							writeOpponent("cancel");
						} else if (query.equals("leave")) {
							player.leaveRoom();
							debug("Player " + player.id + " leaved the room");
							write("success");
							writeOpponent("leave");
							disconnectMeFromOpponentThread();
						} else {
							debug("Invalid query received");
							write("invalid");
						}
						break;
					case MY_TURN:
						if (query.equals("stone")) {
							String coordinatesString = reader.readLine().strip();
							int[] parsedCoordinates = parseCoordinates(coordinatesString);
							if (parsedCoordinates == null) {
								debug("Player " + player.id + " invalid input received");
								write("fail");
								continue;
							}

							int row = parsedCoordinates[0], column = parsedCoordinates[1];
							if (player.putStone(row, column)) {
								debug("Player " + player.id + " put stone on (" + row + ", " + column + ")");
								write("success");
								writeOpponent("stone\n" + row + " " + column);
							} else {
								debug("Player " + player.id + " cannot put stone on (" + row + ", " + column + ")");
								debug("Reason: " + player.putStoneErrorMsg());
								write("invalid move");
								write(player.putStoneErrorMsg());
							}
							System.out.println(player.room.game);
							Server.debug();
						} else if (query.equals("surrender")) {
							player.setLoser();
							debug("Surrender by player " + player.id);
							write("success");
							writeOpponent("surrender");
						} else {
							debug("Invalid query received");
							write("invalid");
						}
						break;
					case NOT_MY_TURN:
						debug("Query from NOT_MY_TURN is ignored");
						write("ignored");
						// ignore all signals
					}
				} else if (isThreadTimeout()) {
					closeConnection = true;
					debug("Client query timeout");
					write("query timeout");
					writeOpponent("query timeout");
				} else {
					switch (player.state) {
					case READY_ROOM:
						if (player.isMyRoomReady()) {
							player.initializeGame();
							debug("Room " + player.room.id + "'s game is initialized with player " + player.id
									+ " with turnID = " + player.turnID);
							write("play");
							if (player.isMyTurn())
								write("your turn");
							else
								write("not your turn");
						}
						break;
					case MY_TURN:
						if (!player.isMyTurn()) {
							player.state = PlayerState.NOT_MY_TURN;
							write("not your turn");
						} else if (player.isTimeout()) {
							player.setLoser();
							debug("Stone timeout for player " + player.id);
							write("stone timeout");
							writeOpponent("stone timeout");
						} else if (player.isMyGameTerminated()) {
							player.endGame();
							debug("Game terminated " + player.id);
							write("end");

							if (player.isWinner()) {
								write("you win");
							} else if (player.isLoser()) {
								write("you lose");
							} else {
								write("you draw");
							}
						}
						break;
					case NOT_MY_TURN:
						setTimer();
						if (player.isMyTurn()) {
							player.setTimer();
							player.state = PlayerState.MY_TURN;
							write("your turn");
						} else if (player.isMyGameTerminated()) {
							player.endGame();
							debug("Game terminated " + player.id);
							write("end");

							if (player.isWinner()) {
								write("you win");
							} else if (player.isLoser()) {
								write("you lose");
							} else {
								write("you draw");
							}
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
			debug("Try to close");
			writeOpponent("leave");
			disconnectMeFromOpponentThread();
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
