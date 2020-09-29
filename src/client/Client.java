package client;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Client {
	final static String SERVER_HOST = "147.46.209.30";
	final static int SERVER_PORT = 20523;

	public static void main(String[] args) {
		Socket socket = null;

		try {
			socket = new Socket(SERVER_HOST, SERVER_PORT);
			//socket.connect();
			//socket.connect(InetAddress.getByAddress(SERVER_HOST), SERVER_PORT);
			System.out.println("Connected successfully!");

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			PrintWriter writer = new PrintWriter(
					new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

			writer.println("Hello Server");
			writer.flush();
			System.out.println(reader.readLine());
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} finally {
			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}
}
