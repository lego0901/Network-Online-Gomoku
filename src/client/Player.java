/*
 * Player.java
 * Author: Woosung Song
 *
 * An FSM managing queries to the server or responses from the server.
 */
package client;

import java.time.LocalDateTime;

import javax.swing.JOptionPane;

public class Player {
  // Player's action states
  static enum State {
    SEARCH_ROOM, ENTER_ROOM, READY_ROOM, MY_TURN, NOT_MY_TURN, TERMINATED, EXIT
  }

  // Query pending state for the client
  static enum InputState {
    OUT_ROOM, CREATE_ROOM, JOIN_ROOM, SEARCH_ROOM, IN_ROOM, BEFORE_GAME, IN_GAME, STONE_GAME, INVALID_MOVE, END_GAME
  }

  // Current player's state
  public static State state = State.SEARCH_ROOM;
  // Current input's state
  public static InputState inputState = InputState.OUT_ROOM;

  public static String id;
  public static String roomID;
  public static int turnID; // 1st player or 2nd player

  // How many responses are left for "search" query
  public static int searchRoomCnt;
  // The "{row column}" format string that will update the game board
  public static String stoneQuery;
  // Putting stone's error message
  public static String putStoneErrorMsgResponse = "";
  // One of "you win", "you lose", "you draw"
  public static String terminateResponse = "";

  // Last put stone time
  public static LocalDateTime lastMoveTime = LocalDateTime.now();
  // Game termination time (to display for 5 seconds)
  public static LocalDateTime terminateTime = LocalDateTime.now();

  // Information that will be displayed after the game terminated
  public static boolean isQueryTimeout = false;
  public static boolean isStoneTimeout = false;
  public static boolean isSurrendered = false;
  public static int putStoneOutOfRangeCnt = 0;

  // A mocking bird function, used for managing the FSM state.
  public static void processQuery(String query) {
    if (query.equals("close")) {
      // Close connection
      Client.write("close");
      state = State.EXIT;
    }

    switch (state) {
      // SEARCH_ROOM: Right after the player is logged on
      case SEARCH_ROOM:
        switch (inputState) {
          case OUT_ROOM:
            if (query.equals("create")) {
              // Available query string: "create\n{roomID}"
              Client.write("create");
              inputState = InputState.CREATE_ROOM;
            } else if (query.equals("join")) {
              // Available query string: "join\n{roomID}"
              Client.write("join");
              inputState = InputState.JOIN_ROOM;
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
              Client.write("search");
              inputState = InputState.SEARCH_ROOM;
              searchRoomCnt = -1;
            } else {
              // invalid query
            }
            break;
          case CREATE_ROOM:
          case JOIN_ROOM:
            // Available query string: "{roomID}"
            Client.write(query);
            roomID = query;
            break;
          default:
            // invalid query
        }
        break;

        // ENTER_ROOM: Right after the player is entered in any room
      case ENTER_ROOM:
        switch (inputState) {
          case IN_ROOM:
            if (query.equals("ready")) {
              // The client is ready in the room
              Client.write("ready");
              state = State.READY_ROOM;
            } else if (query.equals("leave")) {
              // The client is about to leave the room
              Client.write("leave");
              state = State.SEARCH_ROOM;
              inputState = InputState.OUT_ROOM;
            } else {
              // invalid query
            }
            break;
          default:
            // invalid query
        }
        break;

        // READY_ROOM: The player is ready in the room
      case READY_ROOM:
        switch (inputState) {
          case IN_ROOM:
            if (query.equals("cancel")) {
              // Cancel readiness
              Client.write("cancel");
              state = State.ENTER_ROOM;
            } else if (query.equals("leave")) {
              // The client is about to leave the room
              Client.write("leave");
              state = State.SEARCH_ROOM;
              inputState = InputState.OUT_ROOM;
            } else {
              // invalid query
            }
            break;
          default:
            // invalid query
        }
        break;

        // MY_TURN: The player is in a game, and it is his turn
      case MY_TURN:
        switch (inputState) {
          case IN_GAME:
            if (query.equals("stone")) {
              // If the client want to put a stone to the board,
              // the query is embedded as
              //
              // "stone"
              // "{row} {column}"
              Client.write("stone");
              inputState = InputState.STONE_GAME;
            } else if (query.equals("surrender")) {
              // Client want to surrend
              Client.write("surrender");
              isSurrendered = true;
            } else {
              // invalid query
            }
            break;
          case STONE_GAME:
            // "{row} {column}" string after "stone" queried
            Client.write(query);
            stoneQuery = query;
            break;
          default:
            // invalid query
        }
        break;

        // NOT_MY_TURN: The player is in a game, but it is not his turn
      case NOT_MY_TURN:
        if (query.equals("surrender")) {
          // Client want to surrend
          // "end" is queried right after this
          Client.write(query);
          isSurrendered = true;
        }
        break;

        // TERMINATED: (Only in client state) Game is terminated, displaying the final board
      case TERMINATED:
        if (query.equals("leave")) {
          // The client is about to leave the room
          Client.write("leave");
          state = State.SEARCH_ROOM;
          inputState = InputState.OUT_ROOM;
        }
        break;

        // EXIT: (Only in client state) Client is about to exit the program window
      case EXIT:
        // invalid query
        break;
    }
  }

  // Based on the FSM state, it processes the response string from the server
  public static void processResponse(String response) {
    if (response.equals("query timeout")) {
      // Disconnected from the server or idle for too much time
      isQueryTimeout = true;
      state = State.EXIT;
    }

    switch (state) {
      // SEARCH_ROOM: Right after the player is logged on
      case SEARCH_ROOM:
        switch (inputState) {
          case CREATE_ROOM:
          case JOIN_ROOM:
            // after "create\n{roomID}" or "join\n{roomID}" is queried
            if (response.equals("success")) {
              // Now you entered your room
              state = State.ENTER_ROOM;
              inputState = InputState.IN_ROOM;
              Client.roomSelectFrame.setVisible(false);
            } else if (response.equals("fail")) {
              // Invalid room to enter
              state = State.SEARCH_ROOM;
              inputState = InputState.OUT_ROOM;
              if (2 <= roomID.length() && roomID.length() <= 20)
                JOptionPane.showMessageDialog(Client.roomSelectFrame, "Invalid room.");
              else
                JOptionPane.showMessageDialog(Client.roomSelectFrame, "Room ID should be 2<=|len|<=20.");
              roomID = "";
            }
            break;
          case SEARCH_ROOM:
            // the server tells all rooms after "search" is queried
            //
            // "success"           - searchRoomCnt = -2
            // "{# of the rooms}"  - searchRoomCnt = -1 -> {# of rooms}
            // "(wait) roomID 1"   - decrease searchRoomCnt one by one
            // "(wait) roomID 2"   - ...
            // "..."
            // "(full) roomID 1"
            // "(full) roomID 2"
            // "..."
            if (searchRoomCnt == -1) {
              if (response.equals("success")) {
                searchRoomCnt = -2;
                Client.roomSelectFrame.initializeRoomLabels();
              } else if (response.equals("fail")) {
                inputState = InputState.OUT_ROOM;
              }
            } else if (searchRoomCnt == -2) {
              searchRoomCnt = Integer.parseInt(response);
              if (searchRoomCnt == 0) {
                state = State.SEARCH_ROOM;
                inputState = InputState.OUT_ROOM;
              }
            } else {
              Client.roomSelectFrame.addRoomLabel(response);
              Client.roomSelectFrame.repaint();
              searchRoomCnt--;
              if (searchRoomCnt == 0) {
                state = State.SEARCH_ROOM;
                inputState = InputState.OUT_ROOM;
              }
            }
            break;
          default:
            // invalid response
        }
        break;

        // ENTER_ROOM: Right after the player is entered in any room
      case ENTER_ROOM:
        // Actually, no meaningful response on this state
        break;

        // READY_ROOM: The player is ready in the room
      case READY_ROOM:
        switch (inputState) {
          case IN_ROOM:
            if (response.equals("play")) {
              // The game begins; need to initiate some objects
              inputState = InputState.BEFORE_GAME;
            }
            break;
          case BEFORE_GAME:
            if (response.equals("your turn")) {
              // Begins with my turn
              state = State.MY_TURN;
              inputState = InputState.IN_GAME;
              Opponent.state = Opponent.State.NOT_MY_TURN;
              Opponent.inputState = Opponent.InputState.IN_GAME;

              turnID = 1;
              Opponent.turnID = 2;
              Client.gameBoard.begin(id, Opponent.id);
              Client.gameFrame.setPlayerTurn();
            } else if (response.equals("not your turn")) {
              // Begins with opponent's turn
              state = State.NOT_MY_TURN;
              inputState = InputState.IN_GAME;
              Opponent.state = Opponent.State.MY_TURN;
              Opponent.inputState = Opponent.InputState.IN_GAME;

              turnID = 2;
              Opponent.turnID = 1;
              Client.gameBoard.begin(Opponent.id, id);
              Client.gameFrame.setOpponentTurn();
            }

            // Initiates all information related to the game
            isQueryTimeout = false;
            isStoneTimeout = false;
            isSurrendered = false;
            putStoneOutOfRangeCnt = 0;
            putStoneErrorMsgResponse = "";
            Opponent.isQueryTimeout = false;
            Opponent.isStoneTimeout = false;
            Opponent.isSurrendered = false;
            Opponent.putStoneOutOfRangeCnt = 0;

            // GUI repaint
            Client.gameFrame.synchronizeBoard(Client.gameBoard);
            Client.gameFrame.setPlayerID(id);
            Client.gameFrame.setOpponentID(Opponent.id);
            Client.gameFrame.setTimer();
            Client.gameFrame.setPutStoneErrorMsg("");
            break;
          default:
            // invalid response
        }

        // MY_TURN: The player is in a game, and it is his turn
      case MY_TURN:
        switch (inputState) {
          case IN_GAME:
            if (response.equals("stone timeout")) {
              // Stayed to much time not putting any stone
              isStoneTimeout = true;
            } else if (response.equals("end")) {
              // Game ended
              state = State.TERMINATED;
              inputState = InputState.END_GAME;
              if (Opponent.state != Opponent.State.NONE) {
                // Check if game ended because the opponent leaved the game
                Opponent.state = Opponent.State.TERMINATED;
                Opponent.inputState = Opponent.InputState.IN_ROOM;
              }
              terminateTime = LocalDateTime.now();
            } else {
              // invalid response
            }
            break;
          case STONE_GAME:
            // After the query
            //
            // "stone"
            // "{row} {column}"
            if (response.equals("success")) {
              // It is valid position to put a stone
              state = State.NOT_MY_TURN;
              inputState = InputState.IN_GAME;
              Opponent.state = Opponent.State.MY_TURN;
              putStoneErrorMsgResponse = "";

              // Update GUI
              int[] rc = Client.parseCoordinates(stoneQuery);
              Client.gameBoard.board[rc[0]][rc[1]] = turnID;
              Client.gameFrame.putStone(rc[0], rc[1], turnID);
              Client.gameFrame.setTimer();
              Client.gameFrame.setPutStoneErrorMsg("");
            } else if (response.equals("fail")) {
              // May be the signal is somehow weird
              inputState = InputState.IN_GAME;
            } else if (response.equals("invalid move")) {
              // Pending to receive the reason
              inputState = InputState.INVALID_MOVE;
            } else if (response.equals("stone timeout")) {
              // The response "stone timeout" can be receive at any timing
              isStoneTimeout = true;
            } else if (response.equals("end")) {
              // The response "end" can be receive at any timing
              state = State.TERMINATED;
              inputState = InputState.END_GAME;
              if (Opponent.state != Opponent.State.NONE) {
                // Check if game ended because the opponent leaved the game
                Opponent.state = Opponent.State.TERMINATED;
                Opponent.inputState = Opponent.InputState.IN_ROOM;
              }
              terminateTime = LocalDateTime.now();
            } else {
              // invalid response
            }
            break;
          case INVALID_MOVE:
            if (response.equals("stone timeout")) {
              // The response "stone timeout" can be receive at any timing
              isStoneTimeout = true;
            } else if (response.equals("end")) {
              // The response "end" can be receive at any timing
              state = State.TERMINATED;
              inputState = InputState.END_GAME;
              if (Opponent.state != Opponent.State.NONE) {
                // Check if game ended because the opponent leaved the game
                Opponent.state = Opponent.State.TERMINATED;
                Opponent.inputState = Opponent.InputState.IN_ROOM;
              }
              terminateTime = LocalDateTime.now();
            } else {
              // "invalid move"
              // "{reason}"       <- response
              putStoneErrorMsgResponse = response;
              inputState = InputState.IN_GAME;

              if (putStoneErrorMsgResponse.equals("Stone out of range (error count = 1)")) {
                putStoneOutOfRangeCnt = 1;
              } else if (putStoneErrorMsgResponse.equals("Stone out of range (error count = 2)")) {
                putStoneOutOfRangeCnt = 2;
              }
            }
            break;
          default:
            // invalid response
        }
        break;

        // NOT_MY_TURN: The player is in a game, but it is not his turn
      case NOT_MY_TURN:
        if (response.equals("end")) {
          // The response "end" can be receive at any timing
          state = State.TERMINATED;
          inputState = InputState.END_GAME;
          if (Opponent.state != Opponent.State.NONE) {
            // Check if game ended because the opponent leaved the game
            Opponent.state = Opponent.State.TERMINATED;
            Opponent.inputState = Opponent.InputState.NONE;
          }
          terminateTime = LocalDateTime.now();
        } else {
          // invalid response
        }
        break;

        // TERMINATED: (Only in client state) Game is terminated, displaying the final board
      case TERMINATED:
        // you win, you lose, you draw
        terminateResponse = response;
        if (terminateResponse.equals("you win")) {
          Client.gameFrame.setPlayerWin();
        } else if (terminateResponse.equals("you lose")) {
          Client.gameFrame.setPlayerLose();
        } else {
          Client.gameFrame.setPlayerDraw();
          Client.gameFrame.setTerminateReason("You have put 50 stones");
        }

        // Display the termination reason
        if (isStoneTimeout)
          Client.gameFrame.setTerminateReason("Timeout on stone");
        if (Opponent.isStoneTimeout)
          Client.gameFrame.setTerminateReason("Opponent's timeout on stone");
        if (isSurrendered)
          Client.gameFrame.setTerminateReason("You surrendered");
        if (Opponent.isSurrendered)
          Client.gameFrame.setTerminateReason("Opponent surrendered");
        if (putStoneOutOfRangeCnt >= 2)
          Client.gameFrame.setTerminateReason("You put stones out of board x2");
        if (Opponent.putStoneOutOfRangeCnt >= 2)
          Client.gameFrame.setTerminateReason("Opponent put stones out of board x2");
        if (Opponent.state == Opponent.State.NONE)
          Client.gameFrame.setTerminateReason("Opponent disconnected");
        break;

        // EXIT: (Only in client state) Client is about to exit the program window
      case EXIT:
        // Maybe the response is "bye"
        break;
    }
  }
}
