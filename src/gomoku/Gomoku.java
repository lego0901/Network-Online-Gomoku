/*
 * Gomoku.java
 * Author: Woosung Song
 *
 * A gomoku game board information and semantic calculation unit.
 * Only server computes this and tell clients the computation results.
 */
package gomoku;

import java.util.Scanner;

public class Gomoku {
  // basic dimension of the board
  public int rows;
  public int columns;
  // 2d array of board; element is 0 if empty, 1 if black, 2 if white
  public int board[][];

  // Whose turn right now
  public int turn;
  // Number of stones have been put on the board
  public int numStones;
  // Maximum number of stones allowed on the board
  public int maxStones;

  // -1: draw, 0: not determined, 1: black, 2: white
  public int winner;
  public boolean terminated;

  // Number of "out of board" counts for each player;
  // If one of the players do 2 times, then he loses
  public int outOfRangeCount[];
  public String putStoneErrorMsg;

  // Static variables that help computing "directional consecutives"
  private static final int numDirections = 4;
  private static final int diffRows[] = { 0, 1, 1, 1 };
  private static final int diffColumns[] = { 1, 0, 1, -1 };

  // Constructor
  public Gomoku(int rows, int columns, int maxStones) {
    this.rows = rows;
    this.columns = columns;
    this.maxStones = maxStones;
    initialize();
  }

  // Initialize gll game information
  public void initialize() {
    turn = 1;
    numStones = 0;
    winner = 0;
    terminated = false;
    outOfRangeCount = new int[2];
    board = new int[rows][columns];
  }

  // ID value of the given turn, mainly used in CUI client
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

  // 1 -> 2 -> 1 -> 2 -> ...
  public static int nextTurn(int turn) {
    return turn == 1 ? 2 : 1;
  }

  // Put stone of the current turn if possible
  // If it is possible, then update board and return true
  // Otherwise, putStoneErrorMsg is made (and send to the client)
  public boolean putStone(int row, int column) {
    putStoneErrorMsg = "None";

    if (isOutOfRange(row, column)) {
      // Stone out of range
      putStoneErrorMsg = "Stone out of range (error count = " + (++outOfRangeCount[turn - 1]) + ")";

      if (outOfRangeCount[turn - 1] >= 2) {
        // 2 times, then the client of the turn loses
        winner = nextTurn(turn);
        terminated = true;
      }
      return false;
    }

    if (isAbleToPutStone(row, column)) {
      // Possible to put stone
      // isAbleToPutStone function makes putStoneErrorMsg if error
      board[row][column] = turn;
      numStones++;

      if (isConsecutiveFive(row, column)) {
        // Gomoku!
        winner = turn;
        terminated = true;
      } else if (numStones == maxStones) {
        // Max stone reached; draw
        winner = -1;
        terminated = true;
      } else {
        // Change turn
        turn = nextTurn(turn);
      }
      return true;
    }
    // Not able to put stone
    return false;
  }

  // Check if it is possible to put stone.
  // If it is not possible, then make putStoneErrorMsg.
  public boolean isAbleToPutStone(int row, int column) {
    // isOutOfRange condition is checked previously.
    assert (!isOutOfRange(row, column));

    if (board[row][column] != 0) {
      // duplicated stone
      putStoneErrorMsg = "Stone already on that position";
      return false;
    }
    return true;
  }

  // Out of board checker
  public boolean isOutOfRange(int row, int column) {
    return row < 0 || row >= rows || column < 0 || column >= columns;
  }

  // Normal termination condition checker,
  // after the player of the turn puts a stone on (row, column)
  public boolean isConsecutiveFive(int row, int column) {
    // 4 directions (horizontal, vertical, diagonal, anti-diagonal
    for (int dir = 0; dir < numDirections; dir++) {
      int dr = diffRows[dir], dc = diffColumns[dir];
      int left = 0, right = 0, r, c;

      // Update left directional consecutives
      r = row - dr;
      c = column - dc;
      while (!isOutOfRange(r, c) && board[r][c] == turn) {
        left++;
        r -= dr;
        c -= dc;
      }

      // Update right directional consecutives
      r = row + dr;
      c = column + dc;
      while (!isOutOfRange(r, c) && board[r][c] == turn) {
        right++;
        r += dr;
        c += dc;
      }

      // If merged consecutives compose Gomoku, then true
      if (left + 1 + right >= 5)
        return true;
    }
    return false;
  }

  // For CUI server (debug)
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
