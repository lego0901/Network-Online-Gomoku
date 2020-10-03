/*
 * RoomSearchFrame.java
 * Author: Woosung Song
 *
 * GUI for choosing roomID to enter or create.
 *
 * <Please choose a room to play>
 *      <Existing rooms list>
 *    - (wait) room 1
 *    - (wait) room 2
 *    - ...
 *    - (full) room 1
 *    - (full) room 2
 *    - ...
 *  <TextField for room name>
 *  <Refresh> <Join> <Create button>
 */
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

import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;

import javax.swing.JScrollPane;

public class RoomSelectFrame extends JFrame {
  private static final long serialVersionUID = 1L;
  // A scrollPane's panel that displays rooms lists
  private JPanel roomLabelPanel;
  // Components in roomLabelPanel
  private ArrayList<JLabel> roomLabels;
  // Room ID textfield
  private JTextField roomIDTextField;

  // Empify roomLabelPanel
  public void initializeRoomLabels() {
    if (roomLabels != null) {
      for (JLabel roomLabel : roomLabels) {
        // Remove previous components
        roomLabelPanel.remove(roomLabel);
      }
    }
    roomLabels = new ArrayList<JLabel>();
    roomLabelPanel.setPreferredSize(new Dimension(272, 24));
    roomLabelPanel.repaint();
  }

  // Add a room ID to the list
  public void addRoomLabel(String roomInfo) {
    JLabel roomLabel = new JLabel(roomInfo);
    roomLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
    // Waiting room is bolded
    if (roomInfo.substring(0, 6).equals("(wait)"))
      roomLabel.setFont(new Font("Georgia", Font.BOLD, 12));
    else
      roomLabel.setFont(new Font("Georgia", Font.PLAIN, 12));

    roomLabel.addMouseListener((MouseListener) new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        // Click than fill the textfield
        roomIDTextField.setText(roomInfo.substring(7));
      }
    });
    roomLabel.setBounds(12, 12 + 30 * roomLabels.size(), 272, 30);
    roomLabels.add(roomLabel);
    roomLabelPanel.add(roomLabel);
    roomLabelPanel.setPreferredSize(new Dimension(272, 24 + 30 * roomLabels.size()));
    roomLabelPanel.repaint();
  }

  public RoomSelectFrame() {
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

    // <Please choose a room to play>
    JLabel roomIDHelpLabel = new JLabel("Please choose a room to play");
    roomIDHelpLabel.setFont(new Font("Georgia", Font.BOLD, 16));
    roomIDHelpLabel.setHorizontalAlignment(SwingConstants.CENTER);
    roomIDHelpLabel.setBounds(50, 32, 300, 30);
    contentPane.add(roomIDHelpLabel);

    //  <Existing rooms list>
    JLabel roomSearchHelpLabel = new JLabel("Existing rooms lists");
    roomSearchHelpLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
    roomSearchHelpLabel.setHorizontalAlignment(SwingConstants.CENTER);
    roomSearchHelpLabel.setBounds(50, 70, 300, 30);
    contentPane.add(roomSearchHelpLabel);

    // - (wait) room 1
    // - (wait) room 2
    // - ...
    // - (full) room 1
    // - (full) room 2
    // - ...
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setBounds(50, 104, 315, 296);
    contentPane.add(scrollPane);
    roomLabelPanel = new JPanel();
    roomLabelPanel.setBackground(Color.WHITE);
    scrollPane.setViewportView(roomLabelPanel);
    roomLabelPanel.setLayout(null);

    // <TextField for room name>
    roomIDTextField = new JTextField();
    roomIDTextField.setHorizontalAlignment(SwingConstants.CENTER);
    roomIDTextField.setFont(new Font("Georgia", Font.PLAIN, 15));
    roomIDTextField.setBounds(53, 410, 312, 30);
    contentPane.add(roomIDTextField);
    roomIDTextField.setColumns(10);

    // <Refresh> button
    JButton roomRefreshButton = new JButton("Refresh");
    roomRefreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // "search" query to the server
        Client.pendQuery("search");
      }
    });
    roomRefreshButton.setFont(new Font("Georgia", Font.PLAIN, 12));
    roomRefreshButton.setBackground(Color.LIGHT_GRAY);
    roomRefreshButton.setBounds(50, 450, 97, 23);
    contentPane.add(roomRefreshButton);

    // <Join> button
    JButton roomJoinButton = new JButton("Join");
    roomJoinButton.setBackground(Color.LIGHT_GRAY);
    roomJoinButton.setFont(new Font("Georgia", Font.PLAIN, 12));
    roomJoinButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // "join\n{roomID}" query to the server
        Client.pendQuery("join");
        Client.pendQuery(roomIDTextField.getText());
      }
    });
    roomJoinButton.setBounds(159, 450, 97, 23);
    contentPane.add(roomJoinButton);

    // <Create> button
    JButton roomCreateButton = new JButton("Create");
    roomCreateButton.setFont(new Font("Georgia", Font.PLAIN, 12));
    roomCreateButton.setBackground(Color.LIGHT_GRAY);
    roomCreateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // "create\n{roomID}" query to the server
        Client.pendQuery("create");
        Client.pendQuery(roomIDTextField.getText());
      }
    });
    roomCreateButton.setBounds(268, 450, 97, 23);
    contentPane.add(roomCreateButton);

    initializeRoomLabels();
  }
}
