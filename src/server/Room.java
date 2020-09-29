package server;

import java.util.LinkedList;

import gomoku.Gomoku;
import server.Player.PlayerState;

public class Room {
	public static final int MIN_ID_LENGTH = 2;
	public static final int MAX_ID_LENGTH = 20;

	public static enum RoomState {
		WAIT_ROOM, FULL_ROOM, PLAYING,
	}

	public String id;
	public LinkedList<Player> players;
	public RoomState state;
	public Gomoku game;

	public Room(String id) {
		this.id = id;
		initalize();
	}

	public static boolean isValidRoomID(String id) {
		return MIN_ID_LENGTH <= id.length() && id.length() <= MAX_ID_LENGTH;
	}

	public void initalize() {
		players = new LinkedList<Player>();
		state = RoomState.WAIT_ROOM;
	}

	public void addPlayer(Player player) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) == player) {
				return;
			}
		}
		players.add(player);
		if (players.size() == 2) {
			state = RoomState.FULL_ROOM;
		}
	}

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

	public boolean isDestructable() {
		return players.size() == 0;
	}

	public boolean isReady() {
		return players.size() == 2 && players.get(0).state == PlayerState.READY_ROOM
				&& players.get(1).state == PlayerState.READY_ROOM;
	}

	public void setTurnsOfPlayers() {
		assert (players.size() == 2);
		for (int turn = 1; turn <= 2; turn++) {
			players.get(turn - 1).turnID = turn;
		}
	}

	public void setWinner(String playerID) {
		for (Player player : players) {
			if (player.id.equals(playerID)) {
				game.winner = player.turnID;
				game.state = 1;
			}
		}
	}

	public void setLoser(String playerID) {
		for (Player player : players) {
			if (player.id.equals(playerID)) {
				game.winner = Gomoku.nextTurn(player.turnID);
				game.state = 1;
			}
		}
	}
}
