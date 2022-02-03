package main

import (
	"crypto/tls"
	"encoding/json"
	"flag"
	"fmt"
	"github.com/kyokomi/emoji"
	"io"
	"log"
	"net/http"
	"os"
	"strconv"
	"time"
)

func main() {
	var (
		logFormatter string
		logLevel     string
	)

	flag.StringVar(&logFormatter, "log-formatter", "json", "Log formatter (text|json)")
	flag.StringVar(&logLevel, "log-level", "info", "Log level (trace|debug|info|warn|error|fatal|panic)")
	flag.Parse()

	log.SetOutput(os.Stderr)

	URL := getenv("URL", "")
	Interval := getenv("INTERVAL", "2s")
	Sequence := getenv("SEQUENCE", "serial")
	Threads, _ := strconv.Atoi(getenv("THREADS", "4"))

	interval, err := time.ParseDuration(Interval)
	if err != nil {
		log.Printf("failed to parse interval: %v\n", err)
		return
	}

	fmt.Printf("Generating load on URL: %s every %s\n", URL, Interval)

	httpClient := http.Client{
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{
				InsecureSkipVerify: true,
			},
		},
	}

	for {
		if Sequence == "serial" {
			fmt.Printf("Sending request to %s\n", URL)
			if err := request(URL, httpClient); err != nil {
				log.Printf("failed to send request: %v\n", err)
				time.Sleep(interval)
				continue
			}
			time.Sleep(interval)
		} else{
			//var wg        sync.WaitGroup

			for i := 1; i <= Threads; i++ {
				//wg.Add(1)
				go func() {
					fmt.Printf("[Thread: %s]: Sending request to %s\n", getOrdinal(i), URL)
					if err := request(URL, httpClient); err != nil {
						log.Printf("failed to send request: %v\n", err)
					}
					//wg.Done()
				}()
			}
			//.Wait()
			time.Sleep(interval)
		}
	}
}

func request(URL string, httpClient http.Client) error{
	req, err := http.NewRequest("GET", URL, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %v", err)
	}

	res, err := httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %v", err)
	}

	defer res.Body.Close()

	body, err := io.ReadAll(res.Body)
	if err != nil {
		return fmt.Errorf("failed to read response body: %v", err)
	}

	var orders []Order
	if err := json.Unmarshal(body, &orders); err != nil{
		return fmt.Errorf("failed to unmarshal response body: %v", err)
	}

	if res.StatusCode != http.StatusOK{
		log.Printf("Status Code: %d %v\n", res.StatusCode, emoji.Sprint(":cry:"))
	} else{
		if len(orders) > 0 {
			if orders[0].Name == "mobile" || orders[0].Name == "Bluetooth Keyboard" {
				fmt.Printf("Status Code: %d %v\n", res.StatusCode, emoji.Sprint(":smile:"))
			} else {
				fmt.Printf("[Fallback]: Status Code: %d %v\n", res.StatusCode, emoji.Sprint(":slight_smile:"))
			}
		}
	}
	return nil
}

func getenv(key string, defaultValue string) string {
	value := os.Getenv(key)
	if value == "" {
		value = defaultValue
	}
	return value
}

type Order struct {
	  Name string `json:"name" bson:"name"`
}

// getOrdinal returns the ordinal equivalent of a number
// i.e, for 1, 2 and 3 --> 1st, 2nd and 3rd
func getOrdinal(n int) string {
	suffix := "th"
	switch n % 10 {
	case 1:
		if n%100 != 11 {
			suffix = "st"
		}
	case 2:
		if n%100 != 12 {
			suffix = "nd"
		}
	case 3:
		if n%100 != 13 {
			suffix = "rd"
		}
	}
	return strconv.Itoa(n) + suffix
}
