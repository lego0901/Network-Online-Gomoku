package client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ProxyClientWriter {
	final static String READER_HOST = "localhost";
	static int readerPort;

	static PrintWriter proxyWriter;
	static Scanner keyboard;
	
	public static void main(String[] args) {
		Socket proxySocket = null;
		keyboard = new Scanner(System.in);
		
		readerPort = Integer.parseInt(args[0]);
		
		try {
			proxySocket = new Socket();
			proxySocket.connect(new InetSocketAddress(READER_HOST, readerPort));
			
			proxyWriter = new PrintWriter(new OutputStreamWriter(proxySocket.getOutputStream(), StandardCharsets.UTF_8));
			
			while (true) {
				proxyWriter.println(keyboard.nextLine());
				proxyWriter.flush();
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} finally {
			try {
				if (proxySocket != null && !proxySocket.isClosed()) {
					proxySocket.close();
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}
}
