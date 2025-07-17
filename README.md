# Book data manager
Flink job to ingest a book event from Kafka, transform it and load into the AWS DynamoDB.

## Prerequisites

1. Run `init.sh` script in the root directory (this step is needed since current DynamoDB connector dependency
   (v5.0.0-1.20) is incompatible with the latest flink-core v2.0.0).
   Once this is fixed and the version 5.1 is released in Maven central this step can be skipped and an `init.sh` 
   script could be removed.
2. Setup Kafka - follow this guide - [Kafka Quickstart](https://kafka.apache.org/quickstart)
3. Create DynamoDB table:
```bash
aws dynamodb create-table \
 --table-name books \
 --attribute-definitions AttributeName=bookId,AttributeType=S \
 --key-schema AttributeName=bookId,KeyType=HASH \
 --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
```
5. Create Kafka topic according to the documentation from the step number 2
6. In IDE run configuration add the following VM options:
```text
-DKAFKA_BROKER=YOUR_KAFKA_BROKER \
-DAWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY_ID \
-DAWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_ACCESS_KEY \
```

where YOUR_AWS_ACCESS_KEY_ID and YOUR_AWS_SECRET_ACCESS_KEY are your AWS credentials and YOUR_KAFKA_BROKER is your Kafka
broker's host.

7. Fire Kafka event from the `sample_data.json` file into the Kafka topic:
```bash
/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic books-topic < /book-data-manager/sample_data.json
```