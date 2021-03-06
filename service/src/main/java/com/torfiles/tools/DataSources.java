package com.torfiles.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public class DataSources {

    static final Logger log = LoggerFactory.getLogger(DataSources.class);

    public static final String CODE_DIR = System.getProperty("user.dir");

    public static final String PROPERTIES_FILE = CODE_DIR + "/target/classes/app.properties";

    public static Properties PROPERTIES = Tools.loadProperties(PROPERTIES_FILE);

    public static Boolean SSL = false;

    public static final String CHANGELOG_MASTER = CODE_DIR + "/src/main/resources/liquibase/db.changelog-master.xml";

    public static final Integer EXPIRE_SECONDS = 86400 * 7; // stays logged in for 7 days

    public static final String LIBTORRENT_PATH = CODE_DIR + "/lib/libjlibtorrent.so";

    public static final String UBUNTU_TORRENT = CODE_DIR + "/src/main/resources/ubuntu-16.10-desktop-amd64.iso.torrent";

    public static final String TROTSKY_TORRENT = CODE_DIR + "/src/main/resources/trotsky_fascism.torrent";

}
