package client;

import java.time.LocalDateTime;

import javax.swing.JOptionPane;

public class Player {
	static enum State {
		SEARCH_ROOM, ENTER_ROOM, READY_ROOM, MY_TURN, NOT_MY_TURN, TERMINATED, EXIT
	}

	static enum InputState {
		OUT_ROOM, CREATE_ROOM, JOIN_ROOM, SEARCH_ROOM, IN_ROOM, BEFORE_GAME, IN_GAME, STONE_GAME, INVALID_MOVE, END_GAME
	}

	public static State state = State.SEARCH_ROOM;
	public static InputState inputState = InputState.OUT_ROOM;

	public static String id;
	public static String roomID;
	public static int searchRoomCnt;
	public static String stoneQuery;
	public static int turnID;
	public static String putStoneErrorMsgResponse = "";
	public static String terminateResponse = "";
	public static LocalDateTime lastMoveTime = LocalDateTime.now();
	public static LocalDateTime terminateTime = LocalDateTime.now();

	public static boolean isQueryTimeout = false;
	public static boolean isStoneTimeout = false;
	public static boolean isSurrendered = false;
	public static int putStoneOutOfRangeCnt = 0;

	public static void processQuery(String query) {
		if (query.equals("close")) {
			Client.write("close");
			state = State.EXIT;
		}

		switch (state) {
		case SEARCH_ROOM:
			switch (inputState) {
			case OUT_ROOM:
				if (query.equals("create")) {
					Client.write("create");
					inputState = InputState.CREATE_ROOM;
				} else if (query.equals("join")) {
					Client.write("join");
					inputState = InputState.JOIN_ROOM;
				} else if (query.equals("search")) {
					Client.write("search");
					inputState = InputState.SEARCH_ROOM;
					searchRoomCnt = -1;
				} else {
					// invalid query
				}
				break;
			case CREATE_ROOM:
			case JOIN_ROOM:
				Client.write(query);
				roomID = query;
				break;
			default:
				// invalid query
			}
			break;
		case ENTER_ROOM:
			switch (inputState) {
			case IN_ROOM:
				if (query.equals("ready")) {
					Client.write("ready");
					state = State.READY_ROOM;
				} else if (query.equals("leave")) {
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
		case READY_ROOM:
			switch (inputState) {
			case IN_ROOM:
				if (query.equals("cancel")) {
					Client.write("cancel");
					state = State.ENTER_ROOM;
				} else if (query.equals("leave")) {
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
		case MY_TURN:
			switch (inputState) {
			case IN_GAME:
				if (query.equals("stone")) {
					Client.write("stone");
					inputState = InputState.STONE_GAME;
				} else if (query.equals("surrender")) {
					Client.write("surrender");
					isSurrendered = true;
				} else {
					// invalid query
				}
				break;
			case STONE_GAME:
				Client.write(query);
				stoneQuery = query;
				break;
			default:
				// invalid query
			}
			break;
		case NOT_MY_TURN:
			if (query.equals("surrender")) {
				Client.write(query);
				isSurrendered = true;
			}
			break;
		case TERMINATED:
			if (query.equals("leave")) {
				Client.write("leave");
				state = State.SEARCH_ROOM;
				inputState = InputState.OUT_ROOM;
			}
			break;
		case EXIT:
			// invalid query
			break;
		}
	}

	public static void processResponse(String response) {
		if (response.equals("query timeout")) {
			isQueryTimeout = true;
			state = State.EXIT;
		}

		switch (state) {
		case SEARCH_ROOM:
			switch (inputState) {
			case CREATE_ROOM:
			case JOIN_ROOM:
				if (response.equals("success")) {
					state = State.ENTER_ROOM;
					inputState = InputState.IN_ROOM;
					Client.roomSelectFrame.setVisible(false);
				} else if (response.equals("fail")) {
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
		case ENTER_ROOM:
			break;
		case READY_ROOM:
			switch (inputState) {
			case IN_ROOM:
				if (response.equals("play")) {
					inputState = InputState.BEFORE_GAME;
				}
				break;
			case BEFORE_GAME:
				if (response.equals("your turn")) {
					state = State.MY_TURN;
					inputState = InputState.IN_GAME;
					Opponent.state = Opponent.State.NOT_MY_TURN;
					Opponent.inputState = Opponent.InputState.IN_GAME;

					turnID = 1;
					Opponent.turnID = 2;
					Client.gameBoard.begin(id, Opponent.id);
					Client.gameFrame.setPlayerTurn();
				} else if (response.equals("not your turn")) {
					state = State.NOT_MY_TURN;
					inputState = InputState.IN_GAME;
					Opponent.state = Opponent.State.MY_TURN;
					Opponent.inputState = Opponent.InputState.IN_GAME;

					turnID = 2;
					Opponent.turnID = 1;
					Client.gameBoard.begin(Opponent.id, id);
					Client.gameFrame.setOpponentTurn();
				}

				isQueryTimeout = false;
				isStoneTimeout = false;
				isSurrendered = false;
				putStoneOutOfRangeCnt = 0;
				putStoneErrorMsgResponse = "";
				Opponent.isQueryTimeout = false;
				Opponent.isStoneTimeout = false;
				Opponent.isSurrendered = false;
				Opponent.putStoneOutOfRangeCnt = 0;

				Client.gameFrame.synchronizeBoard(Client.gameBoard);
				Client.gameFrame.setPlayerID(id);
				Client.gameFrame.setOpponentID(Opponent.id);
				Client.gameFrame.setTimer();
				Client.gameFrame.setPutStoneErrorMsg("");
				break;
			default:
				// invalid response
			}
		case MY_TURN:
			switch (inputState) {
			case IN_GAME:
				if (response.equals("stone timeout")) {
					isStoneTimeout = true;
				} else if (response.equals("end")) {
					state = State.TERMINATED;
					inputState = InputState.END_GAME;
					if (Opponent.state != Opponent.State.NONE) {
						Opponent.state = Opponent.State.TERMINATED;
						Opponent.inputState = Opponent.InputState.IN_ROOM;
					}
					terminateTime = LocalDateTime.now();
				} else {
					// invalid response
				}
				break;
			case STONE_GAME:
				if (response.equals("success")) {
					state = State.NOT_MY_TURN;
					inputState = InputState.IN_GAME;
					Opponent.state = Opponent.State.MY_TURN;
					putStoneErrorMsgResponse = "";

					int[] rc = Client.parseCoordinates(stoneQuery);
					Client.gameBoard.board[rc[0]][rc[1]] = turnID;
					Client.gameFrame.putStone(rc[0], rc[1], turnID);
					Client.gameFrame.setTimer();
					Client.gameFrame.setPutStoneErrorMsg("");
				} else if (response.equals("fail")) {
					inputState = InputState.IN_GAME;
				} else if (response.equals("invalid move")) {
					inputState = InputState.INVALID_MOVE;
				} else if (response.equals("stone timeout")) {
					isStoneTimeout = true;
				} else if (response.equals("end")) {
					state = State.TERMINATED;
					inputState = InputState.END_GAME;
					if (Opponent.state != Opponent.State.NONE) {
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
					isStoneTimeout = true;
				} else if (response.equals("end")) {
					state = State.TERMINATED;
					inputState = InputState.END_GAME;
					Opponent.state = Opponent.State.TERMINATED;
					Opponent.inputState = Opponent.InputState.IN_ROOM;
					terminateTime = LocalDateTime.now();
				} else {
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
		case NOT_MY_TURN:
			if (response.equals("end")) {
				state = State.TERMINATED;
				inputState = InputState.END_GAME;
				if (Opponent.state != Opponent.State.NONE) {
					Opponent.state = Opponent.State.TERMINATED;
					Opponent.inputState = Opponent.InputState.NONE;
				}
				terminateTime = LocalDateTime.now();
			} else {
				// invalid response
			}
			break;
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
		case EXIT:
			// may be the response is "bye"
			break;
		}
	}
}