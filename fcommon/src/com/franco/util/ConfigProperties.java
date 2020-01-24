package com.franco.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Properties;

/**
 * <h>ConfigProperties</h>
 *
 * @author franco
 */
public class ConfigProperties {

    private String propertyName = "";
    private Properties properties;

    public ConfigProperties() { }

    public ConfigProperties(String filePath) {
        this.propertyName = filePath;
    }

    /**
     * Load properties from file in resource.
     */
    public void loadFromFile() {
        String filePath = IOUtil.getResourcePath(propertyName);
        InputStreamReader reader = null;
        try(InputStream in = new FileInputStream(filePath)) {
            reader = new InputStreamReader(in);
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeGracefully(reader);
        }
    }

    /**
     * read value from properties according to key
     *
     * @param key
     * @return
     */
    public String readProperty(String key) {
        Objects.requireNonNull(key, "the key is null");
        String value = "";
        properties.get(key);
        return value;
    }

    public String getValue(String key) {
        return readProperty(key);
    }
    public int getIntValue(String key) {
        return Integer.parseInt(readProperty(key));
    }
}
