### 使用说明
1. flume对接使用云上Kafka时需要升级flume编译时的Kafka版本，下载flume源码，修改根目录中的pom.xml中<kafka.version>0.9.0.1</kafka.version>为0.10.0.1; 重新编译flume
2. 加入ons-sasl-client-0.1.jar到lib
3. conf文件夹中flume_env.sh文件中通过JAVA_OPTS方式设置jaas文件位置。例如：export JAVA_OPTS="$JAVA_OPTS -Djava.security.auth.login.config=/yourdir/kafka_client_jaas.conf
4. 参照kafka_recv.properties文件使用kafka sink；参照kafka_send.properties文件使用kafka source


