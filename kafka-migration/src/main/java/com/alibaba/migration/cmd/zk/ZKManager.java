package com.alibaba.migration.cmd.zk;

import java.io.IOException;
import org.apache.zookeeper.ZooKeeper;

public class ZKManager {

    public static ZooKeeper buildZookeeper(String sourceZkConnect, int sourceZkSessionTimeout) throws IOException {

        ZooKeeper zooKeeper = new ZooKeeper(sourceZkConnect, sourceZkSessionTimeout, new ZooKeeperClientWatcher());
        return zooKeeper;
    }
}
