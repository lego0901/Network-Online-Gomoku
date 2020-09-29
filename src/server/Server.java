package server;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

class GomokuServerThread extends Thread {
	Socket socket = null;
	
	public GomokuServerThread(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			PrintWriter writer = new PrintWriter(
					new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

			writer.println("Hello Client");
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

public class Server {
	//public final static String SERVER_HOST = "localhost"; // "147.46.209.30";
	public final static int SERVER_PORT = 20523;

	public static void main(String[] args) {
		Socket socket = null;
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			//serverSocket.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
			System.out.println("Bind complete");
			
			while (true) {
				socket = serverSocket.accept();
				System.out.println("Woosung");
				new GomokuServerThread(socket).start();
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} finally {
			try {
				if (serverSocket != null && !serverSocket.isClosed()) {
					serverSocket.close();
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}
}