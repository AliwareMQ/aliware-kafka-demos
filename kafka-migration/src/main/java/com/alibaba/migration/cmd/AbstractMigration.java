package com.alibaba.migration.cmd;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.alikafka.model.v20181015.CreateConsumerGroupRequest;
import com.aliyuncs.alikafka.model.v20181015.CreateConsumerGroupResponse;
import com.aliyuncs.alikafka.model.v20181015.CreateTopicRequest;
import com.aliyuncs.alikafka.model.v20181015.CreateTopicResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import java.util.List;

abstract class AbstractMigration extends Cmd.MigrationCmd {

    protected static final int DEFAULT_PARTITION_NUM = 12;

    protected void createTopicInYunKafka(IAcsClient iAcsClient, String regionId, String instanceId, String topic,
        int partitionNum, boolean isCompact) {

        //构造创建topic信息的request
        CreateTopicRequest request = new CreateTopicRequest();
        request.setAcceptFormat(FormatType.JSON);
        //必要参数 实例ID
        request.setInstanceId(instanceId);
        //必要参数 实例所在的区域region
        request.setRegionId(regionId);
        //必要参数 topic 64个字符以内
        request.setTopic(topic);
        //必要参数 remark 64个字符以内
        request.setRemark("autoCreate");
        request.setCompactTopic(isCompact ? "true" : "false");
        request.setPartitionNum(partitionNum + "");
        //获取返回值
        try {
            CreateTopicResponse response = iAcsClient.getAcsResponse(request);
            logInfo(request, response);
            if (200 == response.getCode() && response.getSuccess() != null && response.getSuccess()) {
                logger.info("TopicCreate success, topic={}, partition number={}, isCompactTopic={}", topic, partitionNum, isCompact);
            } else {
                logger.error("TopicCreate failed, topic={}, errorMessage={}", topic, response.getMessage());
            }
        } catch (ClientException e) {
            logError(e);
        }

        sleep();
    }

    protected void createConsumerGroupInYunKafka(IAcsClient iAcsClient, String regionId, String instanceId,
        List<String> consumerList) {

        consumerList.forEach(consumerId -> {

            //构造创建consumerGroup的request
            CreateConsumerGroupRequest request = new CreateConsumerGroupRequest();
            request.setAcceptFormat(FormatType.JSON);
            //必要参数 实例ID
            request.setInstanceId(instanceId);
            //必要参数 实例所在的区域region
            request.setRegionId(regionId);

            //必要参数consumerGroup 64个字符以内
            request.setConsumerId(consumerId);

            //获取返回值
            try {
                CreateConsumerGroupResponse response = iAcsClient.getAcsResponse(request);
                logInfo(request, response);
                if (200 == response.getCode() && response.getSuccess() != null && response.getSuccess()) {
                    logger.info("ConsumerCreate success, consumer group={}", consumerId);
                } else {
                    logger.error("ConsumerCreate failed, consumer group={}, errorMessage={}", consumerId, response.getMessage());
                }
            } catch (ClientException e) {
                logError(e);
            }
            sleep();
        });
    }

    private void sleep() {
        // sleep防止创建频率过高
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }
}
