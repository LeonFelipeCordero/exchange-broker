package model

import (
	"encoding/json"
	"fmt"
	"testing"
)

func TestOrderFilleMessageParsing(t *testing.T) {
	message := `{"nominals": 0.000000, "instrument": "US011115513099", "institution": "test_institution_12345", "originalPrice": 871.879121, "orderReference": "1a2ce6cb-1224-4cb4-8e90-9c4cad0d67ab", "filledTimestamp": "2024-11-12T19:04:26.729075168+01:00", "externalReference": "fe0abacf-df37-42b5-b175-114ece12764a", "submissionTimestamp": "2024-11-12T18:04:22.255042Z"}`

	orderFilledMessage := OrderFilledMessage{}
	err := json.Unmarshal([]byte(message), &orderFilledMessage)
	if err != nil {
		fmt.Printf("error %e", err)
	}

	fmt.Println(orderFilledMessage.ExternalReference)
}
