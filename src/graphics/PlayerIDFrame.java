/*
 * PlayerIDFrame.java
 * Author: Woosung Song
 *
 * GUI for choosing playerID frame.
 *
 * <Please type your player name>
 *       <Error message>
 *  <TextField for player name>
 *       <Confirm button>
 */
package graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.Client;
import client.Player;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.Dialog.ModalExclusionType;

public class PlayerIDFrame extends JFrame {
  private static final long serialVersionUID = 1L;
  // <Error message> field
  private JLabel playerIDErrorMsg;

  // Set playerIDErrorMsg text
  public void setPlayerIDErrorMsg(String errorMsg) {
    playerIDErrorMsg.setText(errorMsg);
  }

  public PlayerIDFrame() {
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

    // <Please type your player name>
    JLabel playerIDHelpLabel = new JLabel("Please type your player name");
    playerIDHelpLabel.setFont(new Font("Georgia", Font.BOLD, 16));
    playerIDHelpLabel.setHorizontalAlignment(SwingConstants.CENTER);
    playerIDHelpLabel.setBounds(50, 157, 300, 30);
    contentPane.add(playerIDHelpLabel);

    // <Error message>
    playerIDErrorMsg = new JLabel("");
    playerIDErrorMsg.setForeground(Color.RED);
    playerIDErrorMsg.setHorizontalAlignment(SwingConstants.CENTER);
    playerIDErrorMsg.setFont(new Font("Georgia", Font.PLAIN, 12));
    playerIDErrorMsg.setBounds(50, 196, 300, 30);
    contentPane.add(playerIDErrorMsg);

    // <TextField receiving the player name input>
    JTextField playerIDTextField = new JTextField();
    playerIDTextField.setText("");
    playerIDTextField.setHorizontalAlignment(SwingConstants.CENTER);
    playerIDTextField.setFont(new Font("Georgia", Font.PLAIN, 15));
    playerIDTextField.setBounds(50, 225, 300, 35);
    playerIDTextField.setColumns(10);
    playerIDTextField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        // Pressing <ender> in the textfield -> send the textfield content
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          Client.pendQuery(playerIDTextField.getText().strip());
        }
      }
    });
    contentPane.add(playerIDTextField);

    // <Confirm>
    JButton playerIDConfirmButton = new JButton("Confirm");
    playerIDConfirmButton.setBackground(Color.LIGHT_GRAY);
    playerIDConfirmButton.setFont(new Font("Georgia", Font.PLAIN, 12));
    playerIDConfirmButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Pressing <Confirm> button -> send the textfield content
        Client.pendQuery(playerIDTextField.getText().strip());
      }
    });
    playerIDConfirmButton.setBounds(147, 281, 97, 23);
    contentPane.add(playerIDConfirmButton);
  }
}
