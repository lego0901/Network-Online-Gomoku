package gomoku;

import java.util.Scanner;

public class Gomoku {
	public int rows;
	public int columns;
	public int board[][];
	public int turn;
	public int numStones;
	public int maxStones;
	public int winner;
	public boolean terminated;
	public int outOfRangeCount[];
	public String putStoneErrorMsg;
	
	public static boolean isStoneTimeout = false;

	private static final int numDirections = 4;
	private static final int diffRows[] = { 0, 1, 1, 1 };
	private static final int diffColumns[] = { 1, 0, 1, -1 };

	public Gomoku(int rows, int columns, int maxStones) {
		this.rows = rows;
		this.columns = columns;
		this.maxStones = maxStones;
		initialize();
	}

	public void initialize() {
		turn = 1;
		numStones = 0;
		winner = 0;
		terminated = false;
		outOfRangeCount = new int[2];
		board = new int[rows][columns];
	}

	public boolean putStone(int row, int column) {
		putStoneErrorMsg = "None";

		if (isOutOfRange(row, column)) {
			putStoneErrorMsg = "Stone out of range (error count = " + (++outOfRangeCount[turn - 1]) + ")";

			if (outOfRangeCount[turn - 1] >= 2) {
				winner = nextTurn(turn);
				terminated = true;
				return false;
			} else {
				return false;
			}
		}

		if (isAbleToPutStone(row, column)) {
			board[row][column] = turn;
			numStones++;

			if (isConsecutiveFive(row, column)) {
				winner = turn;
				terminated = true;
			} else if (numStones == maxStones) {
				winner = -1;
				terminated = true;
			} else {
				turn = nextTurn(turn);
			}
			return true;
		}
		return false;
	}

	public static String idOfTurn(int turn, String id1, String id2) {
		switch (turn) {
		case -1:
			return "draw";
		case 0:
			return "not determined";
		case 1:
			return id1;
		case 2:
			return id2;
		}
		return "";
	}

	public static int nextTurn(int turn) {
		return turn == 1 ? 2 : 1;
	}

	public boolean isAbleToPutStone(int row, int column) {
		assert (!isOutOfRange(row, column));
		if (board[row][column] != 0) {
			putStoneErrorMsg = "Stone already on that position";
			return false;
		}
		return true;
	}

	public boolean isOutOfRange(int row, int column) {
		return row < 0 || row >= rows || column < 0 || column >= columns;
	}

	public boolean isConsecutiveFive(int row, int column) {
		for (int dir = 0; dir < numDirections; dir++) {
			int dr = diffRows[dir], dc = diffColumns[dir];
			int left = 0, right = 0, r, c;

			r = row - dr;
			c = column - dc;
			while (!isOutOfRange(r, c) && board[r][c] == turn) {
				left++;
				r -= dr;
				c -= dc;
			}

			r = row + dr;
			c = column + dc;
			while (!isOutOfRange(r, c) && board[r][c] == turn) {
				right++;
				r += dr;
				c += dc;
			}

			if (left + 1 + right >= 5)
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		String id1 = "O", id2 = "X";
		String boardString = "Turn: " + idOfTurn(turn, id1, id2) + ", Winner: " + idOfTurn(winner, id1, id2)
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
	
	private static Scanner keyboard;
	
	public static void main(String[] args) {
		keyboard = new Scanner(System.in);
		Gomoku game = new Gomoku(11, 11, 50);
		
		while (!game.terminated) {
			System.out.println(game);
			int row = keyboard.nextInt(), column = keyboard.nextInt();
			if (game.putStone(row, column));
			else {
				System.out.println(game.putStoneErrorMsg);
			}
		}
		
		System.out.println(game);
	}
}
