package com.buy360.isaac.icedemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;

import org.apache.log4j.Logger;

import IceUtilInternal.Base64;

public class ClientRunner {
    Logger logger = Logger.getLogger(ClientRunner.class);

    private static final int NUM_OF_CLIENT = 10000;
    private int numOfAliveClient = NUM_OF_CLIENT;
    private Selector selector;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    public void run() throws IOException, FileNotFoundException, ClosedChannelException {
        selector = Selector.open();

        for (int i = 0; i < NUM_OF_CLIENT; i++) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("IceData.data")));
            SocketChannel channel = SocketChannel.open(Config.SERVER_ENDPOINT);
            channel.finishConnect();
            channel.configureBlocking(false);
            channel.register(selector, channel.validOps(), bufferedReader);
            logger.debug(channel.socket().getLocalPort() + " Connected.");
        }

        while (true) {
            logger.debug("avaliable keys " + selector.select(50));
            Iterator<SelectionKey> selectKeyiterator = selector.selectedKeys().iterator();
            SelectionKey selectionKey = getRandomEntry(selectKeyiterator);
            if (selectionKey != null) {
                handleSelectionKey(selectionKey);
                selectKeyiterator.remove();
            }
            if (numOfAliveClient == 0) {
                break;
            }
        }
    }

    private SelectionKey getRandomEntry(Iterator<SelectionKey> selectKeyiterator) {
        SelectionKey selectionKey = null;
        synchronized (selector.selectedKeys()) {
            int size = new Random().nextInt(selector.selectedKeys().size()) + 1;
            for (int i = 0; i < size; i++) {
                selectionKey = selectKeyiterator.next();
            }
        }
        return selectionKey;
    }

    private void handleSelectionKey(SelectionKey selectionKey) throws IOException {
        logger.debug("processing " + ((SocketChannel) selectionKey.channel()).socket().getLocalPort());
        if (selectionKey.isReadable()) {
            byteBuffer.clear();
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            int numBytesRead = channel.read(byteBuffer);
            if (numBytesRead == -1) {
                channel.close();
                numOfAliveClient--;
            } else {
                sendIceSignal(selectionKey, channel);
            }
        }
    }

    private void sendIceSignal(SelectionKey selectionKey, SocketChannel channel) throws IOException {
        logger.debug("forwarding " + channel.socket().getLocalPort());
        byteBuffer.clear();
        BufferedReader fileReader = (BufferedReader) selectionKey.attachment();
        fileReader.readLine();
        byte[] validateConnBytes = Base64.decode(fileReader.readLine());
        byteBuffer.put(validateConnBytes);
        byteBuffer.flip();
        channel.write(byteBuffer);
    }

    public static void main(final String[] args) throws Exception {
        ClientRunner clientRunner = new ClientRunner();
        clientRunner.run();
    }

}
