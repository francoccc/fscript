package com.franco.nio.io;

import com.franco.common.InternalLogger;
import com.franco.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * <h>FileChannelManager</h>
 * <p> Use FileChannel to manipulate file in system.</p>
 *
 * @author franco
 */
public class FileChannelManager {

    private static final FileChannelManager instance = new FileChannelManager();

    public static FileChannelManager getInstance() {
        return instance;
    }

    public void listFile(String path) throws IOException {
        if(path == null) {
            // avoid null path error in file object;
            return;
        }
        File pFile = new File(path);
        if(pFile.isFile()) {
            InternalLogger.debug("file: " + pFile.getCanonicalPath());
        }
        else if(pFile.isDirectory()) {
            InternalLogger.debug("dir:  " + pFile.getCanonicalPath());
            for(File file : pFile.listFiles()) {
                listFile(file.getPath());
            }
        } else {
            InternalLogger.error("error:" + pFile.getCanonicalPath());
        }
    }

    public void copyTo(String from, String to) {
        Path path = Path.of(from);
        FileChannel oChannel = null;
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)){
            File file = new File(to);
            if(!file.exists())
                file.mkdir();
            Path newFilePath = Path.of(file.getPath()
                    .concat(System.getProperty("file.separator"))
                    .concat(path.getFileName().toString()));
            oChannel = FileChannel.open(newFilePath, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            moveFile(channel, oChannel);
            oChannel.force(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtil.closeGracefully(oChannel);
        }
    }

    public void moveFile(FileChannel from, WritableByteChannel to) throws IOException {
        from.transferTo(0, from.size(), to);
    }
}
