package graphics;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.Board;
import client.Client;

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
	private static final ImageIcon empty = new ImageIcon("./resource/empty.png");
	private static final ImageIcon lu0 = new ImageIcon("./resource/lu0.png");
	private static final ImageIcon ld0 = new ImageIcon("./resource/ld0.png");
	private static final ImageIcon ru0 = new ImageIcon("./resource/ru0.png");
	private static final ImageIcon rd0 = new ImageIcon("./resource/rd0.png");
	private static final ImageIcon lu1 = new ImageIcon("./resource/lu1.png");
	private static final ImageIcon ld1 = new ImageIcon("./resource/ld1.png");
	private static final ImageIcon ru1 = new ImageIcon("./resource/ru1.png");
	private static final ImageIcon rd1 = new ImageIcon("./resource/rd1.png");
	private static final ImageIcon lu2 = new ImageIcon("./resource/lu2.png");
	private static final ImageIcon ld2 = new ImageIcon("./resource/ld2.png");
	private static final ImageIcon ru2 = new ImageIcon("./resource/ru2.png");
	private static final ImageIcon rd2 = new ImageIcon("./resource/rd2.png");
	private static final ImageIcon l0 = new ImageIcon("./resource/l0.png");
	private static final ImageIcon r0 = new ImageIcon("./resource/r0.png");
	private static final ImageIcon u0 = new ImageIcon("./resource/u0.png");
	private static final ImageIcon d0 = new ImageIcon("./resource/d0.png");
	private static final ImageIcon l1 = new ImageIcon("./resource/l1.png");
	private static final ImageIcon r1 = new ImageIcon("./resource/r1.png");
	private static final ImageIcon u1 = new ImageIcon("./resource/u1.png");
	private static final ImageIcon d1 = new ImageIcon("./resource/d1.png");
	private static final ImageIcon l2 = new ImageIcon("./resource/l2.png");
	private static final ImageIcon r2 = new ImageIcon("./resource/r2.png");
	private static final ImageIcon u2 = new ImageIcon("./resource/u2.png");
	private static final ImageIcon d2 = new ImageIcon("./resource/d2.png");
	private static final ImageIcon n0 = new ImageIcon("./resource/0.png");
	private static final ImageIcon n1 = new ImageIcon("./resource/1.png");
	private static final ImageIcon n2 = new ImageIcon("./resource/2.png");

	private static final ImageIcon LU[] = { lu0, lu1, lu2 };
	private static final ImageIcon LD[] = { ld0, ld1, ld2 };
	private static final ImageIcon RU[] = { ru0, ru1, ru2 };
	private static final ImageIcon RD[] = { rd0, rd1, rd2 };
	private static final ImageIcon L[] = { l0, l1, l2 };
	private static final ImageIcon R[] = { r0, r1, r2 };
	private static final ImageIcon U[] = { u0, u1, u2 };
	private static final ImageIcon D[] = { d0, d1, d2 };
	private static final ImageIcon N[] = { n0, n1, n2 };

	private JPanel contentPane;

	private LocalDateTime lastMoveTime;

	private JLabel gameHelpLabel;
	private JLabel playerIDLabel;
	private JLabel opponentIDLabel;
	private JLabel putStoneErrorMsg;
	private JLabel timerLabel;

	private JLabel boardLabels[][];

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GameFrame frame = new GameFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void synchronizeBoard(Board board) {
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				int row = i - 1;
				int column = j - 1;

				if (row < 0 || row >= 11 || column < 0 || column >= 11) {
					boardLabels[i][j].setIcon(empty);
					continue;
				}

				int turnID = board.board[row][column];
				if (row == 0) {
					if (column == 0) {
						boardLabels[i][j].setIcon(LU[turnID]);
					} else if (column == 11 - 1) {
						boardLabels[i][j].setIcon(RU[turnID]);
					} else {
						boardLabels[i][j].setIcon(U[turnID]);
					}
				} else if (row == 11 - 1) {
					if (column == 0) {
						boardLabels[i][j].setIcon(LD[turnID]);
					} else if (column == 11 - 1) {
						boardLabels[i][j].setIcon(RD[turnID]);
					} else {
						boardLabels[i][j].setIcon(D[turnID]);
					}
				} else if (column == 0) {
					boardLabels[i][j].setIcon(L[turnID]);
				} else if (column == 11 - 1) {
					boardLabels[i][j].setIcon(R[turnID]);
				} else {
					boardLabels[i][j].setIcon(N[turnID]);
				}
				
				boardLabels[i][j].repaint();
			}
		}
	}

	public void putStone(int row, int column, int turnID) {
		int i = row + 1, j = column + 1;
		if (row == 0) {
			if (column == 0) {
				boardLabels[i][j].setIcon(LU[turnID]);
			} else if (column == 11 - 1) {
				boardLabels[i][j].setIcon(RU[turnID]);
			} else {
				boardLabels[i][j].setIcon(U[turnID]);
			}
		} else if (row == 11 - 1) {
			if (column == 0) {
				boardLabels[i][j].setIcon(LD[turnID]);
			} else if (column == 11 - 1) {
				boardLabels[i][j].setIcon(RD[turnID]);
			} else {
				boardLabels[i][j].setIcon(D[turnID]);
			}
		} else if (column == 0) {
			boardLabels[i][j].setIcon(L[turnID]);
		} else if (column == 11 - 1) {
			boardLabels[i][j].setIcon(R[turnID]);
		} else {
			boardLabels[i][j].setIcon(N[turnID]);
		}
		boardLabels[i][j].repaint();
	}

	public void setPlayerID(String playerID) {
		playerIDLabel.setText(playerID);
	}

	public void setOpponentID(String opponentID) {
		opponentIDLabel.setText(opponentID);
	}

	public void setPlayerTurn() {
		gameHelpLabel.setText("Your Turn");
		playerIDLabel.setFont(new Font("Georgia", Font.BOLD, 16));
		opponentIDLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
	}

	public void setOpponentTurn() {
		gameHelpLabel.setText("Opponent's Turn");
		playerIDLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
		opponentIDLabel.setFont(new Font("Georgia", Font.BOLD, 16));
	}

	public void setPutStoneErrorMsg(String errorMsg) {
		putStoneErrorMsg.setForeground(Color.RED);
		putStoneErrorMsg.setText(errorMsg);
	}

	public void setTimer() {
		lastMoveTime = LocalDateTime.now();
	}

	public void repaintTimer() {
		int timePassed = (int) Duration.between(lastMoveTime, LocalDateTime.now()).getSeconds();
		int timeLeft = 60 - timePassed;
		if (timeLeft < 0)
			timeLeft = 0;
		timerLabel.setText("" + timeLeft);
	}
	
	public void setPlayerWin() {
		gameHelpLabel.setText("You Win");
	}
	
	public void setPlayerLose() {
		gameHelpLabel.setText("You Lose");
	}
	
	public void setPlayerDraw() {
		gameHelpLabel.setText("Draw");
	}
	
	public void setTerminateReason(String reason) {
		putStoneErrorMsg.setForeground(Color.BLACK);
		putStoneErrorMsg.setText(reason);
	}

	public GameFrame() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Client.pendQuery("close");
			}
		});
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setTitle("Gomoku");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 410, 550);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		gameHelpLabel = new JLabel("Your Turn");
		gameHelpLabel.setFont(new Font("Georgia", Font.BOLD, 16));
		gameHelpLabel.setHorizontalAlignment(SwingConstants.CENTER);
		gameHelpLabel.setBounds(50, 27, 300, 30);
		contentPane.add(gameHelpLabel);

		playerIDLabel = new JLabel("playerID");
		playerIDLabel.setHorizontalAlignment(SwingConstants.CENTER);
		playerIDLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
		playerIDLabel.setBounds(12, 67, 140, 30);
		contentPane.add(playerIDLabel);

		timerLabel = new JLabel("60");
		timerLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
		timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		timerLabel.setBounds(175, 67, 50, 30);
		contentPane.add(timerLabel);

		opponentIDLabel = new JLabel("opponentID");
		opponentIDLabel.setHorizontalAlignment(SwingConstants.CENTER);
		opponentIDLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
		opponentIDLabel.setBounds(252, 67, 140, 30);
		contentPane.add(opponentIDLabel);

		JButton roomLeaveButton = new JButton("Surrender");
		roomLeaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Client.pendQuery("surrender");
			}
		});
		roomLeaveButton.setFont(new Font("Georgia", Font.PLAIN, 12));
		roomLeaveButton.setBackground(Color.LIGHT_GRAY);
		roomLeaveButton.setBounds(154, 467, 97, 23);
		contentPane.add(roomLeaveButton);

		JPanel panel = new JPanel();
		panel.setBounds(56, 150, 286, 286);
		contentPane.add(panel);
		panel.setLayout(null);
		
		putStoneErrorMsg = new JLabel("put stone error msg");
		putStoneErrorMsg.setForeground(Color.RED);
		putStoneErrorMsg.setHorizontalAlignment(SwingConstants.CENTER);
		putStoneErrorMsg.setFont(new Font("Georgia", Font.PLAIN, 12));
		putStoneErrorMsg.setBounds(50, 110, 300, 30);
		contentPane.add(putStoneErrorMsg);

		boardLabels = new JLabel[13][13];
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				int row = i - 1;
				int column = j - 1;
				boardLabels[i][j] = new JLabel(empty);
				panel.add(boardLabels[i][j]);
				boardLabels[i][j].setBounds(j * 22, i * 22, 22, 22);
				boardLabels[i][j].addMouseListener((MouseListener) new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						Client.pendQuery("stone");
						Client.pendQuery("" + row + " " + column);
						System.out.println("" + row + " " + column);
					}
				});
			}
		}
		synchronizeBoard(Client.gameBoard);
	}
}
