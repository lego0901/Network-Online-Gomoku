package server;

import java.util.LinkedList;

import gomoku.Gomoku;

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
		if (players.size() >= 2)
			return;
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
		return players.size() == 2 && players.get(0).isPlayingOrReady() && players.get(1).isPlayingOrReady();
	}

	public void setTurnsOfPlayers() {
		assert (players.size() == 2);
		synchronized (players) {
			for (int turn = 1; turn <= 2; turn++) {
				players.get(turn - 1).turnID = turn;
				System.out.println("Player " + players.get(turn - 1).id + "'s turn is " + turn);
			}
		}
	}

	public void initializeGame() {
		game = new Gomoku(11, 11, 50);
		game.initialize();
		state = RoomState.PLAYING;
	}

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

	public void endGame() {
		state = RoomState.FULL_ROOM;
	}

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
