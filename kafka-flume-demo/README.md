### 使用说明
1. flume对接使用云上Kafka时需要升级flume编译时的Kafka版本，下载flume源码，修改根目录中的pom.xml中<kafka.version>0.9.0.1</kafka.version>中的0.9.0.1为0.10.0.0; 重新编译flume
2. 加入ons-sasl-client-0.1.jar到flume的lib文件夹下
3. 参考Kafka-java-demo，拷贝其中的kafka_client_jaas.conf 和 kafka.client.truststore.jks到个人目录
4. conf文件夹中flume_env.sh文件中通过JAVA_OPTS方式设置jaas文件位置。假设 kafka_client_jaas.conf 放在 /home 下面，实际部署时请注意改为自己的路径, 例如：export JAVA_OPTS="$JAVA_OPTS -Djava.security.auth.login.config=/home/kafka_client_jaas.conf，同时在conf/下的properties文件中指明kafka.client.truststore.jks的位置（参照提供的kafka_sink.properties）
4. 参照kafka_sink.properties文件使用kafka sink；参照kafka_source.properties文件使用kafka source
5. flume对env文件和properties的使用命令参考flume官网。

