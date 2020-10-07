/*
 * Server.java
 * Author: Woosung Song
 *
 * Main server that accepts clients and manage them.
 */
package server;

import java.net.Socket;
import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class Server {
  // Server port number
  public static int serverPort;

  // To print log on the server program
  public static boolean printLog = true;

  // To accept clients
  public static ServerSocket serverSocket;

  // Management on the client states
  public static LinkedList<Player> players;
  public static LinkedList<Room> rooms;
  public static LinkedList<ServerThread> threads;

  // To know whether room information is changed from ServerThread
  public static int roomInfoVersion = 0;

  // Add new player if possible
  public static boolean addPlayer(String playerID, ServerThread thread) {
    if (!Player.isValidPlayerID(playerID))
      return false; // invalid playerID
    synchronized (players) {
      for (Player player : players) {
        if (player.id.equals(playerID))
          return false; // no duplicate allowed
      }
      players.add(new Player(playerID, thread));
      return true;
    }
  }

  // Add new room if possible
  public static boolean addRoom(String roomID) {
    if (!Room.isValidRoomID(roomID))
      return false; // invalid roomID
    synchronized (rooms) {
      for (Room room : rooms) {
        if (room.id.equals(roomID))
          return false; // no duplicate allowed
      }
      rooms.add(new Room(roomID));
      roomInfoVersion++;
      return true;
    }
  }

  // Add new ServerThread to manage socket connection between clients
  public static void addThread(ServerThread thread) {
    synchronized (threads) {
      threads.add(thread);
    }	
  }

  // Fetch player with the given ID. Read process doesn't need synchronization
  public static Player fetchPlayer(String playerID) {
    for (Player player : players) {
      if (player.id.equals(playerID))
        return player;
    }
    return null;
  }

  // Fetch room with the given ID
  public static Room fetchRoom(String roomID) {
    for (Room room : rooms) {
      if (room.id.equals(roomID))
        return room;
    }
    return null;
  }

  // Fetch thread with the given playerID
  public static ServerThread fetchThread(String playerID) {
    for (ServerThread thread : threads) {
      if (thread.player != null && thread.player.id == playerID)
        return thread;
    }
    return null;
  }

  // Erase player with the given name
  public static void erasePlayer(String playerID) {
    synchronized (players) {
      int idx = -1;
      for (int i = 0; i < players.size(); i++) {
        if (players.get(i).id.equals(playerID)) {
          idx = i;
          break;
        }
      }
      if (idx != -1) {
        players.remove(idx);
      }
    }
  }

  // Erase room with the given id
  public static void eraseRoom(String roomID) {
    synchronized (rooms) {
      int idx = -1;
      for (int i = 0; i < rooms.size(); i++) {
        if (rooms.get(i).id.equals(roomID)) {
          idx = i;
          break;
        }
      }
      if (idx != -1) {
        roomInfoVersion++;
        rooms.remove(idx);
      }
    }
  }

  // Erase ServerThread
  public static void eraseThread(ServerThread thread) {
    synchronized (threads) {
      threads.remove(thread);
    }
  }

  // Print out the server status
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

  // Server main process. An asynchronous server that accepts multiple clients.
  // Just accept clients and run ServerThread for each one.
  public static void main(String[] args) {
    serverSocket = null;
    threads = new LinkedList<ServerThread>();
    players = new LinkedList<Player>();
    rooms = new LinkedList<Room>();

    try {
      // Read configure.xml to fetch the serverPort numbeer
      File configureXMLFile = new File("./configure.xml");
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(configureXMLFile);
      doc.getDocumentElement().normalize();

      NodeList serverInfo = doc.getDocumentElement().getChildNodes();
      for (int i = 0; i < serverInfo.getLength(); i++) {
        Node node = serverInfo.item(i);
        if (node.getNodeName().equals("port"))
          serverPort = Integer.parseInt(node.getTextContent());
      }

      // Bind server
      serverSocket = new ServerSocket(serverPort);
      System.out.println("Bind to port " + serverPort + " success");

      while (true) {
        Socket socket = serverSocket.accept();
        System.out.println("New connection created");

        // Create a new ServerThread for each connection
        ServerThread thread = new ServerThread(socket);
        thread.start();
        addThread(thread);
      }
    } catch (IOException ioex) {
      ioex.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
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
