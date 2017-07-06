package configs

type MqConfig struct {
	Topics     []string `json:"topics"`
	Servers    []string `json:"servers"`
	Ak         string   `json:"ak"`
	Password   string   `json:"password"`
	ConsumerId string   `json:"consumerId"`
	CertFile   string   `json:"cert_file"`
}
