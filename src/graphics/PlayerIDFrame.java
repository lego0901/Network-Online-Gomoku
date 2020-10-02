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
	private JPanel contentPane;
	
	public JLabel playerIDErrorMsg;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PlayerIDFrame frame = new PlayerIDFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public PlayerIDFrame() {
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

		JLabel playerIDHelpLabel = new JLabel("Please type your player name");
		playerIDHelpLabel.setFont(new Font("Georgia", Font.BOLD, 16));
		playerIDHelpLabel.setHorizontalAlignment(SwingConstants.CENTER);
		playerIDHelpLabel.setBounds(50, 157, 300, 30);
		contentPane.add(playerIDHelpLabel);

		JTextField playerIDTextField = new JTextField();
		playerIDTextField.setText("");
		playerIDTextField.setHorizontalAlignment(SwingConstants.CENTER);
		playerIDTextField.setFont(new Font("Georgia", Font.PLAIN, 15));
		playerIDTextField.setBounds(50, 225, 300, 35);
		playerIDTextField.setColumns(10);
		playerIDTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					Client.pendQuery(playerIDTextField.getText().strip());
				}
			}
		});
		contentPane.add(playerIDTextField);

		JButton playerIDConfirmButton = new JButton("Confirm");
		playerIDConfirmButton.setBackground(Color.LIGHT_GRAY);
		playerIDConfirmButton.setFont(new Font("Georgia", Font.PLAIN, 12));
		playerIDConfirmButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Client.pendQuery(playerIDTextField.getText().strip());
			}
		});
		playerIDConfirmButton.setBounds(147, 281, 97, 23);
		contentPane.add(playerIDConfirmButton);

		playerIDErrorMsg = new JLabel("");
		playerIDErrorMsg.setForeground(Color.RED);
		playerIDErrorMsg.setHorizontalAlignment(SwingConstants.CENTER);
		playerIDErrorMsg.setFont(new Font("Georgia", Font.PLAIN, 12));
		playerIDErrorMsg.setBounds(50, 196, 300, 30);
		contentPane.add(playerIDErrorMsg);
	}
}
