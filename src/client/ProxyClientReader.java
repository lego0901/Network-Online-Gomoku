package client;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class ProxyClientReader {
	final static boolean printLog = true;
	final static String SERVER_HOST = "147.46.209.30";
	final static int SERVER_PORT = 20523;
	final static String WRITER_HOST = "localhost";

	final static int WAIT_AFTER_TERMINATE = 5;

	static int writerPort;

	static BufferedReader proxyReader;

	static BufferedReader reader;
	static PrintWriter writer;
	static Board gameBoard;
	static boolean printCUI = true;

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

	public static void main(String[] args) {
		ServerSocket proxyServerSocket = null;
		Socket proxySocket = null;

		Socket socket = null;

		gameBoard = new Board(11, 11);

		writerPort = Integer.parseInt(args[0]);

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
			proxyServerSocket = new ServerSocket(writerPort);
			proxySocket = proxyServerSocket.accept();

			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			proxyReader = new BufferedReader(
					new InputStreamReader(proxySocket.getInputStream(), StandardCharsets.UTF_8));

			while (true) {
				System.out.println("Please type your nickname");
				String id = proxyReader.readLine();
				write("player");
				write(id);

				String response = reader.readLine().strip();
				if (response.equals("success")) {
					Player.id = id;
					break;
				} else if (response.equals("fail")) {
					System.out.println("It is an impossible name");
				}
			}

			while (Player.state != Player.State.EXIT) {
				if (reader.ready()) {
					String response = reader.readLine();
					System.out.println("Received: " + response);
					if (response.length() >= 8 && response.substring(0, 8).equals("opponent"))
						Opponent.processResponse(response.substring(9));
					else
						Player.processResponse(response);
					printCUI = true;
				} else if (proxyReader.ready()) {
					String query = proxyReader.readLine().strip();
					Player.processQuery(query);
					printCUI = true;
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
						printCUI = true;
					}
				}
				
				if (printCUI) {
					CUI.repaint();
					printCUI = false;
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
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}
}
