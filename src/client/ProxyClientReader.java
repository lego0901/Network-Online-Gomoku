package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ProxyClientReader {
	final static String SERVER_HOST = "147.46.209.30";
	final static int SERVER_PORT = 20523;
	final static String WRITER_HOST = "localhost";
	static int writerPort;

	static BufferedReader serverReader, proxyReader;
	static PrintWriter serverWriter;

	public static void main(String[] args) {
		Socket serverSocket = null;
		ServerSocket proxyServerSocket = null;
		Socket proxySocket = null;
		
		writerPort = Integer.parseInt(args[0]);
		
		try {
			serverSocket = new Socket();
			proxyServerSocket = new ServerSocket(writerPort);
			serverSocket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
			proxySocket = proxyServerSocket.accept();
			
			System.out.println("Accepted");
			
			serverReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream(), StandardCharsets.UTF_8));
			serverWriter = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream(), StandardCharsets.UTF_8));
			proxyReader = new BufferedReader(new InputStreamReader(proxySocket.getInputStream(), StandardCharsets.UTF_8));
			
			while (true) {
				while (serverReader.ready()) {
					System.out.println(serverReader.readLine());
				}
				while (proxyReader.ready()) {
					String str = proxyReader.readLine();
					System.out.println("(proxy) " + str);
					serverWriter.println(str);
					serverWriter.flush();
				}
				Thread.sleep(10);
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} finally {
			try {
				if (serverSocket != null && !serverSocket.isClosed()) {
					serverSocket.close();
				}
				if (proxySocket != null && !proxySocket.isClosed()) {
					proxySocket.close();
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}
}
