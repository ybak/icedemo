package com.buy360.isaac.icedemo;

import java.net.InetSocketAddress;

public class Config {

    static final int ROUTER_PORT = 10004;
    static final int SERVER_PORT = 10000;
    static final InetSocketAddress ROUTER_ENDPOINT = new InetSocketAddress(ROUTER_PORT);
    static final InetSocketAddress SERVER_ENDPOINT = new InetSocketAddress(SERVER_PORT);
    public static final boolean IS_DATA_RECORD = false;

}
