package com.buy360.isaac.icedemo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PortRedirect extends Thread {

    private int from;
    private InetSocketAddress target;

    private boolean dolog = false;
    private Map<Integer, FileChannel> logs = new HashMap<Integer, FileChannel>();

    private Map<SocketChannel, SocketChannel> map = new HashMap<SocketChannel, SocketChannel>();

    private boolean doStop = false;

    public PortRedirect(int from, InetSocketAddress target) throws IOException {
        this(from, target, false);
    }

    public PortRedirect(int from, InetSocketAddress target, boolean dolog) throws IOException {
        this.from = from;
        this.target = target;
        this.dolog = dolog;
    }

    private void close(SocketChannel one) throws IOException {
        if (one != null) {
            SocketChannel two = map.get(one);
            map.remove(one);
            one.close();
            closeLog(one);
            if (two != null) {
                map.remove(two);
                two.close();
                closeLog(two);
            }
        }
    }

    private void closeLog(SocketChannel one) throws IOException {
        if (dolog) {
            int code = one.hashCode();
            FileChannel file = logs.get(code);
            if (file != null) {
                file.close();
                logs.remove(code);
            }
        }
    }

    private void createLog(SocketChannel one) throws FileNotFoundException {
        if (dolog) {
            int code = one.hashCode();
            FileChannel channel = new FileOutputStream(String.valueOf(code) + ".log").getChannel();
            logs.put(code, channel);
        }
    }

    private void writeBuf(ByteBuffer buf, ByteChannel two, int numBytesRead) throws IOException {
        buf.flip();
        int have = 0;
        while (have < numBytesRead) {
            have += two.write(buf);
        }
    }

    public void run() {
        ServerSocketChannel ssChannel = null;
        Selector selector = null;

        ByteBuffer buf = ByteBuffer.allocateDirect(1024);

        try {
            selector = Selector.open();

            ssChannel = ServerSocketChannel.open();
            ssChannel.configureBlocking(false);
            ssChannel.socket().bind(new InetSocketAddress(from));

            while (!doStop) {
                SocketChannel one = ssChannel.accept();
                if (one != null) {
                    try {
                        one.configureBlocking(false);
                        SocketChannel two = SocketChannel.open();
                        two.configureBlocking(false);
                        map.put(one, two);
                        map.put(two, one);

                        two.connect(this.target);
                        two.finishConnect();

                        one.register(selector, one.validOps());
                        two.register(selector, two.validOps());

                        if (dolog) {
                            createLog(one);
                            createLog(two);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        close(one);
                    }
                } else if (selector.select(50) > 0) {
                    Iterator it = selector.selectedKeys().iterator();
                    boolean hasWrite = false;
                    while (it.hasNext()) {
                        SelectionKey selKey = (SelectionKey) it.next();

                        it.remove();

                        if (selKey.isValid() && selKey.isReadable()) {
                            try {
                                one = (SocketChannel) selKey.channel();
                                SocketChannel two = map.get(one);
                                if (one.finishConnect() && two.finishConnect() && one.isConnected()
                                        && two.isConnected()) {
                                    hasWrite = true;
                                    buf.clear();
                                    int numBytesRead = one.read(buf);
                                    if (numBytesRead == -1) {
                                        close(one);
                                        selKey.cancel();
                                    } else {
                                        if (dolog) {
                                            writeBuf(buf, logs.get(one.hashCode()), numBytesRead);
                                        }
                                        writeBuf(buf, two, numBytesRead);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                close(one);
                                selKey.cancel();
                            }
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
                for (SocketChannel one : new ArrayList<SocketChannel>(map.keySet())) {
                    close(one);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (ssChannel != null) {
                try {
                    ssChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void close() {
        this.doStop = true;
    }

    public static void main(String[] args) throws Exception {
        PortRedirect redirect = new PortRedirect(Integer.parseInt(args[0]), new InetSocketAddress(args[1],
                Integer.parseInt(args[2])), Boolean.valueOf(args[3]));
        redirect.start();
        System.out.println("ok....");
        if (System.in.read() > 0) {
            redirect.close();
        }
    }

}