package com.buy360.isaac.icedemo;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Router {

	public static void main(String[] args) throws Exception {
		ServerSocket servSock = new ServerSocket(10044);

		Socket clntSock = servSock.accept();
		InputStream cin = clntSock.getInputStream();
		OutputStream cout = clntSock.getOutputStream();

		Socket srvSocket = new Socket("192.168.37.128", 10000);
		InputStream sin = srvSocket.getInputStream();
		OutputStream sout = srvSocket.getOutputStream();

		// Server-->Client connection validation
		byte[] buf = new byte[14];
		sin.read(buf);
		cout.write(buf);

		// Client-->Server ice_isA()
		buf = new byte[56];
		cin.read(buf);
		sout.write(buf);

		// Server-->Client Success
		buf = new byte[26];
		sin.read(buf);
		cout.write(buf);
		
		// Client-->Server Hello.SayHello
		buf = new byte[48];
		cin.read(buf);
		sout.write(buf);
		
		// Server-->Client Success
		buf = new byte[42];
		sin.read(buf);
		cout.write(buf);
		
		// Client-->Server Close connection
		buf = new byte[14];
		cin.read(buf);
		sout.write(buf);

		clntSock.close();
		srvSocket.close();
	}
}
