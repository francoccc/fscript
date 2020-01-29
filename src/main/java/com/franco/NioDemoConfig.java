package com.franco;

import com.franco.util.ConfigProperties;

/**
 * <h>NioDemoConfig</h>
 *
 * @author franco
 */
public class NioDemoConfig extends ConfigProperties {

    static NioDemoConfig singleton =
            new NioDemoConfig("/system.properties");

    private NioDemoConfig(String filePath) {
        super(filePath);
        super.loadFromFile();
    }

    // static defined properties
    public static String SERVER_SOCKET_HOST =
            singleton.getValue("socket.server.host");

    public static int SERVER_SOCKET_PORT =
            singleton.getIntValue("socket.server.port");

    public static int SERVER_SOCKET_TIMEOUT =
            singleton.getIntValue("socket.server.timeout");

    public static int SERVER_HANDLER_BUFSIZE =
            singleton.getIntValue("socket.server.handler.bufSize");

    public static String SERVER_RECEIVE_PATH =
            singleton.getValue("socket.server.receivePath");

    public static int FILENAME_LEN =
            singleton.getIntValue("socket.fileName.len");

    public static int SENDER_BUFSIZE =
            singleton.getIntValue("socket.sender.bufSize");

}
