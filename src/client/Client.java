/*
 * Client.java
 * Author: Woosung Song
 *
 * Main client class.
 * Linked with Player and Opponent's FSMs, and process query and response.
 * Also, it is linked with GUI.
 */
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
  // Server connection information
  public final static String SERVER_HOST = "147.46.209.30";
  public final static int SERVER_PORT = 20523;

  // Check if the client is connected to the server for each second
  public static final int CONNECTION_CHECK_PERIOD = 1;
  // If client is disconnected for longer than 10s, then treat it as permanent
  public static final int CONNECTION_CHECK_TIMEOUT_SECONDS = 10;
  // Time sleeping right after the game is ended (5 seconds)
  public final static int WAIT_AFTER_TERMINATE = 5;

  // To print log on the server program
  public final static boolean printLog = true;

  // Reader to the client, writer from the client
  public static BufferedReader reader;
  public static PrintWriter writer;
  // String queue to send to the server
  public static Queue<String> queryQueue;

  // Last time that connection check signal received from the server
  public static LocalDateTime lastConnectionCheckTime;
  // Last time that connection check signal sent to the server
  public static LocalDateTime lastConnectionSendTime;

  // Game board information (much lighter than server's)
  public static Board gameBoard;
  // Should the client GUI should repaint the frames
  public static boolean repaintGUI = true;

  // Graphics
  public static PlayerIDFrame playerIDFrame;
  public static RoomSelectFrame roomSelectFrame;
  public static RoomFrame roomFrame;
  public static GameFrame gameFrame;

  // Parse "1 2" string into {1, 2} array
  public static int[] parseCoordinates(String str) {
    String[] parsed = str.split(" ");
    if (parsed.length != 2) // only accepts "r c" type
      return null;

    int[] rc = new int[2];
    for (int i = 0; i < 2; i++) {
      try {
        rc[i] = Integer.parseInt(parsed[i]);
      } catch (NumberFormatException nfe) {
        return null; // Not parsable; not an integer
      }
    }
    return rc;
  }

  // Print out the client status in console
  static void debug(String str) {
    if (printLog)
      System.out.println(str);
  }

  // Send a string to the server and flush
  static void write(String str) {
    writer.println(str);
    writer.flush();
  }

  // If there is a string to send to the server
  static boolean isQueryPended() {
    return !queryQueue.isEmpty();
  }

  // Next string to send to the server
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

  // Pend a string to send to the server
  public static void pendQuery(String query) {
    queryQueue.add(query);
  }

  // There was a connection check signal sent from the server just before
  static void setConnectionCheckTimer() {
    lastConnectionCheckTime = LocalDateTime.now();
  }

  // There was a connection check signal sent to the server just before
  static void setConnectionSendTimer() {
    lastConnectionSendTime = LocalDateTime.now();
  }

  // To send connection check signal to the client periodically
  static boolean shouldSendConnectionSend() {
    return Duration.between(lastConnectionSendTime, LocalDateTime.now()).getSeconds() >= CONNECTION_CHECK_PERIOD;
  }

  // If the connection is not clear for a long time
  static boolean isConnectionCheckTimeout() {
    if (lastConnectionCheckTime != null && Duration.between(lastConnectionCheckTime, LocalDateTime.now())
        .getSeconds() >= CONNECTION_CHECK_TIMEOUT_SECONDS)
      return true;
    return false;
  }

  // Client main process.
  // The basic FSMs are embedded in Player.java and Opponent.java.
  // It manages query pending and polling.
  public static void main(String[] args) {
    Socket socket = null;

    gameBoard = new Board(11, 11);

    // Graphics
    playerIDFrame = new PlayerIDFrame();
    roomSelectFrame = new RoomSelectFrame();
    roomFrame = new RoomFrame();
    gameFrame = new GameFrame();

    try {
      // Connect to the server
      socket = new Socket();
      socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

      reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
      writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
      queryQueue = new LinkedList<String>();

      // First GUI: playerIDFrame to make player name
      playerIDFrame.setVisible(true);

      // Reset timers
      setConnectionCheckTimer();
      setConnectionSendTimer();

      boolean sendPlayerID = false;
      String id = "";
      while (true) {
        if (isQueryPended()) {
          String query = nextQuery();

          // If the client want to make his ID,
          // the query is embedded as
          //
          // "player"
          // "{playerID}"
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
            // A periodic connection check signal from the client
            setConnectionCheckTimer();
            Thread.sleep(10);
          } else if (sendPlayerID) {
            // Send "{playerID}" information to the server
            if (response.equals("success")) {
              // Valid player name
              Player.id = id;
              // Search available rooms automatically
              pendQuery("search");
              break;
            } else if (response.equals("fail")) {
              // Invalid player name
              sendPlayerID = false;
              if (2 <= id.length() && id.length() <= 20)
                playerIDFrame.setPlayerIDErrorMsg("That player ID already exists.");
              else
                playerIDFrame.setPlayerIDErrorMsg("The length should be 2<=len<=20.");
            }
          }
        }

        // Send "connection check" signal to the client periodiccally
        if (shouldSendConnectionSend()) {
          setConnectionSendTimer();
          write("connection check");
        }

        // If connection timeout is activated, then disconnect
        if (isConnectionCheckTimeout()) {
          JOptionPane.showMessageDialog(Client.roomSelectFrame, "Disconnected from the server");
          Thread.sleep(5000);
          write("close");
          System.exit(0);
        }
      }

      // Player ID is successfully created.
      // Now, roomSelectFrame is shown in front
      roomSelectFrame.setBounds(playerIDFrame.getBounds());
      playerIDFrame.setVisible(false);

      // Infinite loop with the server
      while (Player.state != Player.State.EXIT) {
        if (repaintGUI) {
          // Repaint all GUI frames
          GUI.synchronizeFrameBounds();
          GUI.showFrame();
          GUI.repaint();
          repaintGUI = false;
        }

        if (reader.ready()) {
          String response = reader.readLine();
          if (response.equals("connection check")) {
            // Periodic conncection check signal
            setConnectionCheckTimer();
            Thread.sleep(10);
            continue;
          } else if (response.length() >= 8 && response.substring(0, 8).equals("opponent"))
            // Signal with prefix "opponent"
            Opponent.processResponse(response.substring(9));
          else
            // Signal send to me
            Player.processResponse(response);

          repaintGUI = true;
        } else if (isQueryPended()) {
          // If there is any string to send to the server
          String query = nextQuery().strip();
          // Update the FSM in Player
          Player.processQuery(query);
          repaintGUI = true;
        }

        // Send "connection check" signal to the client periodiccally
        if (shouldSendConnectionSend()) {
          setConnectionSendTimer();
          write("connection check");
        }

        // If connection timeout is activated, then disconnect
        if (isConnectionCheckTimeout()) {
          JOptionPane.showMessageDialog(Client.roomSelectFrame, "Disconnected from the server");
          Thread.sleep(5000);
          write("close");
          System.exit(0);
        }

        // Polling 5 seconds after the game terminated
        if (Player.state == Player.State.TERMINATED) {
          if (Duration.between(Player.terminateTime, LocalDateTime.now())
              .getSeconds() >= WAIT_AFTER_TERMINATE) {
            // Go back to the room
            Player.state = Player.State.SEARCH_ROOM;
            Player.inputState = Player.InputState.SEARCH_ROOM;
            Opponent.state = Opponent.State.NONE;
            Opponent.inputState = Opponent.InputState.NONE;
            // And leave the room
            write("leave");
            Thread.sleep(100);
            // After a while (room is successfully deleted), fetch the room info
            write("search");
            repaintGUI = true;
              }
        }

        // Refresh the put stone timeout indicator
        if (Player.state == Player.State.MY_TURN || Player.state == Player.State.NOT_MY_TURN) {
          gameFrame.repaintTimer();
        }

        Thread.sleep(10);
      }
    } catch (IOException ioex) {
      ioex.printStackTrace();
      System.exit(0);
    } catch (InterruptedException iex) {
      try {
        // Try to say goodbye to the server
        write("close");
        if (socket != null && !socket.isClosed()) {
          socket.close();
        }
      } catch (IOException ioex) {
        ioex.printStackTrace();
        System.exit(0);
      }
      iex.printStackTrace();
      System.exit(0);
    } finally {
      try {
        // Try to say goodbye to the server
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
