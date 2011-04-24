package com.buy360.isaac.icedemo;

import java.util.Scanner;

import Demo.*;

public class Client extends Ice.Application {
	class ShutdownHook extends Thread {
		public void run() {
			try {
				communicator().destroy();
			} catch (Ice.LocalException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void menu() {
		System.out.println("usage:\n" + "t: send greeting\n"
				+ "s: shutdown server\n" + "x: exit\n" + "?: help\n");
	}

	public int run(String[] args) {
		if (args.length > 0) {
			System.err.println(appName() + ": too many arguments");
			return 1;
		}

		setInterruptHook(new ShutdownHook());

		HelloPrx hello = getHelloProxy();
		System.out.println("what's your name?");
		String user = new Scanner(System.in).nextLine();
		System.out.println(hello.sayHello(user));

		return 0;
	}

	private HelloPrx getHelloProxy() {
		HelloPrx hello = HelloPrxHelper.checkedCast(communicator()
				.stringToProxy("Hello:default -p 10044"));
		return hello;
	}

	public static void main(String[] args) {
		Client app = new Client();
		int status = app.main("Client", args);
		System.exit(status);
	}
}
