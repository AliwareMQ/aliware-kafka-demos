package configs

type MqConfig struct {
	Topics     []string `json:"topics"`
	Servers    []string `json:"servers"`
	Ak         string   `json:"username"`
	Password   string   `json:"password"`
	ConsumerId string   `json:"consumerGroup"`
	CertFile   string   `json:"cert_file"`
}
