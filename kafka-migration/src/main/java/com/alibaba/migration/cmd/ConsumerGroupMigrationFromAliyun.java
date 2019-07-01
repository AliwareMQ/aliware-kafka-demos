package com.alibaba.migration.cmd;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.alikafka.model.v20181015.GetConsumerListRequest;
import com.aliyuncs.alikafka.model.v20181015.GetConsumerListResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "ConsumerGroup migration from Aliyun")
public class ConsumerGroupMigrationFromAliyun extends AbstractMigration{

    @Parameter(names = "--sourceAk", description = "ak of the source instance", required = true)
    protected String sourceAk;

    @Parameter(names = "--sourceSk", description = "sk of the source instance", required = true)
    protected String sourceSk;

    @Parameter(names = "--sourceRegionId", description = "region id of the source instance", required = true)
    protected String sourceRegionId;

    @Parameter(names = "--sourceInstanceId", description = "source instance id", required = true)
    protected String sourceInstanceId;

    @Override public String cmdName() {
        return "ConsumerGroupMigrationFromAliyun";
    }

    @Override public Cmd newCmd() {
        return new ConsumerGroupMigrationFromAliyun();
    }

    @Override public void run() {

        try{
            IAcsClient sourceIAcsClient = buildAcsClient(this.sourceAk, this.sourceSk, this.sourceRegionId, this.sourceInstanceId);

            List<GetConsumerListResponse.ConsumerListItem> consumerList = getConsumerGroupListFromAliyun(sourceIAcsClient, sourceRegionId, sourceInstanceId);
            List<String> consumerGroupList = new ArrayList<>();
            consumerList.forEach(consumerListItem -> consumerGroupList.add(consumerListItem.getConsumerId()));
            if (commit) {

                IAcsClient destIAcsClient = buildAcsClient(this.destAk, this.destSk, this.destRegionId, this.destInstanceId);
                createConsumerGroupInYunKafka(destIAcsClient, destRegionId, destInstanceId, consumerGroupList);
            } else {

                logger.info("Will create consumer groups:{}", consumerGroupList);
            }
        } catch (Exception e) {
            logger.error("Error occur while migrate consumer groups.", e);
        }
    }

    private List<GetConsumerListResponse.ConsumerListItem> getConsumerGroupListFromAliyun(IAcsClient iAcsClient, String regionId, String instanceId){

        //构造获取topicList信息request
        GetConsumerListRequest request = new GetConsumerListRequest();
        request.setAcceptFormat(FormatType.JSON);
        //必要参数获取哪个区域的实例
        request.setRegionId(regionId);
        //必要参数实例ID
        request.setInstanceId(instanceId);

        //获取返回值
        try {
            GetConsumerListResponse response = iAcsClient.getAcsResponse(request);
            logInfo(request, response);
            if (200  == response.getCode() && response.getSuccess() != null && response.getSuccess()) {
                List<GetConsumerListResponse.ConsumerListItem> consumerList = response.getConsumerList();
                return consumerList != null ? consumerList : new ArrayList<>();
            } else {
                logger.error("Query consumer group failed, instanceId={}, errorMessage={}", instanceId, response.getMessage());
            }
        } catch (ClientException e) {
            logError(e);
        }
        return new ArrayList<>();
    }
}
