#!/bin/bash

# Endpoint URL to send HTTP requests to
URL="http://localhost:15001/v1/store"

# Number of requests to send
REQUESTS=1200

# Time to sleep between requests (in seconds)
SLEEP_TIME=0.0001

# Start time of the script
START_TIME=$(date +%s)

# Send HTTP requests and count successful responses
SUCCESS_COUNT=0
for (( i=1; i<=$REQUESTS; i++ )); do
    # Generate random key and value
    KEY=$(head -c 8 /dev/urandom | base64 | tr -dc 'a-zA-Z0-9')
    VALUE=$((RANDOM % 1000 + 1))

    # Create JSON payload
    JSON="{\"key\": \"$KEY\", \"value\": \"$VALUE\", \"cmd\": \"set\"}"

    # Send HTTP request
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/json" -d "$JSON" $URL)
    if [ $RESPONSE -eq 200 ]; then
        SUCCESS_COUNT=$((SUCCESS_COUNT+1))
    fi
#     sleep $SLEEP_TIME
done

# End time of the script
END_TIME=$(date +%s)

# Calculate QPS and print the result
ELAPSED_TIME=$(echo "scale=9; ($END_TIME - $START_TIME)" | bc)
QPS=$(echo "scale=2; $SUCCESS_COUNT / $ELAPSED_TIME" | bc)
echo "QPS: $QPS"
