curl --location --request POST 'http://localhost:1337/fraud-scoring' --header 'Content-Type: application/json' -d "{  \"Transactions\" : [{\"Card Type\" : \"Debit\", \"Location\" : \"Local\", \"Amount\" : 10000, \"Auth Code\" : \"Authorized\"}]}"
