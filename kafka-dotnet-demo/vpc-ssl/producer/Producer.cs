using System;
using Confluent.Kafka;

class Producer
{
    public static void Main(string[] args)
    {
        var conf = new ProducerConfig {
            BootstrapServers = "XXX,XXX,XXX",
            SslCaLocation = "XXX/ca-cert.pem",
            SaslMechanism = SaslMechanism.Plain,
            SecurityProtocol = SecurityProtocol.SaslSsl,
            SaslUsername = "XXX",
            SaslPassword = "XXX",
        };

        Action<DeliveryReport<Null, string>> handler = r =>
            Console.WriteLine(!r.Error.IsError
                ? $"Delivered message to {r.TopicPartitionOffset}"
                : $"Delivery Error: {r.Error.Reason}");

        string topic ="XXX";

        using (var p = new ProducerBuilder<Null, string>(conf).Build())
        {
            for (int i=0; i<100; ++i)
            {
                p.Produce(topic, new Message<Null, string> { Value = i.ToString() }, handler);
                p.Flush(TimeSpan.FromSeconds(10));
            }
            //p.Flush(TimeSpan.FromSeconds(10));
        }
    }
}