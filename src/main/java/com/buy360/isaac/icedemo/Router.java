package com.buy360.isaac.icedemo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Router implements Runnable {

	private static final int ICEP_TYPE_REQUEST = 0;
	private static final int ICEP_TYPE_CLOSE_CONNECTION = 4;
	private static final int ICEP_MESSAGE_TYPE = 8;
	private static final int ICEP_HEADER_LENGTH = 14;
	private static final int ICEP_SIZE_LENGTH = 4;
	private static final int ICEP_HEADER_BEFORE_SIZE_LENGTH = ICEP_HEADER_LENGTH
			- ICEP_SIZE_LENGTH;

	private Socket clientRouterSocket;
	private Socket routerServerSocket;

	public Router(Socket socket) throws Exception {
		clientRouterSocket = socket;
		routerServerSocket = new Socket("127.0.0.1", 10000);
	}

	@Override
	public void run() {
		try {
			DataInputStream cin = new DataInputStream(new BufferedInputStream(
					clientRouterSocket.getInputStream()));
			DataOutputStream cout = new DataOutputStream(
					new BufferedOutputStream(
							clientRouterSocket.getOutputStream()));

			DataInputStream sin = new DataInputStream(new BufferedInputStream(
					routerServerSocket.getInputStream()));
			DataOutputStream sout = new DataOutputStream(
					new BufferedOutputStream(
							routerServerSocket.getOutputStream()));
			
			byte messageType = ICEP_TYPE_REQUEST;
			while (messageType != ICEP_TYPE_CLOSE_CONNECTION) {
				// Server-->Client
				forwardBytes(sin, cout);
				// Client-->Server
				messageType = forwardBytes(cin, sout);
			}
			clientRouterSocket.close();
			routerServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte forwardBytes(DataInputStream sin, DataOutputStream cout)
			throws IOException {
		byte[] icepHeaderBuf = new byte[ICEP_HEADER_BEFORE_SIZE_LENGTH];
		sin.readFully(icepHeaderBuf);
		cout.write(icepHeaderBuf);

		byte[] icepSizeBuf = new byte[ICEP_SIZE_LENGTH];
		sin.readFully(icepSizeBuf);
		cout.write(icepSizeBuf);

		int icepBufferSize = ByteBuffer.wrap(icepSizeBuf)
				.order(ByteOrder.LITTLE_ENDIAN).getInt();
		if (icepBufferSize > ICEP_HEADER_LENGTH) {
			byte[] icepBodybuf = new byte[icepBufferSize - ICEP_HEADER_LENGTH];
			sin.readFully(icepBodybuf);
			cout.write(icepBodybuf);
		}
		cout.flush();
		return icepHeaderBuf[ICEP_MESSAGE_TYPE];
	}

	public static void main(String[] args) throws Exception {
		ServerSocket serverSock = new ServerSocket(10044);
		ExecutorService threadPool = Executors.newCachedThreadPool();
		while (true) {
			threadPool.execute(new Router(serverSock.accept()));
		}
	}
}
