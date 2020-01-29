package com.franco.nio.socketDemo;

import com.franco.NioDemoConfig;
import com.franco.common.InternalLogger;
import com.franco.nio.socketDemo.handler.Handler;
import com.franco.nio.socketDemo.handler.NioServerHandler;
import com.franco.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.rmi.server.ServerCloneException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h>NioFileServer</h>
 * use reactor model
 *
 * @author franco
 */
public class NioFileServer implements Runnable {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private int port;

    public NioFileServer() {
        this(NioDemoConfig.SERVER_SOCKET_PORT);
    }

    public NioFileServer(int port) {
        this.port = port;
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = serverSocketChannel.socket();
            serverSocket.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            SelectionKey sk =
                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            // add Acceptor
            sk.attach(new Acceptor());
            InternalLogger.debug("server listening port: " + this.port);
            while(!Thread.interrupted()) {
                selector.select(NioDemoConfig.SERVER_SOCKET_TIMEOUT);
                Set<SelectionKey> interestKeys = selector.keys();
                if(interestKeys.isEmpty()) {
                    continue;
                }
                Iterator<SelectionKey> iter = interestKeys.iterator();
                while(iter.hasNext()) {
                    dispatch(iter.next());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Use this reactor dispatching runnable to
     * unique worker.These workers will do designated working
     * about accepting the channel or doing file download</p>
     *
     * @param sk selectionKey to get attachment
     */
    private void dispatch(SelectionKey sk) {
        Runnable r = (Runnable) sk.attachment();
        if(r != null) {
            r.run();
        }
    }

    class Acceptor implements Runnable {

        @Override
        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if(socketChannel != null) {
                    new NioServerHandler(selector, socketChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Client info
     *
     * @author franco
     */
    public static class Client {

        private static AtomicInteger a = new AtomicInteger();

        private static Charset charset = StandardCharsets.UTF_8;

        private int id = a.incrementAndGet();

        // 文件名
        public String fileName;

        // 文件大小
        public long fileLen;

        // 开始时间
        public long startTime;

        // 客户端地址
        public InetSocketAddress remoteAddress;

        // 文件通道
        public FileChannel oChannel;

        public void markStart() {
            this.startTime = System.currentTimeMillis();
        }

        public long getRunTime() {
            return System.currentTimeMillis() - this.startTime;
        }

        // 预处理
        public void preProcess(SocketChannel channel) throws IOException {
            ByteBuffer fileNameByBuf = ByteBuffer.allocate(NioDemoConfig.FILENAME_LEN);
            ByteBuffer fileLenByBuf = ByteBuffer.allocate(8);
            channel.read(new ByteBuffer[]{fileNameByBuf, fileLenByBuf}, 0, 2);
            fileNameByBuf.flip(); fileLenByBuf.flip();
            this.fileName = charset.decode(fileNameByBuf).toString().trim();
            String destPath = IOUtil.getResourcePath(NioDemoConfig.SERVER_RECEIVE_PATH);
            String fullName = destPath + IOUtil.fileSeparator + fileName;
            File file = new File(fullName);
            this.oChannel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            this.oChannel.force(true);
            this.fileLen = fileLenByBuf.getLong();
            InternalLogger.info( this + " nio 传输文件开始");
        }

        @Override
        public String toString() {
            return String.format("client:[%d,%s,%d]", id, fileName, fileLen);
        }
    }
}
