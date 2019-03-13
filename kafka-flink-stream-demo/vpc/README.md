请参考../vpc-ssl目录下的公网访问方式，只需要做如下修改:

1. src/main/resources/kafka.properties的bootstrap.servers属性设置为控制台上获取的默认接入地址
2. src/main/java/**/*Demo.java 源文件代码中注释调 包含SASL_SSL配置的代码行，如:
  ```
   //SASL_SSL 相关的设置, 如果在VPC内访问，可以注释掉下面的代码行
   // xxxx.putAll(AliKafkaConfigurer.saslSSLConfig()); 注释调此行
  ```
3. 这种访问方式不会使用kafka_client_jaas.conf配置，所以也就不需要设置文件中的username,password属性