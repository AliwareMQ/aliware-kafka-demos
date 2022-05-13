using System;
using System.Threading;
using Confluent.Kafka;

class Consumer
{
    public static void Main(string[] args)
    {
        var conf = new ConsumerConfig {
            GroupId = "XXX",
            BootstrapServers = "XXX,XXX,XXX",
            SslCaLocation = "XXX/mix-4096-ca-cert",
            SaslMechanism = SaslMechanism.Plain,
            SecurityProtocol = SecurityProtocol.SaslSsl,
            SaslUsername = "XXX",
            SaslPassword = "XXX",
            AutoOffsetReset = AutoOffsetReset.Earliest
        };

        string topic = "XXX";

        using (var c = new ConsumerBuilder<Ignore, string>(conf).Build())
        {
            c.Subscribe(topic);

            CancellationTokenSource cts = new CancellationTokenSource();
            Console.CancelKeyPress += (_, e) => {
                e.Cancel = true;
                cts.Cancel();
            };

            try
            {
                while (true)
                {
                    try
                    {
                        var cr = c.Consume(cts.Token);
                        Console.WriteLine($"Consumed message '{cr.Value}' at: '{cr.TopicPartitionOffset}'.");
                    }
                    catch (ConsumeException e)
                    {
                        Console.WriteLine($"Error occured: {e.Error.Reason}");
                    }
                }
            }
            catch (OperationCanceledException)
            {
                c.Close();
            }
        }
    }
}