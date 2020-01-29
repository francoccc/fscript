package com.franco.nio.socketDemo;

import com.franco.nio.io.FileChannelManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * <h>FileSender</h>
 * <p>A client to sender file with designated file path</p>
 *
 * @author franco
 */
public class FileSender {

    private static int EOF = -1;

    /**
     * success
     */
    public static int SUCCESS = 0;

    /**
     * File don't exist
     */
    public static int FILE_NOT_EXIST = 1;

    public static int EXCEPTION = 2;

    private String host;
    private int port;
    public FileSender(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public int sendFile(Path path) {
        File file = path.toFile();
        if(!file.exists())
            return FILE_NOT_EXIST;
        String fileName = file.getName();
        SocketChannel socketChannel = null;
        try (FileChannel fChannel = FileChannel.open(path, StandardOpenOption.READ)){
            socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
            ByteBuffer buf = ByteBuffer.allocate(36);
            byte[] bytes = new byte[32];
            byte[] fBytes = fileName.getBytes();
            int len = Math.min(fBytes.length, bytes.length);
            buf.putInt(len);
            System.arraycopy(fBytes, 0, bytes, 0, len);
            buf.put(bytes);
            buf.flip();
            socketChannel.write(buf);
            FileChannelManager.getInstance().moveFile(fChannel, socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
            return EXCEPTION;
        } finally {
            if(socketChannel != null) {
                try {
                    socketChannel.close();
                } catch (IOException e) { }
            }
        }
        return SUCCESS;
    }
}
