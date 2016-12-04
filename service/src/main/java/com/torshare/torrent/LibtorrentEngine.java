package com.torshare.torrent;

import com.frostwire.jlibtorrent.*;
import com.frostwire.jlibtorrent.alerts.*;
import com.torshare.db.Actions;
import com.torshare.db.Tables;
import com.torshare.tools.DataSources;
import com.torshare.tools.Tools;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by tyler on 12/2/16.
 */
public enum LibtorrentEngine {

    INSTANCE;

    private Logger log = LoggerFactory.getLogger(LibtorrentEngine.class);

    private SessionManager s;

    LibtorrentEngine() {

        System.setProperty("jlibtorrent.jni.path", DataSources.LIBTORRENT_PATH);
        log.info("Starting up libtorrent with version: " + LibTorrent.version());

        s = new SessionManager();

        s.start();
        s.pause();

        try {
            setupAlerts();
//            dhtBootstrap();
        } catch(Throwable e) {
            e.printStackTrace();
        }

    }

    public void addTorrent(TorrentInfo ti) throws IOException {

        log.info("Added torrent: " + ti.name());

        Path tempDir = Files.createTempDirectory("tmp");

        this.s.download(ti, tempDir.toFile());

        log.info("temp dir: " + tempDir.toAbsolutePath().toString());

//
//
//        ArrayList<TcpEndpoint> asdf = s.dhtGetPeers(ti.infoHash(),120);
//
//        System.out.println("dzht peers = " + asdf.size());
    }

    public void fetchMagnetURI(String uri) {
        System.out.println("Fetching the magnet uri, please wait...");
        byte[] data = s.fetchMagnet(uri, 30);

        if (data != null) {
            System.out.println(Entry.bdecode(data));
        } else {
            System.out.println("Failed to retrieve the magnet");
        }

    }

    public void addTorrentsOnStartup() throws IOException {

        Tools.dbInit();

        LazyList<Tables.Torrent> torrents = Tables.Torrent.find("bencode is not null");

        for (Tables.Torrent t : torrents) {
            byte[] data = t.getBytes("bencode");
            addTorrent(TorrentInfo.bdecode(data));
        }

        Tools.dbClose();
    }

    private void setupAlerts() {

        Map<String, Integer> trackerCount = new HashMap<String, Integer>();

        s.addListener(new AlertListener() {
            @Override
            public int[] types() {
                return null;
            }



            @Override
            public void alert(Alert<?> alert) {
                AlertType type = alert.type();

                System.out.println(alert.what());
                System.out.println(alert.message());



                switch (type) {
                    case TORRENT_ADDED:
                        TorrentAddedAlert a = (TorrentAddedAlert) alert;
                        trackerCount.put(a.handle().infoHash().toString(), 0);
                        a.handle().scrapeTracker(); // TDOO need to make this periodic
                        a.handle().forceDHTAnnounce();

                        break;
                    case SCRAPE_REPLY:
                        ScrapeReplyAlert c = (ScrapeReplyAlert) alert;
                        Tools.dbInit();
                        trackerCount.remove(c.handle().infoHash().toString());
                        Actions.saveSeeders(c.handle().infoHash().toString(), c.getComplete(), c.getIncomplete());
                        Tools.dbClose();
                        break;
                    case SCRAPE_FAILED:
                        ScrapeFailedAlert v = (ScrapeFailedAlert) alert;
                        Integer count = trackerCount.get(v.handle().infoHash().toString()) + 1;
                        trackerCount.put(v.handle().infoHash().toString(), count);
                        if (count < v.handle().trackers().size()) {
                            log.info(count + "");
                            v.handle().swig().scrape_tracker(count);
                        }
                        break;
//                    case DHT_GET_PEERS:
//                        DhtGetPeersAlert e = (DhtGetPeersAlert) alert;
//                        System.out.println(e.message());
//                        break;
                    case DHT_GET_PEERS_REPLY:
                        DhtGetPeersReplyAlert d = (DhtGetPeersReplyAlert) alert;
                        System.out.println(d.infoHash());
                        System.out.println(d.numPeers());
                        break;

                }



            }
        });
    }

}