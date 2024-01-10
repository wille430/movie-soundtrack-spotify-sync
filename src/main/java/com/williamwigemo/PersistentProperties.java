package com.williamwigemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PersistentProperties extends AppProperties {
    private final String filePath;
    private final Properties props;

    public PersistentProperties(String filePath) {
        this.filePath = filePath;
        this.props = new Properties();

        try {
            this.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createFile() throws IOException {
        File file = new File(filePath);

        if (!file.exists()) {
            file.createNewFile();
        }

        return file;
    }

    public void store() {
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            try {
                fileOut = new FileOutputStream(createFile());
            } catch (IOException e1) {
                e1.printStackTrace();
                return;
            }
        }

        try {
            this.props.store(fileOut, "Store properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() throws IOException {
        FileInputStream input;
        try {
            input = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            File propertiesFile = new File(filePath);
            createFile();
            input = new FileInputStream(propertiesFile);
        }

        this.props.load(input);
    }

    @Override
    public void put(String key, String value) {
        this.props.put(key, value);
    }

    @Override
    public String get(String key, String defaultValue) {
        return this.props.getProperty(key, defaultValue);
    }

    @Override
    public void clear() {
        this.props.clear();
    }
}
