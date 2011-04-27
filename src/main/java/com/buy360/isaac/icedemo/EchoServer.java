package com.buy360.isaac.icedemo;

import Ice.Identity;

public class EchoServer extends Ice.Application {
	public int run(String[] args) {
		Ice.ObjectAdapter adapter = communicator()
				.createObjectAdapterWithEndpoints("Echo", "default -p 10001");
		adapter.add(new EchoI(), new Identity("Echo", null));
		adapter.activate();
		communicator().waitForShutdown();
		return 0;
	}

	static public void main(String[] args) {
		EchoServer app = new EchoServer();
		int status = app.main("Server", args);
		System.exit(status);
	}

}
