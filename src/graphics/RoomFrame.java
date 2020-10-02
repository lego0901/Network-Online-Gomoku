package graphics;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.Client;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.Dialog.ModalExclusionType;

public class RoomFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	
	private String playerID;
	private String opponentID;
	
	private JLabel roomIDLabel;
	private JLabel playerIDLabel;
	private JLabel opponentIDLabel;
	private JButton roomReadyOrCancelButton;
	private boolean isPlayerReady = false;
	private boolean isOpponentReady = false;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RoomFrame frame = new RoomFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void setRoomID(String roomID) {
		roomIDLabel.setText("Room ID: " + roomID);
	}
	
	public void setPlayerID(String playerID) {
		this.playerID = playerID;
		if (isPlayerReady)
			playerIDLabel.setText(playerID + " (Ready)");
		else
			playerIDLabel.setText(playerID);
	}
	
	public void setPlayerReadyOrCancel(boolean ready) {
		if (ready) {
			isPlayerReady = true;
			playerIDLabel.setFont(new Font("Arial", Font.BOLD, 16));
			playerIDLabel.setText(playerID + " (Ready)");
			roomReadyOrCancelButton.setText("Cancel Ready");
		} else {
			isPlayerReady = false;
			playerIDLabel.setFont(new Font("Arial", Font.PLAIN, 16));
			playerIDLabel.setText(playerID);
			roomReadyOrCancelButton.setText("Ready");
		}
	}
	
	public void setOpponentID(String opponentID) {
		this.opponentID = opponentID;
		if (isOpponentReady)
			opponentIDLabel.setText(opponentID + " (ready)");
		else
			opponentIDLabel.setText(opponentID);
	}
	
	public void setOpponentReadyOrCancel(boolean ready) {
		if (ready) {
			opponentIDLabel.setFont(new Font("Arial", Font.BOLD, 16));
			opponentIDLabel.setText(opponentID + " (ready)");
		} else {
			opponentIDLabel.setFont(new Font("Arial", Font.PLAIN, 16));
			opponentIDLabel.setText(opponentID);
		}
	}

	public RoomFrame() {
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

		JLabel roomHelpLabel = new JLabel("Waiting for a game");
		roomHelpLabel.setFont(new Font("Arial", Font.BOLD, 16));
		roomHelpLabel.setHorizontalAlignment(SwingConstants.CENTER);
		roomHelpLabel.setBounds(50, 68, 300, 30);
		contentPane.add(roomHelpLabel);
		
		roomIDLabel = new JLabel("Room ID: ");
		roomIDLabel.setHorizontalAlignment(SwingConstants.CENTER);
		roomIDLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		roomIDLabel.setBounds(50, 108, 300, 30);
		contentPane.add(roomIDLabel);
		
		playerIDLabel = new JLabel("playerID");
		playerIDLabel.setHorizontalAlignment(SwingConstants.CENTER);
		playerIDLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		playerIDLabel.setBounds(50, 209, 300, 30);
		contentPane.add(playerIDLabel);
		
		JLabel roomVSLabel = new JLabel("v.s.");
		roomVSLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		roomVSLabel.setHorizontalAlignment(SwingConstants.CENTER);
		roomVSLabel.setBounds(50, 249, 300, 30);
		contentPane.add(roomVSLabel);
		
		opponentIDLabel = new JLabel("opponentID");
		opponentIDLabel.setHorizontalAlignment(SwingConstants.CENTER);
		opponentIDLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		opponentIDLabel.setBounds(50, 289, 300, 30);
		contentPane.add(opponentIDLabel);
		
		JButton roomLeaveButton = new JButton("Leave");
		roomLeaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Client.pendQuery("leave");
				Client.pendQuery("search");
			}
		});
		roomLeaveButton.setFont(new Font("Arial", Font.PLAIN, 12));
		roomLeaveButton.setBackground(Color.LIGHT_GRAY);
		roomLeaveButton.setBounds(89, 450, 97, 23);
		contentPane.add(roomLeaveButton);
		
		roomReadyOrCancelButton = new JButton("Ready");
		roomReadyOrCancelButton.setFont(new Font("Arial", Font.PLAIN, 12));
		roomReadyOrCancelButton.setBackground(Color.LIGHT_GRAY);
		roomReadyOrCancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isPlayerReady) {
					Client.pendQuery("cancel");
					setPlayerReadyOrCancel(false);
				} else {
					Client.pendQuery("ready");
					setPlayerReadyOrCancel(true);
				}
			}
		});
		roomReadyOrCancelButton.setBounds(221, 450, 97, 23);
		contentPane.add(roomReadyOrCancelButton);
		
		setOpponentID("(Waiting)");
		setOpponentReadyOrCancel(false);
	}
}
