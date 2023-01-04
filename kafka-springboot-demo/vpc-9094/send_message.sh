#!/bin/bash
echo "Ready to send 100 messages"

for i in {1..100} ; do
    curl http://127.0.0.1:8080/kafka/send
    echo ""
done