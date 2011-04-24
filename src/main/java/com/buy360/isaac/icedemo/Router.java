package com.buy360.isaac.icedemo;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class Router {
	private static final int BUFSIZE = 10000;

	public static void main(String[] args) throws Exception {
		ServerSocket servSock = new ServerSocket(10044);

		int recvMsgSize;
		byte[] receiveBuf = new byte[BUFSIZE];

		while (true) {
			Socket clntSock = servSock.accept();
			SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
			System.out.println("Handling client at " + clientAddress);

			InputStream in = clntSock.getInputStream();
			OutputStream out = clntSock.getOutputStream();

			System.out.println(in.read());
			while ((recvMsgSize = in.read(receiveBuf)) != -1) {
				out.write(receiveBuf, 0, recvMsgSize);
			}

			System.out.println("recvMsgSize is " + recvMsgSize);
			clntSock.close();
		}
	}
}
