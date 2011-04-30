package com.buy360.isaac.icedemo;

import java.net.InetSocketAddress;

public class Config {

    public static final int ROUTER_PORT = 10004;
    public static final int SERVER_PORT = 10000;
    public static final InetSocketAddress ROUTER_ENDPOINT = new InetSocketAddress(ROUTER_PORT);
    public static final InetSocketAddress SERVER_ENDPOINT = new InetSocketAddress(SERVER_PORT);
    public static final boolean IS_DATA_RECORD = false;
    public static final int BYTEBUFFER_SIZE = 1024;

    public static final int ICEP_TYPE_REQUEST = 0;
    public static final int ICEP_TYPE_CLOSE_CONNECTION = 4;
    public static final int ICEP_MESSAGE_TYPE = 8;
    public static final int ICEP_HEADER_LENGTH = 14;
    public static final int ICEP_SIZE_LENGTH = 4;
    public static final int ICEP_HEADER_BEFORE_SIZE_LENGTH = ICEP_HEADER_LENGTH - ICEP_SIZE_LENGTH;

}
