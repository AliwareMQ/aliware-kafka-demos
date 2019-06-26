package com.alibaba.migration.cmd;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.AcsResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.RpcAcsRequest;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Cmd {

    String cmdName();

    Cmd newCmd();

    void run();

    abstract class MigrationCmd implements Cmd {

        protected Logger logger = LoggerFactory.getLogger(getClass());

        @Parameter(names = "--destAk", order = 1, description = "ak of the dest instance", required = true)
        protected String destAk;

        @Parameter(names = "--destSk", order = 2, description = "sk of the dest instance", required = true)
        protected String destSk;

        @Parameter(names = "--destRegionId", description = "region id of the dest instance", order = 3, required = true)
        protected String destRegionId;

        @Parameter(names = "--destInstanceId", description = "dest instance id", order = 4, required = true)
        protected String destInstanceId;

        @Parameter(names = "--commit", description = "if not specify, the command will only check whether can execute.", order = 7)
        protected boolean commit = false;

        protected IAcsClient buildAcsClient(String accessKey, String secretKey, String regionId, String endPointName) {
            //产品code
            String productName = "alikafka";
            String domain = "alikafka." + this.destRegionId + ".aliyuncs.com";
            try {
                DefaultProfile.addEndpoint(endPointName, regionId, productName, domain);
            } catch (ClientException e) {
                e.printStackTrace();
            }

            IClientProfile profile = DefaultProfile.getProfile(regionId, accessKey, secretKey);
            return new DefaultAcsClient(profile);
        }

        protected void logError(ClientException e) {
            logger.error("cmd={} error", cmdName(), e);
        }

        protected void logInfo(RpcAcsRequest request, AcsResponse response) {
            logger.info("cmd={}, request={}, response={}", cmdName(), JSON.toJSONString(request.getHttpContent()), JSON.toJSON(response));
        }
    }

}
