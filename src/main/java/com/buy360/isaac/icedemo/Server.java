package com.buy360.isaac.icedemo;

import java.util.Arrays;


public class Server extends Ice.Application {
	public int run(String[] args) {
		if (args.length > 0) {
			System.err.println(appName() + ": too many arguments");
			return 1;
		}

		Ice.ObjectAdapter adapter = communicator().createObjectAdapter("Hello");
		Ice.Properties properties = communicator().getProperties();
		Ice.Identity id = communicator().stringToIdentity(
				properties.getProperty("Identity"));
		adapter.add(new HelloI(properties.getProperty("Ice.ProgramName")), id);
		adapter.activate();
		communicator().waitForShutdown();
		return 0;
	}

	static public void main(String[] args) {
		System.out.println(Arrays.toString(args));
		Server app = new Server();
		int status = app.main("Server", args);
		System.exit(status);
	}
}
