package com.alibaba.migration.cmd.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZooKeeperClientWatcher implements Watcher {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override public void process(WatchedEvent event) {
        logger.info("Receive event:" + event);
    }
}
