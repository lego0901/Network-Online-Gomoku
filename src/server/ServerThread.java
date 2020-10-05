/*
 * ServerThread.java
 * Author: Woosung Song
 *
 * Main server thread for each client connection.
 * It manages a simple FSM to process queries and responsees.
 */
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
  // Longest idle time for each client
  public static final int QUERY_TIMEOUT_SECONDS = 120;
  // Check if the client is connected to the server for each second
  public static final int CONNECTION_CHECK_PERIOD = 1;
  // If client is disconnected for longer than 10s, then treat it as permanent
  public static final int CONNECTION_CHECK_TIMEOUT_SECONDS = 10;

  // Query pending state for the client
  public static enum InputState {
    DEFAULT, PLAYER, CREATE, JOIN, STONE;
  }

  Socket socket = null;
  Player player = null;

  // ServerThread of the opponent in the room or the game
  ServerThread opponentThread = null;

  // Reader to the client, writer from the client
  BufferedReader reader;
  PrintWriter writer;

  // ServerThread's current input state
  InputState inputState = InputState.DEFAULT;

  // To synchronize roomInfoVersion with Server
  int roomInfoVersion;

  // To estimate the idle time
  LocalDateTime lastQueryTime;
  // Last time that connection check signal received from the client
  LocalDateTime lastConnectionCheckTime;
  // Last time that connection check signal sent to the client
  LocalDateTime lastConnectionSendTime;

  // Simple constructor of the class.
  // The body part is in Thread.run procedure.
  public ServerThread(Socket socket) {
    this.socket = socket;
    lastQueryTime = LocalDateTime.now();
  }

  // Print out the server thread status
  void debug(String str) {
    if (Server.printLog)
      System.out.println(str);
  }

  // Send a string to the client and flush
  void write(String str) {
    writer.println(str);
    writer.flush();
  }

  // Send a string to the opponent and flush
  void writeOpponent(String str) {
    if (opponentThread != null) {
      opponentThread.write("opponent " + str);
    }
  }

  // Say goodbye to your opponent. He is leaving
  void disconnectMeFromOpponentThread() {
    if (opponentThread != null) {
      opponentThread.opponentThread = null;
    }
  }

  // There was a string sent from the client just before
  void setQueryTimer() {
    lastQueryTime = LocalDateTime.now();
  }

  // There was a connection check signal sent from the client just before
  void setConnectionCheckTimer() {
    lastConnectionCheckTime = LocalDateTime.now();
  }

  // There was a connection check signal sent to the client just before
  void setConnectionSendTimer() {
    lastConnectionSendTime = LocalDateTime.now();
  }

  // To send connection check signal to the client periodically
  boolean shouldSendConnectionSend() {
    return Duration.between(lastConnectionSendTime, LocalDateTime.now()).getSeconds() >= CONNECTION_CHECK_PERIOD;
  }

  // Either the client is idle for a too long time, or connection is not clear
  boolean isThreadTimeout() {
    if (Duration.between(lastQueryTime, LocalDateTime.now()).getSeconds() >= QUERY_TIMEOUT_SECONDS)
      return true;
    if (lastConnectionCheckTime != null && Duration.between(lastConnectionCheckTime, LocalDateTime.now())
        .getSeconds() >= CONNECTION_CHECK_TIMEOUT_SECONDS)
      return true;
    return false;
  }

  // To use System.out.println(.) in Server.java
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

  // Manage an FSM communcating with the client
  public void run() {
    try {
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
      writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

      boolean closeConnection = false;

      // Reset timers
      setQueryTimer();
      setConnectionCheckTimer();
      setConnectionSendTimer();

      // Get a playerID from the client
      while (player == null && !closeConnection) {
        if (reader.ready()) {
          String query = reader.readLine().strip();

          // A periodic connection check signal from the client
          if (query.equals("connection check")) {
            setConnectionCheckTimer();
            sleep(10);
            continue;
          }

          // Process the query string along to the FSM state
          switch (inputState) {
            case DEFAULT:
              if (query.equals("close")) {
                // Close connection
                closeConnection = true;
                break;
              } else if (query.equals("player")) {
                // For example, "player\nwoosung" can be queried
                // If the current string is "player", then the next string is playerID
                inputState = InputState.PLAYER;
              } else {
                debug("Invalid query received");
                write("invalid");
              }
              break;
            case PLAYER:
              // The next string of "player"
              String playerID = query;
              if (Server.addPlayer(playerID, this)) {
                // if valid id and is not a duplicate
                inputState = InputState.DEFAULT;
                player = Server.fetchPlayer(playerID);
                debug("Player " + playerID + " successfully created!");
                write("success");
              } else {
                // If not valid ID or is a duplicate
                inputState = InputState.DEFAULT;
                debug("Player " + playerID + " is invalid or duplicated");
                write("fail");
              }
              break;
            default:
              // impossible
          }

          // Indicate that the client is not idle
          setQueryTimer();
          // Debug the server state
          Server.debug();
        }

        // Send "connection check" signal to the client periodiccally
        if (shouldSendConnectionSend()) {
          setConnectionSendTimer();
          write("connection check");
        }

        // If any timeout is activated, then disconnect
        if (isThreadTimeout()) {
          debug("Client query timeout");
          write("query timeout");
          writeOpponent("query timeout");
          closeConnection = true;
        }

        // Not to be buried in the while loop; sleep
        sleep(10);
      }

      // Infinite loop with the client
      while (!closeConnection) {
        // If there is any string sent from the client
        if (reader.ready()) {
          String query = reader.readLine().strip();

          if (query.equals("close")) {
            // Bye client
            closeConnection = true;
            break;
          } else if (query.equals("connection check")) {
            // Periodic conncection check signal
            setConnectionCheckTimer();
            sleep(10);
            continue;
          }

          // Debug and indicate that client is not idle
          Server.debug();
          setQueryTimer();

          switch (player.state) {
            // SEARCH_ROOM: Right after the player is logged on
            case SEARCH_ROOM:
              switch (inputState) {
                case DEFAULT:
                  if (query.equals("create")) {
                    // Available query string: "create\n{roomID}"
                    inputState = InputState.CREATE;
                  } else if (query.equals("join")) {
                    // Available query string: "join\n{roomID}"
                    inputState = InputState.JOIN;
                  } else if (query.equals("search")) {
                    // If the client want to know about the rooms information,
                    // the server tells all rooms with the below format;
                    //
                    // "success"
                    // "{# of the rooms}"
                    // "(wait) roomID 1"
                    // "(wait) roomID 2"
                    // "..."
                    // "(full) roomID 1"
                    // "(full) roomID 2"
                    // "..."
                    write("success");
                    write("" + Server.rooms.size());

                    // print waiting room first
                    for (Room room : Server.rooms) {
                      if (!room.isFull()) {
                        write("(wait) " + room.id);
                      }
                    }

                    // print full room after
                    for (Room room : Server.rooms) {
                      if (room.isFull()) {
                        write("(full) " + room.id);
                      }
                    }
                  } else {
                    debug("Invalid query received");
                    write("invalid");
                  }
                  break;
                case CREATE:
                  // Available query string: "create\nroomID". "roomID" part
                  String roomCreateID = query;
                  inputState = InputState.DEFAULT;
                  if (Server.fetchRoom(roomCreateID) == null) {
                    // If there is no duplicated room ID
                    if (player.createAndEnterRoom(roomCreateID)) {
                      // Valid and not a duplicate; Create and enter that room
                      debug("Player " + player.id + " created and joined the room " + roomCreateID);
                      write("success");
                    } else {
                      // Invalid room
                      debug("Room " + roomCreateID + " is invalid");
                      write("fail");
                    }
                  } else {
                    // Duplicated
                    debug("Room " + roomCreateID + " is duplicated");
                    write("fail");
                  }
                  Server.debug();
                  break;
                case JOIN:
                  // Available query string: "join\nroomID". "roomID" part
                  String roomJoinID = query;
                  inputState = InputState.DEFAULT;
                  if (Server.fetchRoom(roomJoinID) != null) {
                    // OK. Existing room
                    if (player.enterRoom(roomJoinID)) {
                      // Able to join the room
                      opponentThread = Server.fetchThread(player.opponentID());
                      opponentThread.opponentThread = this;

                      // Enter the room
                      debug("Player " + player.id + " joined the room " + roomJoinID);
                      write("success");
                      // Send the opponent information on that room
                      write("opponent join");
                      write("opponent " + opponentThread.player.id);
                      if (opponentThread.player.state == PlayerState.READY_ROOM) {
                        write("opponent ready");
                      }
                      // Tell the opponent client about my information
                      writeOpponent("join");
                      writeOpponent(player.id);
                    } else {
                      // Not able to join the room
                      debug("Room " + roomJoinID + " is now full");
                      write("fail");
                    }
                  } else {
                    // roomJoinID does not exist
                    debug("Room " + roomJoinID + " is not found");
                    write("fail");
                  }
                  Server.debug();
                  break;
                default:
                  // impossible
              }
              break;

              // ENTER_ROOM: Right after the player is entered in any room
            case ENTER_ROOM:
              if (query.equals("ready")) {
                // The client is ready in the room
                debug("Player " + player.id + " is now ready");
                player.ready();
                write("success");
                // Tell opponent that I am ready
                writeOpponent("ready");
              } else if (query.equals("leave")) {
                // The client is about to leave the room
                player.leaveRoom();
                debug("Player " + player.id + " leaved the room");
                write("success");
                // Tell opponent that I am leaving
                writeOpponent("leave");
                disconnectMeFromOpponentThread();
              } else {
                debug("Invalid query received");
                write("invalid");
              }
              break;

              // READY_ROOM: The player is ready in the room
            case READY_ROOM:
              if (query.equals("cancel")) {
                // Cancel readiness
                player.cancelReady();
                debug("Player " + player.id + " isn't ready");
                write("success");
                // Tell opponent that I am not ready
                writeOpponent("cancel");
              } else if (query.equals("leave")) {
                // The client is about to leave the room
                player.leaveRoom();
                debug("Player " + player.id + " leaved the room");
                write("success");
                // Tell opponent that I am leaving
                writeOpponent("leave");
                disconnectMeFromOpponentThread();
              } else {
                debug("Invalid query received");
                write("invalid");
              }
              break;

              // MY_TURN: The player is in a game, and it is his turn
            case MY_TURN:
              switch (inputState) {
                case DEFAULT:
                  if (query.equals("stone")) {
                    // If the client want to put a stone to the board,
                    // the query is embedded as
                    //
                    // "stone"
                    // "{row} {column}"
                    inputState = InputState.STONE;
                  } else if (query.equals("surrender")) {
                    // Client want to surrend
                    player.setLoser();
                    debug("Surrender by player " + player.id);
                    write("success");
                    // Tell opponent that I surrend
                    writeOpponent("surrender");
                  } else {
                    debug("Invalid query received");
                    write("invalid");
                  }
                  break;
                case STONE:
                  // "{row} {column}" string after "stone" queried
                  String coordinatesString = query;
                  inputState = InputState.DEFAULT;
                  int[] parsedCoordinates = parseCoordinates(coordinatesString);
                  if (parsedCoordinates == null) {
                    debug("Player " + player.id + " invalid input received");
                    write("fail");
                    continue;
                  }

                  int row = parsedCoordinates[0], column = parsedCoordinates[1];
                  if (player.putStone(row, column)) {
                    // Successfully put stone
                    debug("Player " + player.id + " put stone on (" + row + ", " + column + ")");
                    write("success");
                    // Tell opponent that I put stone
                    writeOpponent("stone");
                    writeOpponent("" + row + " " + column);
                  } else {
                    // Error occured during putting stone
                    debug("Player " + player.id + " cannot put stone on (" + row + ", " + column + ")");
                    debug("Reason: " + player.putStoneErrorMsg());
                    write("invalid move");
                    write(player.putStoneErrorMsg());
                    writeOpponent(player.putStoneErrorMsg());
                  }
                  debug(player.room.game.toString());
                  Server.debug();
                  break;
                default:
                  // impossible
              }
              break;

              // NOT_MY_TURN: The player is in a game, but it is not his turn
            case NOT_MY_TURN:
              if (query.equals("surrender")) {
                // Client want to surrend
                player.setLoser();
                debug("Surrender by player " + player.id);
                write("success");
                // Tell opponent that I surrend
                writeOpponent("surrender");
              } else {
                debug("Invalid query received");
                write("invalid");
              }
              break;
          }
        } else {
          // Polling processes
          switch (player.state) {
            // SEARCH_ROOM: Right after the player is logged on
            case SEARCH_ROOM:
              // Room information is changed
              if (inputState == InputState.DEFAULT && Server.roomInfoVersion != roomInfoVersion) {
                roomInfoVersion = Server.roomInfoVersion;
                write("change");
              }

            // READY_ROOM: The player is ready in the room
            case READY_ROOM:
              if (player.isMyRoomReady()) {
                // If both player and opponent are ready, then initiate a game
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

            // MY_TURN: The player is in a game, and it is his turn
            case MY_TURN:
              if (!player.isMyTurn()) {
                // If game's turn is changed (by the opponentThread), then change the state
                player.state = PlayerState.NOT_MY_TURN;
                write("not your turn");
              } else if (player.isTimeout()) {
                // Put stone timeout for 60 seconds
                player.setLoser();
                debug("Stone timeout for player " + player.id);
                write("stone timeout");
                writeOpponent("stone timeout");
              } else if (player.isMyGameTerminated()) {
                // If game is ended
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

            // NOT_MY_TURN: The player is in a game, but it is not his turn
            case NOT_MY_TURN:
              // Client is allowed to be idle during this state
              setQueryTimer();
              if (player.isMyTurn()) {
                // If game's turn is changed (by the opponentThread), then change the state
                player.setTimer();
                player.state = PlayerState.MY_TURN;
                write("your turn");
              } else if (player.isMyGameTerminated()) {
                // If game is ended
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

        // Send "connection check" signal to the client periodiccally
        if (shouldSendConnectionSend()) {
          setConnectionSendTimer();
          write("connection check");
        }

        // If any timeout is activated, then disconnect
        if (isThreadTimeout()) {
          closeConnection = true;
          debug("Client query timeout");
          write("query timeout");
          writeOpponent("query timeout");
        }

        // Not to be buried in the while loop; sleep
        sleep(10);
      }

      write("bye");
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    } finally {
      debug("Try to close");
      // Try to send a disconnection signal to the opponent client
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
