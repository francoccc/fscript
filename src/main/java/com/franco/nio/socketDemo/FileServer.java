package com.franco.nio.socketDemo;

import com.franco.NioDemoConfig;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * <h>FileServer</h>
 * <p>A server receive new file from sender</p>
 *
 * @author franco
 */
public class FileServer implements Runnable {

    private String filePath;

    private ServerSocketChannel serverSocketChannel;

    public FileServer() {
        this(NioDemoConfig.SERVER_SOCKET_HOST,
                NioDemoConfig.SERVER_SOCKET_PORT);
    }

    public FileServer(String path, int port) {
        this.filePath = path;
        File file = new File(path);
        if(!file.exists())
            file.mkdir();
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
        } catch (IOException e) { }
    }

    @Override
    public void run() {
        while(true) {
            FileChannel fChannel = null;
            try {
                SocketChannel channel = serverSocketChannel.accept();
                ByteBuffer buf = ByteBuffer.allocate(1024);
                int len = 0;
                String fileName = "unname";
                while(channel.read(buf) != -1) {
                    System.out.println("capacity:" + buf.capacity());
                    System.out.println("limit:" + buf.limit());
                    System.out.println("position:" + buf.position());
                    buf.flip();
                    if(len == 0) {
                        len = buf.getInt();
                        System.out.println("len:" + len);
                        byte[] bytes = new byte[32];
                        // get fileName
                        buf.get(bytes, 0, bytes.length);
                        fileName = new String(bytes, 0, len);
                        filePath = filePath + System.getProperty("file.separator") + fileName;
                        fChannel = FileChannel.open(Path.of(filePath), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                        fChannel.force(true);
                        buf.clear();
                        continue;
                    }
                    while(buf.hasRemaining())
                        fChannel.write(buf);
                    buf.clear();
                }
                System.out.println("restore success");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(fChannel != null)
                        fChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("block ready to next file");
            }
        }
    }
}
