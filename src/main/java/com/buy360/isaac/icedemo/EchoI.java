package com.buy360.isaac.icedemo;

import Demo._EchoDisp;
import Ice.Current;

public class EchoI extends _EchoDisp {

	@Override
	public void shutdown(Current __current) {
		System.out.println(" shutting down...");
		__current.adapter.getCommunicator().shutdown();
	}

	@Override
	public String doEcho(String text, Current __current) {
		String ehcoText = "Server echo: " + text;
		System.out.println(ehcoText);
		return ehcoText;
	}

}
