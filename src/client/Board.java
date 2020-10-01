package client;

public class Board {
	public int rows;
	public int columns;
	public int board[][];
	public String turnIDs[];
	public int turn;
	public int winner;
	public boolean terminated;
	public String putStoneErrorMsg;
	
	public Board(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		initialize();
	}
	
	public Board(int rows, int columns, String id1, String id2) {
		this.rows = rows;
		this.columns = columns;
		begin(id1, id2);
	}
	
	public void initialize() {
		turn = 1;
		board = new int[rows][columns];
		winner = 0;
		terminated = false;
		turnIDs = new String[2];
	}
	
	public void begin(String id1, String id2) {
		initialize();
		turnIDs = new String[2];
		turnIDs[0] = id1;
		turnIDs[1] = id2;
	}
	
	private String idOfTurn(int turn) {
		switch (turn) {
		case -1:
			return "draw";
		case 0:
			return "not determined";
		case 1:
		case 2:
			return turnIDs[turn - 1]; 
		}
		return "";
	}

	@Override
	public String toString() {
		String boardString = "Turn: " + idOfTurn(turn) + ", Winner: " + idOfTurn(winner)
				+ "\n======================================\n  ";

		for (int j = 0; j < columns; j++) {
			boardString += (char) ('0' + j % 10);
		}
		boardString += "\n";

		for (int i = 0; i < rows; i++) {
			boardString += (char) ('0' + i % 10) + " ";
			for (int j = 0; j < columns; j++) {
				switch (board[i][j]) {
				case 1:
					boardString += "O";
					break;
				case 2:
					boardString += "X";
					break;
				default:
					boardString += "-";
					break;
				}
			}
			boardString += "\n";
		}

		return boardString;
	}
}
