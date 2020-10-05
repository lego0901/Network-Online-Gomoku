/*
 * Player.java
 * Author: Woosung Song
 *
 * Simple information storage class for the player.
 */
package server;

import java.time.Duration;
import java.time.LocalDateTime;

import gomoku.Gomoku;
import server.Room.RoomState;

public class Player {
  // The ID length constraint of the player
  public static final int MIN_ID_LENGTH = 2;
  public static final int MAX_ID_LENGTH = 20;
  // Stone timeout: 60 seconds
  public static final int MOVE_TIMEOUT_SECONDS = 60;

  // State of the player
  public static enum PlayerState {
    SEARCH_ROOM, ENTER_ROOM, READY_ROOM, MY_TURN, NOT_MY_TURN,
  }

  public String id;
  public PlayerState state;
  public ServerThread thread;
  public Room room;

  // turnID: If the player puts 1st(black) or 2nd(white) stone
  public int turnID;
  public LocalDateTime lastMoveTime;

  // A simple constructor of the class
  public Player(String id, ServerThread thread) {
    this.id = id;
    this.thread = thread;
    state = PlayerState.SEARCH_ROOM;
  }

  // Check the validity of the id
  public static boolean isValidPlayerID(String id) {
    return MIN_ID_LENGTH <= id.length() && id.length() <= MAX_ID_LENGTH;
  }

  // Methods BEFORE playing game
  // Player create and enter the room name roomID if possible
  public boolean createAndEnterRoom(String roomID) {
    if (Server.addRoom(roomID)) {
      // If successfully created a room, then enter that
      room = Server.fetchRoom(roomID);
      state = PlayerState.ENTER_ROOM;
      room.addPlayer(this);
      // Make clients to refresh the search result
      Server.roomInfoVersion++;
      return true;
    }
    return false;
  }

  // Player enter the room name roomID if possible
  public boolean enterRoom(String roomID) {
    room = Server.fetchRoom(roomID);
    if (room != null && room.players.size() < 2) {
      // Existing name and room is not full => OK
      state = PlayerState.ENTER_ROOM;
      room.addPlayer(this);
      // Make clients to refresh the search result
      Server.roomInfoVersion++;
      return true;
    }
    return false;
  }

  // Player leaves the room
  public void leaveRoom() {
    if (room != null) {
      if (room.state == RoomState.PLAYING) {
        // Leaves room whiling playing.. loser
        setLoser();
      }
      room.removePlayer(this);
      if (room.isDestructable()) {
        // If there is nobody in that room, then erase it
        Server.eraseRoom(room.id);
      }
      // Make clients to refresh the search result
      Server.roomInfoVersion++;
    }
    state = PlayerState.SEARCH_ROOM;
    room = null;
  }

  // Fetch opponentID from the room class
  public String opponentID() {
    if (room != null && room.isFull()) {
      if (room.players.get(0) == this)
        return room.players.get(1).id;
      else
        return room.players.get(0).id;
    }
    return "";
  }

  public void ready() {
    if (state == PlayerState.ENTER_ROOM)
      state = PlayerState.READY_ROOM;
  }

  public void cancelReady() {
    if (state == PlayerState.READY_ROOM)
      state = PlayerState.ENTER_ROOM;
  }

  // To check if it is okay to start a game
  // by checking isReadyOrPlaying() for both players in the room
  public boolean isReadyOrPlaying() {
    return state == PlayerState.READY_ROOM || state == PlayerState.MY_TURN || state == PlayerState.NOT_MY_TURN;
  }

  // It the game startable
  public boolean isMyRoomReady() {
    return room != null && room.isReady();
  }

  // Initialize the game class of the room
  public void initializeGame() {
    room.setTurnsOfPlayers(); // 1st, 2nd are fixed
    if (turnID == 1) {
      // The first player initialize the game
      room.initializeGame();
      state = PlayerState.MY_TURN;
      // Put stone timer for the first player
      setTimer();
    } else {
      state = PlayerState.NOT_MY_TURN;
    }
  }

  // Methods WHILE playing game
  // Check if it is my client's turn
  public boolean isMyTurn() {
    if (room == null || room.state != RoomState.PLAYING)
      return false;
    else {
      assert (room.game != null);
      return turnID == room.game.turn;
    }
  }

  // Put stone if it is possible
  public boolean putStone(int row, int column) {
    if (!isMyTurn())
      return false;
    return room.putStone(row, column);
  }

  // Error message while putting a stone by me
  public String putStoneErrorMsg() {
    return room.game.putStoneErrorMsg;
  }

  // Yes, my client is a winner
  public void setWinner() {
    room.setWinner(id);
  }

  // No, my client is a lower
  public void setLoser() {
    room.setLoser(id);
  }

  // Set put stone time to estimate the timeout
  public void setTimer() {
    lastMoveTime = LocalDateTime.now();
  }

  // Estimate the timeout
  public boolean isTimeout() {
    if (Duration.between(lastMoveTime, LocalDateTime.now()).getSeconds() >= MOVE_TIMEOUT_SECONDS)
      return true;
    return false;
  }

  // Is the game ended?
  public boolean isMyGameTerminated() {
    return room.game != null && room.game.terminated;
  }

  // Update player's state and the room
  public void endGame() {
    state = PlayerState.ENTER_ROOM;
    if (turnID == 1) {
      // No racing, only 1st player terminates the room
      room.endGame();
    }
  }

  // Methods AFTER playing game
  public boolean isWinner() {
    return room.game != null && room.game.winner == turnID;
  }

  public boolean isLoser() {
    return room.game != null && room.game.winner == Gomoku.nextTurn(turnID);
  }

  public boolean isDraw() {
    return room.game != null && room.game.winner == -1;
  }

  // To use System.out.println(.)
  @Override
  public String toString() {
    String str = "<" + id;
    if (room != null) {
      str += ", in " + room.id;
    }
    str += ", " + state;
    str += ">";
    return str;
  }
}
