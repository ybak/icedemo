package com.buy360.isaac.icedemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import IceUtilInternal.Base64;

public class Router {

    private Map<SocketChannel, SocketChannel> channelMapper = new HashMap<SocketChannel, SocketChannel>();
    private FileOutputStream fileOutputStream;

    private void run() {
        Selector selector = null;
        ServerSocketChannel clientAcceptListenChannel = null;
        try {
            selector = Selector.open();
            clientAcceptListenChannel = ServerSocketChannel.open();
            ByteBuffer buf = ByteBuffer.allocateDirect(Config.BYTEBUFFER_SIZE);
            clientAcceptListenChannel.configureBlocking(false);
            clientAcceptListenChannel.socket().bind(Config.ROUTER_ENDPOINT);

            if (Config.IS_DATA_RECORD) {
                fileOutputStream = new FileOutputStream(new File("IceData.data"));
            }

            while (true) {
                SocketChannel clientRouterChannel = clientAcceptListenChannel.accept();
                if (clientRouterChannel != null) {
                    configChannelPair(selector, clientRouterChannel);
                } else if (selector.select(50) > 0) {
                    handleIO(selector, buf);
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
                if (Config.IS_DATA_RECORD) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
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

    private void handleIO(Selector selector, ByteBuffer buf) throws IOException, InterruptedException {
        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        boolean hasWrite = false;
        while (iterator.hasNext()) {
            SocketChannel readChannel = null;
            SelectionKey selectionKey = iterator.next();
            try {
                if (selectionKey.isValid() && selectionKey.isReadable()) {
                    readChannel = (SocketChannel) selectionKey.channel();
                    SocketChannel writeChannel = channelMapper.get(readChannel);
                    if (readChannel.finishConnect() && writeChannel.finishConnect() && readChannel.isConnected()
                            && writeChannel.isConnected()) {
                        hasWrite = true;
                        buf.clear();
                        int numBytesRead = readChannel.read(buf);
                        if (numBytesRead == -1) {
                            close(readChannel);
                            selectionKey.cancel();
                        } else if (numBytesRead == Config.BYTEBUFFER_SIZE) {
                            writeBuffer(buf, numBytesRead, writeChannel);
                        } else {
                            writeBuffer(buf, numBytesRead, writeChannel);
                            iterator.remove();
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

    private void configChannelPair(Selector selector, SocketChannel clientRouterChannel) throws IOException,
            ClosedChannelException {
        clientRouterChannel.configureBlocking(false);
        clientRouterChannel.register(selector, clientRouterChannel.validOps());

        SocketChannel serverRouterChannel = SocketChannel.open();
        serverRouterChannel.configureBlocking(false);
        serverRouterChannel.connect(Config.SERVER_ENDPOINT);
        serverRouterChannel.finishConnect();
        serverRouterChannel.register(selector, serverRouterChannel.validOps());

        channelMapper.put(clientRouterChannel, serverRouterChannel);
        channelMapper.put(serverRouterChannel, clientRouterChannel);
    }

    private void writeBuffer(ByteBuffer buf, int bytesRead, SocketChannel channel) throws IOException {
        buf.flip();
        int have = 0;
        while (have < bytesRead) {
            have += channel.write(buf);
        }
        if (Config.IS_DATA_RECORD) {
            writeBuf2File(buf, bytesRead);
        }
    }

    private void writeBuf2File(ByteBuffer buf, int bytesRead) throws IOException {
        buf.rewind();
        byte[] bytes = new byte[bytesRead];
        buf.get(bytes);
        fileOutputStream.write(Base64.encode(bytes).getBytes());
        fileOutputStream.write("\n".getBytes());
        fileOutputStream.flush();
    }

    private void close(SocketChannel readChannel) throws IOException {
        if (readChannel != null) {
            SocketChannel writeChannel = channelMapper.get(readChannel);
            channelMapper.remove(readChannel);
            readChannel.close();
            if (writeChannel != null) {
                channelMapper.remove(writeChannel);
                writeChannel.close();
            }
        }
    }

    public static void main(String[] args) {
        Router router = new Router();
        router.run();
    }
}
