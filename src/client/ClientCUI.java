package client;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Scanner;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;

public class ClientCUI {
	final static boolean printLog = true;
	final static String SERVER_HOST = "147.46.209.30";
	final static int SERVER_PORT = 20523;

	static BufferedReader reader;
	static PrintWriter writer;
	static Scanner keyboard;
	static Board gameBoard;

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
		Socket socket = null;
		boolean closeConnection = false;
		String playerID = "";
		String roomID = "";
		String opponentID = "";
		boolean opponentReady = false;
		Board board = null;
		int turnID = 0;
		LocalDateTime lastMove = LocalDateTime.now();

		keyboard = new Scanner(System.in);
		gameBoard = new Board(11, 11);

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
			debug("Connected successfully!");

			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

			while (true) {
				System.out.println("Please type your nickname");
				String id = keyboard.nextLine();
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

			boolean printCUI = true;
			
			while (Player.state != Player.State.EXIT) {
				if (reader.ready()) {
					System.out.println("ready?");
					String response = reader.readLine();
					if (response.length() >= 8 && response.substring(0, 8) == "opponent")
						Opponent.processResponse(response.substring(9));
					else
						Player.processResponse(response);
					printCUI = true;
				} else if (keyboard.hasNextLine()) {
					System.out.println("keyboard?");
					String query = keyboard.nextLine().strip();
					Player.processQuery(query);
					printCUI = true;
				}
				if (printCUI) {
					Player.repaint();
					printCUI = false;
				}
				Thread.sleep(10);
			}
			System.out.println("out?");
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
