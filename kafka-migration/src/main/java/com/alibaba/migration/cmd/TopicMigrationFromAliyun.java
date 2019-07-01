package com.alibaba.migration.cmd;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.alikafka.model.v20181015.GetTopicListRequest;
import com.aliyuncs.alikafka.model.v20181015.GetTopicListResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Topic migration from aliyun")
public class TopicMigrationFromAliyun extends AbstractMigration {

    @Parameter(names = "--sourceAk", description = "ak of the source instance", required = true)
    protected String sourceAk;

    @Parameter(names = "--sourceSk", description = "sk of the source instance", required = true)
    protected String sourceSk;

    @Parameter(names = "--sourceRegionId", description = "region id of the source instance", required = true)
    protected String sourceRegionId;

    @Parameter(names = "--sourceInstanceId", description = "source instance id", required = true)
    protected String sourceInstanceId;

    @Override public String cmdName() {
        return "TopicMigrationFromAliyun";
    }

    @Override public Cmd newCmd() {
        return new TopicMigrationFromAliyun();
    }

    @Override public void run() {

        try {
            IAcsClient sourceIAcsClient = buildAcsClient(this.sourceAk, this.sourceSk, this.sourceRegionId, this.sourceInstanceId);
            IAcsClient destIAcsClient = buildAcsClient(this.destAk, this.destSk, this.destRegionId, this.destInstanceId);

            List<GetTopicListResponse.TopicListItem> sourceTopicList = getTopicListFromAliyun(sourceIAcsClient, sourceRegionId, sourceInstanceId);
            for (GetTopicListResponse.TopicListItem topicItem : sourceTopicList) {

                int paratitionNum = topicItem.getPartitionNum() == null ? DEFAULT_PARTITION_NUM : topicItem.getPartitionNum();
                boolean isCompactTopic = topicItem.getCompactTopic() == null ? false : topicItem.getCompactTopic();
                if (commit) {

                    createTopicInYunKafka(destIAcsClient, destRegionId, destInstanceId, topicItem.getTopic(), paratitionNum, isCompactTopic);
                } else {

                    logger.info("Will create topic:{}, isCompactTopic:{}, partition number:{}"
                        , topicItem.getTopic(), isCompactTopic, paratitionNum);
                }
            }
        } catch (Exception e) {
            logger.error("Error occur while migrate topics.", e);
        }
    }

    private List<GetTopicListResponse.TopicListItem> getTopicListFromAliyun(IAcsClient iAcsClient, String regionId,
        String instanceId) {
        //构造获取topicList信息request
        GetTopicListRequest request = new GetTopicListRequest();
        request.setAcceptFormat(FormatType.JSON);

        //必要参数获取哪个区域的实例
        request.setRegionId(regionId);
        //必要参数实例ID
        request.setInstanceId(instanceId);

        //获取返回值
        try {
            GetTopicListResponse response = iAcsClient.getAcsResponse(request);
            logInfo(request, response);
            if (200 == response.getCode() && response.getSuccess() != null && response.getSuccess()) {
                List<GetTopicListResponse.TopicListItem> topicListItems = response.getTopicList();
                return topicListItems != null ? topicListItems : new ArrayList<>();
            } else {
                logger.error("Query topicList failed, instanceId={}, errorMessage={}", instanceId, response.getMessage());
            }
        } catch (ClientException e) {
            logError(e);
        }
        return new ArrayList<>();
    }
}
