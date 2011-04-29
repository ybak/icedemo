package com.buy360.isaac.icedemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import IceUtilInternal.Base64;

public class ClientRunner {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        Runnable clientRunnber = new Runnable() {
            @Override
            public void run() {
                try {
                    Selector selector = Selector.open();
                    SocketChannel channel = SocketChannel.open(Config.ROUTER_ENDPOINT);
                    channel.finishConnect();
                    channel.configureBlocking(false);
                    channel.register(selector, channel.validOps());
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                    FileReader fileReader = new FileReader(new File("IceData.data"));
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    while (true) {
                        selector.select(50);
                        Iterator<SelectionKey> selectKeyiterator = selector.selectedKeys().iterator();
                        if (selectKeyiterator.hasNext()) {
                            SelectionKey selectionKey = selectKeyiterator.next();
                            selectKeyiterator.remove();
                            if (selectionKey.isReadable()) {
                                byteBuffer.clear();
                                SocketChannel readChannel = (SocketChannel) selectionKey.channel();
                                int numBytesRead = readChannel.read(byteBuffer);
                                if (numBytesRead == -1) {
                                    readChannel.close();
                                    break;
                                } else {
                                    byteBuffer.clear();
                                    bufferedReader.readLine();
                                    byte[] validateConnBytes = Base64.decode(bufferedReader.readLine());
                                    byteBuffer.put(validateConnBytes);
                                    byteBuffer.flip();
                                    readChannel.write(byteBuffer);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        long nanoTime = System.nanoTime();
        for (int i = 0; i < 50; i++) {
            cachedThreadPool.execute(clientRunnber);
        }
        cachedThreadPool.shutdown();
        System.out.println(System.nanoTime() - nanoTime);
    }

}
