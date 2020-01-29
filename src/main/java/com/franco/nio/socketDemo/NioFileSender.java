package com.franco.nio.socketDemo;

import com.franco.NioDemoConfig;
import com.franco.common.InternalLogger;
import com.franco.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * <h>NioFileSender</h>
 *
 * @author franco
 */
public class NioFileSender {

    private String host;
    private int port;

    public NioFileSender() {
        this(NioDemoConfig.SERVER_SOCKET_HOST, NioDemoConfig.SERVER_SOCKET_PORT);
    }

    public NioFileSender(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void sendFile(Path path) {
        File file = path.toFile();
        if(!file.exists()) {
            InternalLogger.error("file doesn't exist ", path);
            return;
        }
        final String fileName = path.getFileName().toString();
        final long fileLen = file.length();
        try (FileChannel channel = new FileInputStream(file).getChannel()) {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(this.host, this.port));
            socketChannel.configureBlocking(false);
            while(!socketChannel.finishConnect()) {

            }
            InternalLogger.debug("Client connect succ");
            byte[] limitedfileNameBytes = new byte[NioDemoConfig.FILENAME_LEN];
            byte[] fileNameBytes = fileName.getBytes();
            System.arraycopy(fileNameBytes, 0, limitedfileNameBytes, 0,
                    Math.min(limitedfileNameBytes.length, fileNameBytes.length));
            ByteBuffer fileNameByBuf = ByteBuffer.allocate(NioDemoConfig.FILENAME_LEN);
            fileNameByBuf.put(limitedfileNameBytes);
            fileNameByBuf.flip();
            socketChannel.write(fileNameByBuf);
            ByteBuffer buf = ByteBuffer.allocate(NioDemoConfig.SENDER_BUFSIZE);
            buf.putLong(fileLen);
            buf.flip();
            socketChannel.write(buf);
            buf.clear();
            int process = 0;
            int len = -1;
            while((len = channel.read(buf)) > 0) {
                buf.flip();
                while(buf.hasRemaining())
                    socketChannel.write(buf);
                process += len;
                InternalLogger.debug(String.format("|      upload file (%d / %d)     |", process, fileLen));
                buf.clear();
            }
            socketChannel.shutdownOutput();
            IOUtil.closeGracefully(socketChannel);
            InternalLogger.debug(" uploading file is done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
