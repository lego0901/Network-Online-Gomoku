package client;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Client {
	final static String SERVER_HOST = "147.46.209.30";
	final static int SERVER_PORT = 20523;
	
	static BufferedReader reader;
	static PrintWriter writer;
	static Scanner keyboard;
	
	public static void main(String[] args) {
		Socket socket = null;
		keyboard = new Scanner(System.in);

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
			System.out.println("Connected successfully!");

			reader = new BufferedReader(
					new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			writer = new PrintWriter(
					new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

			while (true) {
				if (keyboard.hasNextLine()) {
					String query = keyboard.nextLine();
					if (query.equals("close"))
						break;
					
					writer.println(query);
					writer.flush();
				}
				if (reader.ready()) {
					System.out.println(reader.readLine());
					System.out.flush();
				}
				Thread.sleep(10);
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} catch (InterruptedException iex) {
			try {
				writer.println("close");
				writer.flush();
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
			iex.printStackTrace();
		} finally {
			try {
				writer.println("close");
				writer.flush();
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}
}
