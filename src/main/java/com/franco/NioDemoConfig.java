package com.franco;

import com.franco.util.ConfigProperties;

/**
 * <h>NioDemoConfig</h>
 *
 * @author franco
 */
public class NioDemoConfig extends ConfigProperties {

    static NioDemoConfig singleton =
            new NioDemoConfig("/server.properties");

    private NioDemoConfig(String filePath) {
        super(filePath);
        super.loadFromFile();
    }

    // static defined properties

}
