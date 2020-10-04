/*
 * RoomSearchFrame.java
 * Author: Woosung Song
 *
 * GUI in the room before playing the game
 *
 *       <Waiting for a game>
 *          <Room ID: ...>
 *    <Player ID (ready or not)>
 *              <v.s.>
 *   <Opponent ID (ready or not)>
 *  <Leave> <Ready or cancel button>
 */
package graphics;

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

  // Player and opponent ID info
  private String playerID;
  private String opponentID;
  // Are they ready or not
  private boolean isPlayerReady = false;
  private boolean isOpponentReady = false;

  // Class data
  private JLabel roomIDLabel;
  private JLabel playerIDLabel;
  private JLabel opponentIDLabel;
  private JButton roomReadyOrCancelButton;

  // <Room ID: ...>
  public void setRoomID(String roomID) {
    roomIDLabel.setText("Room ID: " + roomID);
  }

  // <Player ID (ready or not)>
  public void setPlayerID(String playerID) {
    this.playerID = playerID;
    if (isPlayerReady)
      playerIDLabel.setText(playerID + " (Ready)");
    else
      playerIDLabel.setText(playerID);
  }

  // <Player ID (ready or not)> (Ready info changed)
  public void setPlayerReadyOrCancel(boolean ready) {
    if (ready) {
      isPlayerReady = true;
      playerIDLabel.setFont(new Font("Georgia", Font.BOLD, 16));
      playerIDLabel.setText(playerID + " (Ready)");
      roomReadyOrCancelButton.setText("Cancel");
    } else {
      isPlayerReady = false;
      playerIDLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
      playerIDLabel.setText(playerID);
      roomReadyOrCancelButton.setText("Ready");
    }
  }

  // <Opponent ID (ready or not)>
  public void setOpponentID(String opponentID) {
    this.opponentID = opponentID;
    if (isOpponentReady)
      opponentIDLabel.setText(opponentID + " (ready)");
    else
      opponentIDLabel.setText(opponentID);
  }

  // <Opponent ID (ready or not)> (Ready info changed)
  public void setOpponentReadyOrCancel(boolean ready) {
    if (ready) {
      opponentIDLabel.setFont(new Font("Georgia", Font.BOLD, 16));
      opponentIDLabel.setText(opponentID + " (ready)");
    } else {
      opponentIDLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
      opponentIDLabel.setText(opponentID);
    }
  }

  public RoomFrame() {
    // Exit frame -> send "close" signal to the server
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

    JPanel contentPane = new JPanel();
    contentPane.setBackground(Color.WHITE);
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);

    // <Waiting for a game>
    JLabel roomHelpLabel = new JLabel("Waiting for a game");
    roomHelpLabel.setFont(new Font("Georgia", Font.BOLD, 16));
    roomHelpLabel.setHorizontalAlignment(SwingConstants.CENTER);
    roomHelpLabel.setBounds(50, 68, 300, 30);
    contentPane.add(roomHelpLabel);

    // <Room ID: ...>
    roomIDLabel = new JLabel("Room ID: ");
    roomIDLabel.setHorizontalAlignment(SwingConstants.CENTER);
    roomIDLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
    roomIDLabel.setBounds(50, 108, 300, 30);
    contentPane.add(roomIDLabel);

    // <Player ID (ready or not)>
    playerIDLabel = new JLabel("playerID");
    playerIDLabel.setHorizontalAlignment(SwingConstants.CENTER);
    playerIDLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
    playerIDLabel.setBounds(50, 209, 300, 30);
    contentPane.add(playerIDLabel);

    // <v.s.>
    JLabel roomVSLabel = new JLabel("v.s.");
    roomVSLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
    roomVSLabel.setHorizontalAlignment(SwingConstants.CENTER);
    roomVSLabel.setBounds(50, 249, 300, 30);
    contentPane.add(roomVSLabel);

    // <Opponent ID (ready or not)>
    opponentIDLabel = new JLabel("opponentID");
    opponentIDLabel.setHorizontalAlignment(SwingConstants.CENTER);
    opponentIDLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
    opponentIDLabel.setBounds(50, 289, 300, 30);
    contentPane.add(opponentIDLabel);

    // <Leave> button
    JButton roomLeaveButton = new JButton("Leave");
    roomLeaveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Click? then leave and search the rooms to join
        Client.pendQuery("leave");
        Client.pendQuery("search");
      }
    });
    roomLeaveButton.setFont(new Font("Georgia", Font.PLAIN, 12));
    roomLeaveButton.setBackground(Color.LIGHT_GRAY);
    roomLeaveButton.setBounds(89, 450, 97, 23);
    contentPane.add(roomLeaveButton);

    // <Ready> button. If already ready, then it is <Cancel>
    roomReadyOrCancelButton = new JButton("Ready");
    roomReadyOrCancelButton.setFont(new Font("Georgia", Font.PLAIN, 12));
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

    // Opponent is initially none.
    setOpponentID("(Waiting)");
    setOpponentReadyOrCancel(false);
  }
}
