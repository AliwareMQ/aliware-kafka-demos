#!/usr/bin/env bash

currdir=`pwd`

cd ~/go/pkg/mod/cache/  && tar -czf  go-mod-cache.tgz download
mv go-mod-cache.tgz $currdir/packages/

cd $currdir

tar -czf code.tgz ../kafka-*
mv code.tgz $currdir/packages



docker build . -t alibaba-kafka-demo
imageId=`docker images|grep alibaba-kafka-demo|grep latest|head -n 1|awk '{print $3}'`
docker tag $imageId registry.cn-beijing.aliyuncs.com/kafka-test/alibaba-kafka-demo:latest
docker push registry.cn-beijing.aliyuncs.com/kafka-test/alibaba-kafka-demo:latest
