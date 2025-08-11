## Run Demo
1. 安装软件：确保安装了 JDK 8+ 和 Maven 3.2.5+
2. 编写配置：修改 application.yml 里面的 bootstrap-servers、 topic 、group 配置
3. 发送消息：执行 run_demo.sh 后，send_message.sh 即可发送100条消息
4. 消费消息：发送消息后，观察日志输出
5. ![img.png](img.png)
6. 如图类似消费日志，表示消费成功。






## 注意事项
#### 该 demo 依赖的 spring-kafka，对应的 kafka-clients 为 3.x。
#### 我们建议 kafka-clients 保持 2.4 以及以上，2.4 版本及以上默认支持 Sticky Partition Strategy。
#### 低于 2.4 版本的客户端可能会有某些特性不支持的情况。
#### 如：2.0.1 版本不支持 zstd 压缩算法。

	


