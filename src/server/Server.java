package server;

import java.net.Socket;
import java.util.LinkedList;
import java.io.IOException;
import java.net.ServerSocket;

public class Server {
	public final static int SERVER_PORT = 20523;
	public static boolean printLog = true;

	public static ServerSocket serverSocket;

	public static LinkedList<ServerThread> threads;
	public static LinkedList<Player> players;
	public static LinkedList<Room> rooms;

	public static boolean addRoom(String roomID) {
		synchronized (rooms) {
			for (Room room : rooms) {
				if (room.id.equals(roomID))
					return false;
			}
			rooms.add(new Room(roomID));
			return true;
		}
	}

	public static boolean addPlayer(String playerID, ServerThread thread) {
		synchronized (players) {
			for (Player player : players) {
				if (player.id.equals(playerID))
					return false;
			}
			players.add(new Player(playerID, thread));
			return true;
		}
	}

	public static Room fetchRoom(String roomID) {
		for (Room room : rooms) {
			if (room.id.equals(roomID))
				return room;
		}
		return null;
	}

	public static Player fetchPlayer(String playerID) {
		for (Player player : players) {
			if (player.id.equals(playerID))
				return player;
		}
		return null;
	}

	public static void eraseRoom(String roomID) {
		synchronized (rooms) {
			int idx = -1;
			for (int i = 0; i < rooms.size(); i++) {
				if (rooms.get(i).id.equals(roomID)) {
					idx = i;
					break;
				}
			}
			rooms.remove(idx);
		}
	}

	public static void erasePlayer(String playerID) {
		synchronized (players) {
			int idx = -1;
			for (int i = 0; i < players.size(); i++) {
				if (players.get(i).id.equals(playerID)) {
					idx = i;
					break;
				}
			}
			players.remove(idx);
		}
	}
	
	public static void addThread(ServerThread thread) {
		synchronized (threads) {
			threads.add(thread);
		}	
	}
	
	public static void eraseThread(ServerThread thread) {
		synchronized (threads) {
			threads.remove(thread);
		}
	}
	
	public static void debug() {
		System.out.println();
		System.out.println("(Threads)");
		for (ServerThread thread : threads) {
			System.out.println(thread);
		}
		System.out.println("(Players)");
		for (Player player : players) {
			System.out.println(player);
		}
		System.out.println("(Rooms)");
		for (Room room : rooms) {
			System.out.println(room);
		}
	}

	public static void main(String[] args) {
		serverSocket = null;
		threads = new LinkedList<ServerThread>();
		players = new LinkedList<Player>();
		rooms = new LinkedList<Room>();

		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			System.out.println("Bind complete");

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("New connection created");

				ServerThread thread = new ServerThread(socket);
				thread.start();
				addThread(thread);
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} finally {
			try {
				if (serverSocket != null && !serverSocket.isClosed()) {
					serverSocket.close();
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}
}