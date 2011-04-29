package com.buy360.isaac.icedemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Router {

    private static final int RouterPort = 10000;
    private static final int ICEP_TYPE_REQUEST = 0;
    private static final int ICEP_TYPE_CLOSE_CONNECTION = 4;
    private static final int ICEP_MESSAGE_TYPE = 8;
    private static final int ICEP_HEADER_LENGTH = 14;
    private static final int ICEP_SIZE_LENGTH = 4;
    private static final int ICEP_HEADER_BEFORE_SIZE_LENGTH = ICEP_HEADER_LENGTH - ICEP_SIZE_LENGTH;

    private static final int TIMEOUT = 3000;
    private static final int ROUTER_PORT = 10004;
    private static final int SERVER_PORT = 10000;
    private static final InetSocketAddress ROUTER_ENDPOINT = new InetSocketAddress(ROUTER_PORT);
    private static final InetSocketAddress SERVER_ENDPOINT = new InetSocketAddress(SERVER_PORT);

    private Map<SocketChannel, SocketChannel> channelMapper = new HashMap<SocketChannel, SocketChannel>();
    private Selector selector;

    private void run() {
        ServerSocketChannel clientAcceptListenChannel = null;
        try {
            clientAcceptListenChannel = ServerSocketChannel.open();
            ByteBuffer buf = ByteBuffer.allocateDirect(1024);
            clientAcceptListenChannel.configureBlocking(false);
            clientAcceptListenChannel.socket().bind(ROUTER_ENDPOINT);

            while (true) {
                SocketChannel clientRouterChannel = clientAcceptListenChannel.accept();
                if (clientRouterChannel != null) {
                    clientRouterChannel.configureBlocking(false);
                    SocketChannel serverRouterChannel = SocketChannel.open();
                    serverRouterChannel.configureBlocking(false);
                    channelMapper.put(clientRouterChannel, serverRouterChannel);
                    channelMapper.put(serverRouterChannel, clientRouterChannel);

                    serverRouterChannel.connect(SERVER_ENDPOINT);
                    serverRouterChannel.finishConnect();

                    clientRouterChannel.register(selector, clientRouterChannel.validOps());
                    serverRouterChannel.register(selector, serverRouterChannel.validOps());
                } else {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    boolean hasWrite = false;
                    while (iterator.hasNext()) {
                        SocketChannel readChannel = null;
                        SelectionKey selectionKey = iterator.next();
                        iterator.remove();
                        try {
                            if (selectionKey.isValid() && selectionKey.isReadable()) {
                                readChannel = (SocketChannel) selectionKey.channel();
                                SocketChannel writeChannel = channelMapper.get(readChannel);
                                if (readChannel.finishConnect() && writeChannel.finishConnect()
                                        && readChannel.isConnected() && writeChannel.isConnected()) {
                                    hasWrite = true;
                                    buf.clear();
                                    int numBytesRead = readChannel.read(buf);
                                    if (numBytesRead == -1) {
                                        close(readChannel);
                                        selectionKey.cancel();
                                    } else {
                                        writeBuffer(buf, numBytesRead, writeChannel);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            close(readChannel);
                            selectionKey.cancel();
                        }
                    }
                    if (!hasWrite) {
                        Thread.sleep(10);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                for (SocketChannel one : new ArrayList<SocketChannel>(channelMapper.keySet())) {
                    close(one);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (clientAcceptListenChannel != null) {
                try {
                    clientAcceptListenChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeBuffer(ByteBuffer buf, int bytesRead, SocketChannel channel) throws IOException {
        buf.flip();
        int have = 0;
        while (have < bytesRead) {
            have += channel.write(buf);
        }
    }

    private void close(SocketChannel readChannel) throws IOException {
        if (readChannel != null) {
            SocketChannel writeChannel = channelMapper.get(readChannel);
            channelMapper.remove(readChannel);
            readChannel.close();
            if (writeChannel != null) {
                writeChannel.close();
            }
        }
    }

    public static void main(String[] args) {
        Router router = new Router();
        router.run();
    }
}
