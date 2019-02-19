package configs

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
)

var (
	configPath string
)

func init() {
	var err error

	workPath, err := os.Getwd()
	if err != nil {
		panic(err)
	}

	configPath = filepath.Join(workPath, "conf")
}

// LoadJsonConfig is used to load config files as json format to config.
// config should be a pointer to structure, if not, panic
func LoadJsonConfig(config interface{}, filename string) {
	var err error
	var decoder *json.Decoder

	file := OpenFile(filename)
	defer file.Close()

	decoder = json.NewDecoder(file)
	if err = decoder.Decode(config); err != nil {
		msg := fmt.Sprintf("Decode json fail for config file at %s. Error: %v", filename, err)
		panic(msg)
	}

	json.Marshal(config)
}

func LoadJsonFile(filename string) (cfg string) {

	file := OpenFile(filename)
	defer file.Close()

	content, err := ioutil.ReadAll(file)
	if err != nil {
		msg := fmt.Sprintf("Read config to string error. file at %s. Error: %v", filename, err)
		panic(msg)
	}

	cfg = string(content)

	return cfg
}

func GetFullPath(filename string) string {
	return filepath.Join(configPath, filename)
}

func OpenFile(filename string) *os.File {
	fullPath := filepath.Join(configPath, filename)

	var file *os.File
	var err error

	if file, err = os.Open(fullPath); err != nil {
		msg := fmt.Sprintf("Can not load config at %s. Error: %v", fullPath, err)
		panic(msg)
	}

	return file
}
