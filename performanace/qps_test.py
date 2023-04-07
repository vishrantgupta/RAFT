import os
import json
import random

# Generate random values for key and value fields
key = ''.join(random.choices('abcdefghijklmnopqrstuvwxyz', k=5))
value = str(random.randint(1, 100))

# Create a JSON payload with the random values
payload = {
    "key": key,
    "value": value,
    "cmd": "set"
}

# Convert the payload to a JSON string
payload_str = json.dumps(payload)

# Write the payload to a temporary file
with open('temp_payload.txt', 'w') as f:
    f.write(payload_str)

# Send the AB request and capture the results in a CSV file
os.system(f"ab -n 15000 -c 100 -p temp_payload.txt -T application/json -e results.csv http://localhost:15000/v1/store")

# Delete the temporary file
os.remove('temp_payload.txt')

# Read the results from the CSV file
with open('results.csv', 'r') as f:
    results = f.read()

print(results)
