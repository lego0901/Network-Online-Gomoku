/*
 * GameFrame.java
 * Author: Woosung Song
 *
 * GUI in game.
 *
 * <Game info (your turn, you win, ..)>
 * <Player Color>      <Opponent Color>
 * <Player ID>            <Opponent ID>
 *      <Put stone remaining time>
 *          ---------------
 *          --O------------
 *          --XO-----------
 *          --XOO-X--------
 *          --XOXX---------
 *          --XXOO---------
 *
 *         <Surrender button>
 */
package graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.Board;
import client.Client;
import client.Player;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.awt.event.ActionEvent;
import java.awt.Dialog.ModalExclusionType;

public class GameFrame extends JFrame {
  private static final long serialVersionUID = 1L;
  // Image of the game board loaded
  private static final ImageIcon EMPTY = new ImageIcon("./resource/empty.png");
  private static final ImageIcon LU0 = new ImageIcon("./resource/lu0.png");
  private static final ImageIcon LD0 = new ImageIcon("./resource/ld0.png");
  private static final ImageIcon RU0 = new ImageIcon("./resource/ru0.png");
  private static final ImageIcon RD0 = new ImageIcon("./resource/rd0.png");
  private static final ImageIcon LU1 = new ImageIcon("./resource/lu1.png");
  private static final ImageIcon LD1 = new ImageIcon("./resource/ld1.png");
  private static final ImageIcon RU1 = new ImageIcon("./resource/ru1.png");
  private static final ImageIcon RD1 = new ImageIcon("./resource/rd1.png");
  private static final ImageIcon LU2 = new ImageIcon("./resource/lu2.png");
  private static final ImageIcon LD2 = new ImageIcon("./resource/ld2.png");
  private static final ImageIcon RU2 = new ImageIcon("./resource/ru2.png");
  private static final ImageIcon RD2 = new ImageIcon("./resource/rd2.png");
  private static final ImageIcon L0 = new ImageIcon("./resource/l0.png");
  private static final ImageIcon R0 = new ImageIcon("./resource/r0.png");
  private static final ImageIcon U0 = new ImageIcon("./resource/u0.png");
  private static final ImageIcon D0 = new ImageIcon("./resource/d0.png");
  private static final ImageIcon L1 = new ImageIcon("./resource/l1.png");
  private static final ImageIcon R1 = new ImageIcon("./resource/r1.png");
  private static final ImageIcon U1 = new ImageIcon("./resource/u1.png");
  private static final ImageIcon D1 = new ImageIcon("./resource/d1.png");
  private static final ImageIcon L2 = new ImageIcon("./resource/l2.png");
  private static final ImageIcon R2 = new ImageIcon("./resource/r2.png");
  private static final ImageIcon U2 = new ImageIcon("./resource/u2.png");
  private static final ImageIcon D2 = new ImageIcon("./resource/d2.png");
  private static final ImageIcon N0 = new ImageIcon("./resource/0.png");
  private static final ImageIcon N1 = new ImageIcon("./resource/1.png");
  private static final ImageIcon N2 = new ImageIcon("./resource/2.png");

  // {empty, black stone image, white stone image}
  private static final ImageIcon LU[] = { LU0, LU1, LU2 };
  private static final ImageIcon LD[] = { LD0, LD1, LD2 };
  private static final ImageIcon RU[] = { RU0, RU1, RU2 };
  private static final ImageIcon RD[] = { RD0, RD1, RD2 };
  private static final ImageIcon L[] = { L0, L1, L2 };
  private static final ImageIcon R[] = { R0, R1, R2 };
  private static final ImageIcon U[] = { U0, U1, U2 };
  private static final ImageIcon D[] = { D0, D1, D2 };
  private static final ImageIcon N[] = { N0, N1, N2 };

  // Put stone time reference (counts 60->59->58->...)
  private LocalDateTime lastMoveTime;

  // Basic GUI unit
  private JLabel gameHelpLabel;
  private JLabel playerIDLabel;
  private JLabel opponentIDLabel;
  private JLabel playerColorLabel;
  private JLabel opponentColorLabel;
  private JLabel putStoneErrorMsg;
  private JLabel timerLabel;

  // NxN dimensional board buttons
  private JLabel boardLabels[][];

  // Make it same to the Board stoarge info
  public void synchronizeBoard(Board board) {
    for (int i = 0; i < 13; i++) {
      for (int j = 0; j < 13; j++) {
        int row = i - 1;
        int column = j - 1;

        if (row < 0 || row >= 11 || column < 0 || column >= 11) {
          // Out of board block
          boardLabels[i][j].setIcon(EMPTY);
          continue;
        }

        int turnID = board.board[row][column];
        if (row == 0) {
          if (column == 0) {
            // Left Up position
            boardLabels[i][j].setIcon(LU[turnID]);
          } else if (column == 11 - 1) {
            // Right Up position
            boardLabels[i][j].setIcon(RU[turnID]);
          } else {
            // Up position
            boardLabels[i][j].setIcon(U[turnID]);
          }
        } else if (row == 11 - 1) {
          if (column == 0) {
            // Left Down position
            boardLabels[i][j].setIcon(LD[turnID]);
          } else if (column == 11 - 1) {
            // Right Down position
            boardLabels[i][j].setIcon(RD[turnID]);
          } else {
            // Down position
            boardLabels[i][j].setIcon(D[turnID]);
          }
        } else if (column == 0) {
          // Left position
          boardLabels[i][j].setIcon(L[turnID]);
        } else if (column == 11 - 1) {
          // Right position
          boardLabels[i][j].setIcon(R[turnID]);
        } else {
          // Normal position
          boardLabels[i][j].setIcon(N[turnID]);
        }

        boardLabels[i][j].repaint();
      }
    }
  }

  // Put stone of the turn
  public void putStone(int row, int column, int turnID) {
    int i = row + 1, j = column + 1;
    if (row == 0) {
      if (column == 0) {
        // Left Up position
        boardLabels[i][j].setIcon(LU[turnID]);
      } else if (column == 11 - 1) {
        // Right Up position
        boardLabels[i][j].setIcon(RU[turnID]);
      } else {
        // Up position
        boardLabels[i][j].setIcon(U[turnID]);
      }
    } else if (row == 11 - 1) {
      if (column == 0) {
        // Left Down position
        boardLabels[i][j].setIcon(LD[turnID]);
      } else if (column == 11 - 1) {
        // Right Down position
        boardLabels[i][j].setIcon(RD[turnID]);
      } else {
        // Down position
        boardLabels[i][j].setIcon(D[turnID]);
      }
    } else if (column == 0) {
      // Left position
      boardLabels[i][j].setIcon(L[turnID]);
    } else if (column == 11 - 1) {
      // Right position
      boardLabels[i][j].setIcon(R[turnID]);
    } else {
      // Normal position
      boardLabels[i][j].setIcon(N[turnID]);
    }

    boardLabels[i][j].repaint();
  }

  // Setting the player's ID
  public void setPlayerID(String playerID, int playerTurn) {
    playerIDLabel.setText(playerID);
    if (playerTurn == 1)
      playerColorLabel.setText("black");
    else
      playerColorLabel.setText("white");
  }

  // Setting the opponent's ID
  public void setOpponentID(String opponentID, int opponentTurn) {
    opponentIDLabel.setText(opponentID);
    if (opponentTurn == 1)
      opponentColorLabel.setText("black");
    else
      opponentColorLabel.setText("white");
  }

  // Set it is player's turn
  public void setPlayerTurn() {
    gameHelpLabel.setText("Your Turn");
    playerIDLabel.setFont(new Font("Georgia", Font.BOLD, 16));
    opponentIDLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
  }

  // Set it is opponent's turn
  public void setOpponentTurn() {
    gameHelpLabel.setText("Opponent's Turn");
    playerIDLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
    opponentIDLabel.setFont(new Font("Georgia", Font.BOLD, 16));
  }

  // If the client received an error message with putting a stone
  public void setPutStoneErrorMsg(String errorMsg) {
    putStoneErrorMsg.setForeground(Color.RED);
    putStoneErrorMsg.setText(errorMsg);
  }

  // Set put stone timer reference
  public void setTimer() {
    lastMoveTime = LocalDateTime.now();
  }

  // repaint 60 -> 59 -> 58 -> .. countdown
  public void repaintTimer() {
    int timePassed = (int) Duration.between(lastMoveTime, LocalDateTime.now()).getSeconds();
    int timeLeft = 60 - timePassed;
    if (timeLeft < 0)
      timeLeft = 0;
    timerLabel.setText("" + timeLeft);
  }

  // Set winner, loser, draw string
  public void setPlayerWin() {
    gameHelpLabel.setText("You Win");
  }

  public void setPlayerLose() {
    gameHelpLabel.setText("You Lose");
  }

  public void setPlayerDraw() {
    gameHelpLabel.setText("Draw");
  }

  // Set termination reason (ex., opponent disconnected, 50 stones, ..)
  public void setTerminateReason(String reason) {
    putStoneErrorMsg.setForeground(Color.BLACK);
    putStoneErrorMsg.setText(reason);
  }

  public GameFrame() {
    // Exit frame -> send "close" signal to the server
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        Player.state = Player.State.EXIT;
        Client.write("close");
      }
    });
    setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
    setTitle("Gomoku");
    setResizable(false);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 410, 550);

    JPanel contentPane = new JPanel();
    contentPane.setBackground(Color.WHITE);
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);

    // <Game info (your turn, you win, ..)>
    gameHelpLabel = new JLabel("Your Turn");
    gameHelpLabel.setFont(new Font("Georgia", Font.BOLD, 16));
    gameHelpLabel.setHorizontalAlignment(SwingConstants.CENTER);
    gameHelpLabel.setBounds(50, 27, 300, 30);
    contentPane.add(gameHelpLabel);

    // Player and Opponent ID field
    playerIDLabel = new JLabel("playerID");
    playerIDLabel.setHorizontalAlignment(SwingConstants.CENTER);
    playerIDLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
    playerIDLabel.setBounds(12, 67, 140, 30);
    contentPane.add(playerIDLabel);

    opponentIDLabel = new JLabel("opponentID");
    opponentIDLabel.setHorizontalAlignment(SwingConstants.CENTER);
    opponentIDLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
    opponentIDLabel.setBounds(252, 67, 140, 30);
    contentPane.add(opponentIDLabel);

    // Player and Opponent Color field
    playerColorLabel = new JLabel("");
    playerColorLabel.setHorizontalAlignment(SwingConstants.CENTER);
    playerColorLabel.setFont(new Font("Georgia", Font.PLAIN, 10));
    playerColorLabel.setBounds(12, 52, 140, 30);
    contentPane.add(playerColorLabel);

    opponentColorLabel = new JLabel("");
    opponentColorLabel.setHorizontalAlignment(SwingConstants.CENTER);
    opponentColorLabel.setFont(new Font("Georgia", Font.PLAIN, 10));
    opponentColorLabel.setBounds(252, 52, 140, 30);
    contentPane.add(opponentColorLabel);

    // Put stone countdown field
    timerLabel = new JLabel("60");
    timerLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
    timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
    timerLabel.setBounds(175, 67, 50, 30);
    contentPane.add(timerLabel);

    // Put stone error message field
    putStoneErrorMsg = new JLabel("put stone error msg");
    putStoneErrorMsg.setForeground(Color.RED);
    putStoneErrorMsg.setHorizontalAlignment(SwingConstants.CENTER);
    putStoneErrorMsg.setFont(new Font("Georgia", Font.PLAIN, 12));
    putStoneErrorMsg.setBounds(50, 110, 300, 30);
    contentPane.add(putStoneErrorMsg);

    // Surrender button
    JButton roomLeaveButton = new JButton("Surrender");
    roomLeaveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // send "surrender" signal to the server
        Client.pendQuery("surrender");
      }
    });
    roomLeaveButton.setFont(new Font("Georgia", Font.PLAIN, 12));
    roomLeaveButton.setBackground(Color.LIGHT_GRAY);
    roomLeaveButton.setBounds(154, 467, 97, 23);
    contentPane.add(roomLeaveButton);

    // Game board panel
    JPanel panel = new JPanel();
    panel.setBounds(56, 150, 286, 286);
    contentPane.add(panel);
    panel.setLayout(null);

    // Initiate all game board image button
    boardLabels = new JLabel[13][13];
    for (int i = 0; i < 13; i++) {
      for (int j = 0; j < 13; j++) {
        int row = i - 1;
        int column = j - 1;
        boardLabels[i][j] = new JLabel(EMPTY);
        panel.add(boardLabels[i][j]);
        boardLabels[i][j].setBounds(j * 22, i * 22, 22, 22);
        boardLabels[i][j].addMouseListener((MouseListener) new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            // If clicked, then send "stone\n{row} {column}" signal to server
            Client.pendQuery("stone");
            Client.pendQuery("" + row + " " + column);
          }
        });
      }
    }
    synchronizeBoard(Client.gameBoard);
  }
}
