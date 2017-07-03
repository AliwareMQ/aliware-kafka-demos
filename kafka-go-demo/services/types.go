package configs

type MqConfig struct {
	Topics struct {
		Gift string `json:"gift"`
	} `json:"topics"`
	Servers    []string `json:"servers"`
	Ak         string   `json:"ak"`
	Sk         string   `json:"sk"`
	ProducerId string   `json:"producerId"`
	ConsumerId string   `json:"consumerId"`
	CertFile   string   `json:"cert_file"`
	Password   string   `json:"password"`
}
