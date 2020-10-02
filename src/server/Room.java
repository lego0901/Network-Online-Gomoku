/*
 * Room.java
 * Author: Woosung Song
 *
 * Simple information storage class for the player.
 */
package server;

import java.util.LinkedList;

import gomoku.Gomoku;

public class Room {
  // The ID length constraint of the room
	public static final int MIN_ID_LENGTH = 2;
	public static final int MAX_ID_LENGTH = 20;

  // State of the room
	public static enum RoomState {
		WAIT_ROOM, FULL_ROOM, PLAYING,
	}

	public String id;
	public RoomState state;
	public LinkedList<Player> players;
	public Gomoku game;

  // A simple constructor of the class
	public Room(String id) {
		this.id = id;
		players = new LinkedList<Player>();
		state = RoomState.WAIT_ROOM;
	}

  // Check the validity of the id
	public static boolean isValidRoomID(String id) {
		return MIN_ID_LENGTH <= id.length() && id.length() <= MAX_ID_LENGTH;
	}

  // Add a player in the room
	public void addPlayer(Player player) {
		if (players.size() >= 2)
			return; // Full room. Possibility should be checked before
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) == player) {
				return; // No duplicated player allowed
			}
		}
		players.add(player);
		if (isFull()) {
			state = RoomState.FULL_ROOM;
		}
	}

  // Remove player from the room
	public void removePlayer(Player player) {
		int idx = -1;
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) == player) {
				idx = i;
				break;
			}
		}
		if (idx != -1) {
			players.remove(idx);
			state = RoomState.WAIT_ROOM;
		}
	}

  // If the room is empty so that it is destructable
	public boolean isDestructable() {
		return players.size() == 0;
	}

  // If both players are ready to play the game;
  // to avoid race, I used .isReadyOrPlaying, not .isReady.
	public boolean isReady() {
		return isFull() && players.get(0).isReadyOrPlaying() && players.get(1).isReadyOrPlaying();
	}
	
	public boolean isFull() {
		return players.size() == 2;
	}

  // players.get(0): 1st player, .get(1): 2nd player
	public void setTurnsOfPlayers() {
		assert (isFull());
		synchronized (players) {
			for (int turn = 1; turn <= 2; turn++) {
				players.get(turn - 1).turnID = turn;
			}
		}
	}

  // Initialize game before playing
	public void initializeGame() {
		game = new Gomoku(11, 11, 50);
		game.initialize();
		state = RoomState.PLAYING;
	}

  // Put stone if it is possible.
  // The turn information is kept on `game'.
	public boolean putStone(int row, int column) {
		return game.putStone(row, column);
	}

	public void setWinner(String playerID) {
		endGame();
		for (Player player : players) {
			if (player.id.equals(playerID)) {
				game.winner = player.turnID;
				game.terminated = true;
			}
		}
	}

	public void setLoser(String playerID) {
		endGame();
		for (Player player : players) {
			if (player.id.equals(playerID)) {
				game.winner = Gomoku.nextTurn(player.turnID);
				game.terminated = true;
			}
		}
	}

  // Go back to the room (with no players are ready)
	public void endGame() {
		state = RoomState.FULL_ROOM;
	}

  // To use in System.out.println(.)
	@Override
	public String toString() {
		String str = "<" + id;
		if (players.size() > 0) {
			str += ", players:";
			for (Player player : players) {
				str += " " + player.id;
			}
		}
		str += ", " + state;
		str += ">";
		return str;
	}
}
