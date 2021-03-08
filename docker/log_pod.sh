kubectl exec -it   `kubectl get pod|grep alibaba-kafka-demo|awk '{print $1}'` bash
