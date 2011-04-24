package com.buy360.isaac.icedemo;

// **********************************************************************
//
// Copyright (c) 2003-2010 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

import Demo._HelloDisp;
import Ice.Current;

public class HelloI extends _HelloDisp {
	private String name = "###$%%#";

	public void shutdown(Ice.Current current) {
		System.out.println(name + " shutting down...");
		current.adapter.getCommunicator().shutdown();
	}

	@Override
	public String sayHello(String user, Current __current) {
		System.out.println(user + " says Hello!");
		return "hello " + user;
	}
}
