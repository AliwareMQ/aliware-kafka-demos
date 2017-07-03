FROM docker.elastic.co/logstash/logstash:5.4.0
RUN rm -f /usr/share/logstash/agent/
ADD agent/ /usr/share/logstash/agent/
ADD agent/ons-sasl-client-0.1.jar /usr/share/logstash/vendor/bundle/jruby/1.9/gems/logstash-input-kafka-5.1.6/vendor/jar-dependencies/runtime-jars/ons-sasl-client-0.1.jar
EXPOSE 9600 5044
CMD ["-f", "/usr/share/logstash/agent/logstash.conf"]
