package configs

type MqConfig struct {
	Topics     []string `json:"topics"`
	Servers    []string `json:"servers"`
	ConsumerId string   `json:"consumerGroup"`
}
