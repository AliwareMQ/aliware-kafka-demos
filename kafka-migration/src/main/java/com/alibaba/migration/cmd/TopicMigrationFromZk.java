package com.alibaba.migration.cmd;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.migration.cmd.zk.ZKManager;
import com.aliyuncs.IAcsClient;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.util.List;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

@Parameters(commandDescription = "Topic migration from zk")
public class TopicMigrationFromZk extends AbstractMigration {

    private static final String BROKER_TOPIC_PATH = "/brokers/topics";

    private static final String TOPIC_CONFIG_PATH = "/config/topics";

    @Parameter(names = "--sourceZkConnect", description = "zk connect of the source cluster")
    protected String sourceZkConnect;

    @Parameter(names = "--sourceZkSessionTimeout", description = "zk session timeout")
    protected Integer sourceZkSessionTimeout = 15000;

    @Override
    public String cmdName() {
        return "TopicMigrationFromZk";
    }

    @Override
    public Cmd newCmd() {
        return new TopicMigrationFromZk();
    }

    @Override
    public void run() {

        try {
            ZooKeeper zooKeeper = ZKManager.buildZookeeper(sourceZkConnect, sourceZkSessionTimeout);

            List<String> children = zooKeeper.getChildren(BROKER_TOPIC_PATH, false);
            logger.info("Begin to migrate topics:{}", children);
            if (children == null || children.size() == 0) {
                return;
            }
            logger.info("Total topic number:{}", children.size());

            IAcsClient iAcsClient = buildAcsClient(this.destAk, this.destSk, this.destRegionId, this.destInstanceId);
            for (String topic : children) {

                boolean isCompactTopic = isCompactTopic(zooKeeper, topic);
                int partitionNum = getPartitionNum(zooKeeper, topic);
                if (partitionNum == -1) {
                    logger.error("Error occur while getting the partition num of topic:{}, " +
                        "will use default partition num:{} to create topic.", topic, DEFAULT_PARTITION_NUM);
                    partitionNum = DEFAULT_PARTITION_NUM;
                }
                if (commit) {

                    createTopicInYunKafka(iAcsClient, destRegionId, destInstanceId, topic, partitionNum, isCompactTopic);
                } else {

                    logger.info("Will create topic:{}, isCompactTopic:{}, partition number:{}"
                        , topic, isCompactTopic, partitionNum);
                }
            }
        } catch (Exception e) {
            logger.error("Error occur while migrate topics.", e);
        }
    }

    private int getPartitionNum(ZooKeeper zooKeeper, String topic) throws Exception {

        try {
            List<String> partitions = zooKeeper.getChildren(BROKER_TOPIC_PATH + "/" + topic + "/partitions", false);
            return partitions.size();

        } catch (Exception e) {
            return -1;
        }
    }

    private boolean isCompactTopic(ZooKeeper zooKeeper, String topic) throws Exception {

        Stat stat = zooKeeper.exists(TOPIC_CONFIG_PATH + "/" + topic, false);
        if (null == stat) {
            return false;
        }

        byte[] bytes = zooKeeper.getData(TOPIC_CONFIG_PATH + "/" + topic, false, null);
        if (null == bytes) {
            return false;
        }

        JSONObject topicConfig = JSONObject.parseObject(new String(bytes));
        if (topicConfig.getJSONObject("config") == null) {
            return false;
        }
        String cleanupPolicy = topicConfig.getJSONObject("config").getString("cleanup.policy");
        if (null != cleanupPolicy && "compact".equals(cleanupPolicy)) {
            return true;
        }
        return false;
    }

}
