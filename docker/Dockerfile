FROM reg.docker.alibaba-inc.com/private-paas/alios-7u-kafka-base:1.0.0

RUN yum install git -y
RUN yum install go -y
RUN yum install maven -y

COPY kafka-tools/init_dirs.sh /tmp/
RUN chmod a+x /tmp/*.sh
RUN bash /tmp/init_dirs.sh

COPY kafka-tools/*.sh /home/admin/kafka-tools/
RUN chmod a+x /home/admin/kafka-tools/*.sh

ADD packages/go-mod-cache.tgz  /home/admin/go/pkg/mod/cache

ADD packages/code.tgz  /home/admin/aliware-kafka-demos



WORKDIR /home/admin
CMD ["bash", "-c", "/home/admin/kafka-tools/start_test.sh"]


