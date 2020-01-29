package com.franco.nio.socketDemo.handler;

import com.franco.NioDemoConfig;
import com.franco.common.InternalLogger;
import com.franco.nio.socketDemo.NioFileServer;
import com.franco.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * <h>NioServerHandler</h>
 * This handler can read or send msg.
 * @author franco
 */
public class NioServerHandler implements Runnable, Handler {

    // 客户端
    private NioFileServer.Client client;

    private SelectionKey sk;
    private SocketChannel socketChannel;

    private static final int READ = 0, SEND = 1;

    private int state = READ;

    private ByteBuffer buf = ByteBuffer.allocate(NioDemoConfig.SERVER_HANDLER_BUFSIZE);

    static {
        String destPath = IOUtil.getResourcePath("/");
        File dir = new File(destPath + NioDemoConfig.SERVER_RECEIVE_PATH);
        if(!dir.exists()) {
            dir.mkdir();
        }
    }

    public NioServerHandler(Selector selector, SocketChannel socketChannel) {
        final SocketChannel c = socketChannel;
        try {
            c.configureBlocking(false);
            this.socketChannel = c;
            this.sk = socketChannel.register(selector, 0);
            sk.attach(this);
            sk.interestOps(SelectionKey.OP_READ);
            this.client = new NioFileServer.Client();
            client.remoteAddress =
                    (InetSocketAddress) socketChannel.getRemoteAddress();
            InternalLogger.debug(client.remoteAddress + " connected");
            selector.wakeup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            switch (state) {
                case READ:
                    read();
                    break;
                case SEND:
                    send();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            sk.cancel();
            IOUtil.closeGracefully(socketChannel);
        } catch (CancelledKeyException e) {
            // already cancel selector key
            IOUtil.closeGracefully(socketChannel);
        }
    }

    private void read() throws IOException {
        if(!sk.isReadable()) {
            InternalLogger.error("selection key isn't readable.");
            sk.cancel();
            return;
        }
        client.markStart();
        if(!isInputComplete()) {
            client.preProcess(socketChannel);
        }
        process();
        if(isOutputComplete()) {
            flipState();
            sk.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void send() {
        if(!sk.isWritable()) {
            InternalLogger.error("selection key isn't writeable");
            sk.cancel();
            return;
        }

    }

    private boolean isInputComplete() {
        return client.fileLen > 0 && client.fileName != null;
    }

    private boolean isOutputComplete() {
        return false;
    }

    /**
     * main logic procedure
     */
    @Override
    public void process() {
        buf.clear();
        try {
            int len = -1;
            while ((len = socketChannel.read(buf)) > 0) {
                buf.flip();
                while(buf.hasRemaining())
                    client.oChannel.write(buf);
                buf.clear();
            }
            if(len == -1) {
                InternalLogger.debug(client + " nio 传输文件完成");
                InternalLogger.debug(client + " nio 传输耗时:" + client.getRunTime());
                IOUtil.closeGracefully(client.oChannel);
                sk.cancel();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void flipState() {
        this.state = 1 - state;
    }
}
