package client;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
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

	static enum ClientState {
		NONE, SEARCH_ROOM, ENTER_ROOM, READY_ROOM, MY_TURN, NOT_MY_TURN, TERMINATED,
	}

	static BufferedReader reader;
	static PrintWriter writer;
	static Scanner keyboard;

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
		keyboard = new Scanner(System.in);
		ClientState state = ClientState.NONE;
		boolean closeConnection = false;

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
			debug("Connected successfully!");

			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

			while (state == ClientState.NONE) {
				System.out.println("Please type your nickname\n> ");
				String id = keyboard.nextLine();
				write("player");
				write(id);

				String response = reader.readLine().strip();
				if (response.equals("success")) {
					state = ClientState.SEARCH_ROOM;
				} else if (response.equals("fail")) {
					System.out.println("It is an impossible name");
				}
			}

			while (!closeConnection) {
				if (reader.ready()) {
					String response = reader.readLine().strip();
					
					if (response.equals("query timeout")) {
						closeConnection = false;
						break;
					}

					switch (state) {
					case SEARCH_ROOM:
						break;
					case ENTER_ROOM:
						if (response.equals("opponent")) {
							String nextResponse = reader.readLine().strip();
							if (nextResponse.equals("join")) {
								// TODO: implement this
							} else if (nextResponse.equals("ready")) {
								// TODO: implement this
							} else if (nextResponse.equals("cancel")) {
								// TODO: implement this
							} else if (nextResponse.equals("leave")) {
								// TODO: implement this
							} else {
								System.out.println("Unexpected nextResponse: " + nextResponse);
							}
						} else {
							System.out.println("Unexpected response: " + response);
						}
						break;
					case READY_ROOM:
						if (response.equals("opponent")) {
							String nextResponse = reader.readLine().strip();
							if (nextResponse.equals("join")) {
								// TODO: implement this
							} else if (nextResponse.equals("ready")) {
								// TODO: implement this
							} else if (nextResponse.equals("cancel")) {
								// TODO: implement this
							} else if (nextResponse.equals("leave")) {
								// TODO: implement this
							} else {
								System.out.println("Unexpected nextResponse: " + nextResponse);
							}
						} else if (response.equals("play")) {
							// TODO: implement this
						} else {
							System.out.println("Unexpected response: " + response);
						}
						break;
					case MY_TURN:
						if (response.equals("opponent")) {
							String nextResponse = reader.readLine().strip();
							if (nextResponse.equals("leave")) {
								// TODO: implement this
							} else if (nextResponse.equals("surrender")) {
								// TODO: implement this
							} else {
								System.out.println("Unexpected nextResponse: " + nextResponse);
							}
						} else if (response.equals("stone timeout")) {
							// TODO: implement this
						} else if (response.equals("end")) {
							// TODO: implement this
						} else {
							System.out.println("Unexpected response: " + response);
						}
						break;
					case NOT_MY_TURN:
						if (response.equals("opponent")) {
							String nextResponse = reader.readLine().strip();
							if (nextResponse.equals("stone")) {
								// TODO: implement this
							} else if (nextResponse.equals("stone timeout")) {
								// TODO: implement this
							} else if (nextResponse.equals("leave")) {
								// TODO: implement this
							} else if (nextResponse.equals("surrender")) {
								// TODO: implement this
							} else {
								System.out.println("Unexpected nextResponse: " + nextResponse);
							}
						} else if (response.equals("end")) {
							// TODO: implement this
						} else {
							System.out.println("Unexpected response: " + response);
						}
						break;
					case TERMINATED:
					default:
						closeConnection = true;
						break;
					}
				} else {
					String query;
					
					switch (state) {
					case SEARCH_ROOM:
						System.out.println("Please select any action to choose a room");
						System.out.println("> create");
						System.out.println("> search");
						System.out.println("> join");
						
						query = keyboard.nextLine();
						if (query.equals("create")) {
							// TODO: implement this
						} else if (query.equals("search")) {
							// TODO: implement this
						} else if (query.equals("join")) {
							// TODO: implement this
						} else {
							System.out.println("Unexpected query: " + query);
						}
						break;
					case ENTER_ROOM:
						System.out.println("Are you ready?");
						System.out.println("> ready");
						System.out.println("> leave");
						
						query = keyboard.nextLine();
						if (query.equals("ready")) {
							// TODO: implement this
						} else if (query.equals("leave")) {
							// TODO: implement this
						} else {
							System.out.println("Unexpected query: " + query);
						}
						break;
					case READY_ROOM:
						System.out.println("Are you ready?");
						System.out.println("> cancel");
						System.out.println("> leave");
						
						query = keyboard.nextLine();
						if (query.equals("cancel")) {
							// TODO: implement this
						} else if (query.equals("leave")) {
							// TODO: implement this
						} else {
							System.out.println("Unexpected query: " + query);
						}
						break;
					case MY_TURN:
						System.out.println("What do you want to do?");
						System.out.println("> stone");
						System.out.println("> surrender");
						
						query = keyboard.nextLine();
						if (query.equals("stone")) {
							// TODO: implement this
						} else if (query.equals("surrender")) {
							// TODO: implement this
						} else {
							System.out.println("Unexpected query: " + query);
						}
						break;
					case NOT_MY_TURN:
						System.out.println("Other's turn");
						break;
					case TERMINATED:
						// TODO: implement this
						break;
					default:
						closeConnection = true;
						break;
					}
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
