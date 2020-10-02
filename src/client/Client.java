package client;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JOptionPane;

import graphics.GameFrame;
import graphics.PlayerIDFrame;
import graphics.RoomFrame;
import graphics.RoomSelectFrame;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Client {
	public final static boolean printLog = true;
	public final static String SERVER_HOST = "147.46.209.30";
	public final static int SERVER_PORT = 20523;

	public final static int WAIT_AFTER_TERMINATE = 5;
	public static final int CONNECTION_CHECK_TIMEOUT_SECONDS = 10;
	public static final int CONNECTION_CHECK_PERIOD = 1;

	public static BufferedReader reader;
	public static PrintWriter writer;
	public static Queue<String> queryQueue;
	public static Board gameBoard;
	public static LocalDateTime lastConnectionCheckTime;
	public static LocalDateTime lastConnectionSendTime;
	public static boolean displayGUI = true;

	public static PlayerIDFrame playerIDFrame;
	public static RoomSelectFrame roomSelectFrame;
	public static RoomFrame roomFrame;
	public static GameFrame gameFrame;

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

	static void debug(String str) {
		if (printLog)
			System.out.println(str);
	}

	static void write(String str) {
		writer.println(str);
		writer.flush();
	}

	static boolean isQueryPended() {
		return !queryQueue.isEmpty();
	}

	static String nextQuery() {
		while (!isQueryPended()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return queryQueue.poll();
	}

	public static void pendQuery(String query) {
		queryQueue.add(query);
	}

	static void setConnectionCheckTimer() {
		lastConnectionCheckTime = LocalDateTime.now();
	}

	static void setConnectionSendTimer() {
		lastConnectionSendTime = LocalDateTime.now();
	}

	static boolean shouldSendConnectionSend() {
		return Duration.between(lastConnectionSendTime, LocalDateTime.now()).getSeconds() >= CONNECTION_CHECK_PERIOD;
	}

	static boolean isConnectionCheckTimeout() {
		if (lastConnectionCheckTime != null && Duration.between(lastConnectionCheckTime, LocalDateTime.now())
				.getSeconds() >= CONNECTION_CHECK_TIMEOUT_SECONDS)
			return true;
		return false;
	}

	public static void main(String[] args) {
		Socket socket = null;

		gameBoard = new Board(11, 11);

		playerIDFrame = new PlayerIDFrame();
		roomSelectFrame = new RoomSelectFrame();
		roomFrame = new RoomFrame();
		gameFrame = new GameFrame();

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			queryQueue = new LinkedList<String>();

			playerIDFrame.setVisible(true);
			
			setConnectionCheckTimer();
			setConnectionSendTimer();

			boolean sendPlayerID = false;
			String id = "";
			while (true) {
				if (isQueryPended()) {
					String query = nextQuery();
					
					if (!sendPlayerID) {
						sendPlayerID = true;
						id = query;
						write("player");
						write(id);
					}
				}
				if (reader.ready()) {
					String response = reader.readLine().strip();
					if (response.equals("connection check")) {
						setConnectionCheckTimer();
						Thread.sleep(10);
					} else if (sendPlayerID) {
						if (response.equals("success")) {
							Player.id = id;
							pendQuery("search");
							break;
						} else if (response.equals("fail")) {
							sendPlayerID = false;
							if (2 <= id.length() && id.length() <= 20)
								playerIDFrame.playerIDErrorMsg.setText("That player ID already exists.");
							else
								playerIDFrame.playerIDErrorMsg.setText("The length should be 2<=len<=20.");
						}
					}
				}
				if (shouldSendConnectionSend()) {
					setConnectionSendTimer();
					write("connection check");
				}
				if (isConnectionCheckTimeout()) {
					JOptionPane.showMessageDialog(Client.roomSelectFrame, "Disconnected from the server");
					Thread.sleep(5000);
					write("close");
					System.exit(0);
				}
			}
			roomSelectFrame.setBounds(playerIDFrame.getBounds());
			playerIDFrame.setVisible(false);

			while (Player.state != Player.State.EXIT) {
				if (displayGUI) {
					GUI.synchronizeFrameBounds();
					GUI.showFrame();
					GUI.repaint();
					displayGUI = false;
				}

				if (reader.ready()) {
					String response = reader.readLine();
					if (response.equals("connection check")) {
						Thread.sleep(10);
						setConnectionCheckTimer();
						continue;
					} else if (response.length() >= 8 && response.substring(0, 8).equals("opponent"))
						Opponent.processResponse(response.substring(9));
					else
						Player.processResponse(response);
					displayGUI = true;
				} else if (isQueryPended()) {
					String query = nextQuery().strip();
					Player.processQuery(query);
					displayGUI = true;
				}

				if (shouldSendConnectionSend()) {
					setConnectionSendTimer();
					write("connection check");
				}
				if (isConnectionCheckTimeout()) {
					JOptionPane.showMessageDialog(Client.roomSelectFrame, "Disconnected from the server");
					Thread.sleep(5000);
					write("close");
					System.exit(0);
				}

				if (Player.state == Player.State.TERMINATED) {
					if (Duration.between(Player.terminateTime, LocalDateTime.now())
							.getSeconds() >= WAIT_AFTER_TERMINATE) {
						Player.state = Player.State.ENTER_ROOM;
						Player.inputState = Player.InputState.IN_ROOM;
						if (Opponent.state != Opponent.State.NONE) {
							Opponent.state = Opponent.State.ENTER_ROOM;
							Opponent.inputState = Opponent.InputState.IN_ROOM;
						}
						displayGUI = true;
					}
				}
				if (Player.state == Player.State.MY_TURN || Player.state == Player.State.NOT_MY_TURN) {
					gameFrame.repaintTimer();
				}

				Thread.sleep(10);
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} catch (InterruptedException iex) {
			try {
				write("close");
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
			iex.printStackTrace();
		} finally {
			try {
				write("close");
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
				System.exit(0);
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}
}
