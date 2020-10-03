/*
 * Opponent.java
 * Author: Woosung Song
 *
 * An FSM managing response with prefix "opponent" from the server.
 */
package client;

public class Opponent {
  // Opponent's action states
  static enum State {
    NONE, ENTER_ROOM, READY_ROOM, MY_TURN, NOT_MY_TURN, TERMINATED
  }

  // Response pending state for the opponent
  static enum InputState {
    NONE, JOIN_ROOM, IN_ROOM, IN_GAME, STONE_GAME
  }

  // Current opponent's state
  public static State state = State.NONE;
  // Current input's state
  public static InputState inputState = InputState.NONE;

  public static String id;
  public static int turnID; // 1st player or 2nd player

  // Information that will be displayed after the game terminated
  public static boolean isQueryTimeout = false;
  public static boolean isStoneTimeout = false;
  public static boolean isSurrendered = false;
  public static int putStoneOutOfRangeCnt = 0;

  // Based on the FSM state, it processes the response string from the server
  public static void processResponse(String response) {
    if (response.equals("query timeout")) {
      // Disconnected from the server or idle for too much time
      isQueryTimeout = true;
      state = State.NONE;
      inputState = InputState.NONE;
    } else if (response.equals("leave")) {
      // If the opponent leaves
      state = State.NONE;
      inputState = InputState.NONE;
    }

    switch (state) {
      // NONE: No opponent for now
      case NONE:
        switch (inputState) {
          case NONE:
            if (response.equals("join")) {
              // "join"         <- response
              // "{opponentID}"
              inputState = InputState.JOIN_ROOM;
            }
            break;
          case JOIN_ROOM:
            // "join"
            // "{opponentID}" <- response
            id = response;
            state = State.ENTER_ROOM;
            inputState = InputState.IN_ROOM;
            break;
          default:
            // invalid response
        }
        break;

        // ENTER_ROOM: Right after the opponent is entered in any room
      case ENTER_ROOM:
        switch (inputState) {
          case IN_ROOM:
            if (response.equals("ready")) {
              // The opponent is ready in the room
              state = State.READY_ROOM;
            }
            break;
          default:
            // invalid response
        }
        break;

        // READY_ROOM: The opponent is ready in the room
      case READY_ROOM:
        switch (inputState) {
          case IN_ROOM:
            if (response.equals("cancel")) {
              // The opponent is not ready in the room
              state = State.ENTER_ROOM;
            }
            break;
          default:
            // invalid response
        }
        break;

        // MY_TURN: The opponent is in a game, and it is his turn
      case MY_TURN:
        switch (inputState) {
          case IN_GAME:
            if (response.equals("stone")) {
              // "stone"          <- response
              // "{row} {column}"
              inputState = InputState.STONE_GAME;
            } else if (response.equals("surrender")) {
              // If the opponent surrends
              isSurrendered = true;
            } else if (response.equals("stone timeout")) {
              // If the opponent passed too much time after putting a stone
              isStoneTimeout = true;
            } else if (response.equals("Stone out of range (error count = 1)")) {
              putStoneOutOfRangeCnt = 1;
            } else if (response.equals("Stone out of range (error count = 2)")) {
              // "end" will be responsed right after this
              putStoneOutOfRangeCnt = 2;
            } else {
              // invalid response
            }
            break;
          case STONE_GAME:
            // "stone"
            // "{row} {column}" <- response
            int[] rc = Client.parseCoordinates(response);
            Client.gameBoard.board[rc[0]][rc[1]] = turnID;

            state = State.NOT_MY_TURN;
            inputState = InputState.IN_GAME;
            Player.state = Player.State.MY_TURN;

            // Update GUI
            Client.gameFrame.putStone(rc[0], rc[1], turnID);
            Client.gameFrame.setTimer();
            break;
          default:
            // invalid response
        }
        break;

        // NOT_MY_TURN: The opponent is in a game, but it is not his turn
      case NOT_MY_TURN:
        if (response.equals("surrender")) {
          // The player surrends
          isSurrendered = true;
        } else {
          // invalid response
        }
        break;

        // TERMINATED: Game is terminated, displaying the final board
      case TERMINATED:
        // you win, you lose, you draw
        // no need to display opponent's one
        break;
    }
  }
}
