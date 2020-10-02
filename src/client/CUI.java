package client;

public class CUI {
	public static void repaint() {
		/*
		System.out.println("state: " + Player.state);
		System.out.println("inputState: " + Player.inputState);
		System.out.println("opponent state: " + Opponent.state);
		System.out.println("opponent inputState: " + Opponent.inputState);
		*/
		switch (Player.state) {
		case SEARCH_ROOM:
			switch (Player.inputState) {
			case OUT_ROOM:
				System.out.println("What are you going to do?");
				System.out.println("> create");
				System.out.println("> join");
				System.out.println("> search");
				break;
			case CREATE_ROOM:
				System.out.println("Please type your new room name");
				break;
			case JOIN_ROOM:
				System.out.println("Please type a room name to join");
				break;
			default:
				// invalid state
			}
			break;
		case ENTER_ROOM:
			switch (Player.inputState) {
			case IN_ROOM:
				System.out.println("You are in the room " + Player.roomID);
				System.out.println("Players:");
				System.out.println(">> " + Player.id + " (not ready)");
				if (Opponent.state != Opponent.State.NONE) {
					System.out.print(">> " + Opponent.id);
					if (Opponent.state == Opponent.State.ENTER_ROOM)
						System.out.println(" (not ready)");
					else
						System.out.println(" (ready)");
				}
				System.out.println("Your possible actions");
				System.out.println("> ready");
				System.out.println("> leave");
				break;
			default:
				// invalid state
			}
			break;
		case READY_ROOM:
			switch (Player.inputState) {
			case IN_ROOM:
				System.out.println("You are in the room " + Player.roomID);
				System.out.println("Players:");
				System.out.println(">> " + Player.id + " (ready)");
				if (Opponent.state != Opponent.State.NONE) {
					System.out.print(">> " + Opponent.id);
					if (Opponent.state == Opponent.State.ENTER_ROOM)
						System.out.println(" (not ready)");
					else
						System.out.println(" (ready)");
				}
				System.out.println("Your possible actions");
				System.out.println("> cancel");
				System.out.println("> leave");
				break;
			case BEFORE_GAME:
				System.out.println("Now it begins to play");
				if (Player.turnID == 1) {
					System.out.println(">> 1st player: " + Player.id + " (you)");
					System.out.println(">> 2nd player: " + Opponent.id);
				} else {
					System.out.println(">> 1st player: " + Opponent.id);
					System.out.println(">> 2nd player: " + Player.id + " (you)");
				}
				break;
			default:
				// invalid state
			}
			break;
		case MY_TURN:
			switch (Player.inputState) {
			case IN_GAME:
				if (!Player.putStoneErrorMsgResponse.equals(""))
					System.out.println(">> " + Player.putStoneErrorMsgResponse);
				System.out.println("(Your turn) The board status is");
				System.out.println(ProxyClientReader.gameBoard);
				System.out.println("Your possible actions");
				System.out.println("> stone");
				System.out.println("> surrender");
				break;
			case STONE_GAME:
				System.out.println("> row column");
				break;
			default:
				// invalid state
			}
			break;
		case NOT_MY_TURN:
			switch (Player.inputState) {
			case IN_GAME:
				System.out.println("(Opponent turn) The board status is");
				System.out.println(ProxyClientReader.gameBoard);
				break;
			default:
				// invalid state
			}
			break;
		case TERMINATED:
			System.out.println("Game end");
			System.out.println(">> " + Player.terminateResponse);
			if (Player.isStoneTimeout)
				System.out.println(">> player stone timeout");
			if (Opponent.isStoneTimeout)
				System.out.println(">> opponent stone timeout");
			if (Player.isSurrendered)
				System.out.println(">> player surrendered");
			if (Opponent.isSurrendered)
				System.out.println(">> opponent surrendered");
			if (Player.putStoneOutOfRangeCnt >= 2)
				System.out.println(">> player out of range stone 2 times");
			if (Opponent.putStoneOutOfRangeCnt >= 2)
				System.out.println(">> opponent out of range stone 2 times");
			if (Opponent.state == Opponent.State.NONE)
				System.out.println(">> opponent disconnected");
			break;
		case EXIT:
			System.out.println("Bye");
			break;
		}
	}
}
