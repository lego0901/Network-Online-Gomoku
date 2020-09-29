package server;

import java.time.Duration;
import java.time.LocalDateTime;

import server.Room.RoomState;

public class Player {
	public static final int MIN_ID_LENGTH = 2;
	public static final int MAX_ID_LENGTH = 20;
	public static final int MOVE_TIMEOUT_SECONDS = 60;

	public static enum PlayerState {
		SEARCH_ROOM, ENTER_ROOM, READY_ROOM, MY_TURN, NOT_MY_TURN,
	}

	public String id;
	public PlayerState state;
	public ServerThread thread;
	public Room room;

	public int turnID;
	public LocalDateTime lastMoveTime;

	public Player(String id, ServerThread thread) {
		this.id = id;
		this.thread = thread;
		initalize();
	}

	public static boolean isValidPlayerID(String id) {
		return MIN_ID_LENGTH <= id.length() && id.length() <= MAX_ID_LENGTH;
	}

	public void initalize() {
		state = PlayerState.SEARCH_ROOM;
	}

	// before playing game
	public boolean enterRoom(String roomID) {
		room = Server.fetchRoom(roomID);
		if (room != null && room.players.size() < 2) {
			state = PlayerState.ENTER_ROOM;
			room.addPlayer(this);
			return true;
		}
		return false;
	}

	public void leaveRoom() {
		if (room != null) {
			room.removePlayer(this);
			if (room.isDestructable()) {
				Server.eraseRoom(room.id);
			}
		}
		state = PlayerState.SEARCH_ROOM;
		room = null;
	}

	public void ready() {
		if (state == PlayerState.ENTER_ROOM)
			state = PlayerState.READY_ROOM;
	}

	public void cancelReady() {
		if (state == PlayerState.READY_ROOM)
			state = PlayerState.ENTER_ROOM;
	}

	public boolean isMyRoomReady() {
		return room != null && room.isReady();
	}

	// while playing game
	public boolean isMyTurn() {
		if (room == null || room.state != RoomState.PLAYING)
			return false;
		else {
			assert (room.game != null);
			return this == room.players.get(room.game.turn - 1);
		}
	}
	
	public void setTimer() {
		lastMoveTime = LocalDateTime.now();
	}

	public boolean isTimeout() {
		if (Duration.between(lastMoveTime, LocalDateTime.now()).getSeconds() >= MOVE_TIMEOUT_SECONDS)
			return true;
		return false;
	}

	public boolean isMyGameTerminated() {
		return room.game.state == 1;
	}
	
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
