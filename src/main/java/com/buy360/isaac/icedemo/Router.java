package com.buy360.isaac.icedemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

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

    private ByteBuffer buf = ByteBuffer.allocateDirect(1024);
    private Selector clientSelector;
    private Selector serverSelector;

    public static void main(String[] args) throws Exception {
        Router router = new Router();
        router.run();
    }

    private void run() throws IOException, ClosedChannelException {
        clientSelector = openClientSelector();
        serverSelector = openServerSelector();

        while (true) {
            if (clientSelector.select(TIMEOUT) == 0 && serverSelector.select(TIMEOUT) == 0) {
                System.out.print(".");
                continue;
            }

            Iterator<SelectionKey> serverSelectorKeyIterator = serverSelector.selectedKeys().iterator();
            Iterator<SelectionKey> clientSelectorkeyIterator = clientSelector.selectedKeys().iterator();

            while (clientSelectorkeyIterator.hasNext() && serverSelectorKeyIterator.hasNext()) {
                SelectionKey clientKey = clientSelectorkeyIterator.next();
                SelectionKey serverKey = serverSelectorKeyIterator.next();

                if (clientKey.isAcceptable() && serverKey.isReadable()) {
                    handleClientConnection(clientKey, serverKey);
                    clientSelectorkeyIterator.remove();
                    serverSelectorKeyIterator.remove();
                }
                
                if (clientKey.isWritable() && serverKey.isReadable()) {
                    handleServerRequest(clientKey, serverKey);
                    clientSelectorkeyIterator.remove();
                    serverSelectorKeyIterator.remove();
                }

                if (clientKey.isReadable() && serverKey.isWritable()) {
                    byte returnType = handleClientRequset(clientKey, serverKey);
                    clientSelectorkeyIterator.remove();
                    serverSelectorKeyIterator.remove();
                    if (ICEP_TYPE_CLOSE_CONNECTION == returnType) {
                        clientKey.channel().close();
//                        serverKey.channel().close();
                    }
                }


            }
        }
    }

    private Selector openServerSelector() throws IOException, ClosedChannelException {
        Selector serverSelector = Selector.open();
        SocketChannel serverRouterChan = SocketChannel.open();
        if (!serverRouterChan.connect(new InetSocketAddress(SERVER_PORT))) {
            while (!serverRouterChan.finishConnect()) {
                System.out.print(".");
            }
        }
        serverRouterChan.configureBlocking(false);
        serverRouterChan.register(serverSelector, SelectionKey.OP_READ);
        return serverSelector;
    }

    private Selector openClientSelector() throws IOException, ClosedChannelException {
        Selector clientSelector = Selector.open();
        ServerSocketChannel clientRouterListenChannel = ServerSocketChannel.open();
        clientRouterListenChannel.socket().bind(new InetSocketAddress(ROUTER_PORT));
        clientRouterListenChannel.configureBlocking(false);
        clientRouterListenChannel.register(clientSelector, SelectionKey.OP_ACCEPT);
        return clientSelector;
    }

    private void handleClientConnection(SelectionKey clientKey, SelectionKey serverKey) throws IOException {
        SocketChannel serverRouterChan = (SocketChannel) serverKey.channel();
        buf.clear();
        int bytesRead = serverRouterChan.read(buf);
        SocketChannel clientRouterChannel = ((ServerSocketChannel) clientKey.channel()).accept();
        writeBuffer(bytesRead, clientRouterChannel);
        clientRouterChannel.configureBlocking(false);
        clientRouterChannel.register(clientKey.selector(), SelectionKey.OP_READ);
        serverKey.interestOps(SelectionKey.OP_WRITE);
    }

    private byte handleClientRequset(SelectionKey clientKey, SelectionKey serverKey) throws IOException {
        SocketChannel clientChan = (SocketChannel) clientKey.channel();
        buf.clear();
        int bytesRead = clientChan.read(buf);
        SocketChannel serverRouterChannel = (SocketChannel) serverKey.channel();
        writeBuffer(bytesRead, serverRouterChannel);
        clientKey.interestOps(SelectionKey.OP_WRITE);
        serverKey.interestOps(SelectionKey.OP_READ);
        return buf.get(ICEP_MESSAGE_TYPE);
    }

    private void handleServerRequest(SelectionKey clientKey, SelectionKey serverKey) throws IOException {
        SocketChannel serverRouterChan = (SocketChannel) serverKey.channel();
        buf.clear();
        int bytesRead = serverRouterChan.read(buf);
        SocketChannel clientChannel = (SocketChannel) clientKey.channel();
        writeBuffer(bytesRead, clientChannel);
        serverKey.interestOps(SelectionKey.OP_WRITE);
        clientKey.interestOps(SelectionKey.OP_READ);
    }

    private void writeBuffer(int bytesRead, SocketChannel channel) throws IOException {
        buf.flip();
        int have = 0;
        while (have < bytesRead) {
            have += channel.write(buf);
        }
    }
}
