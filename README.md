# Book data manager
Flink job to ingest a book event from Kafka, transform it and load into the AWS DynamoDB.

## Prerequisites

1. Run `init.sh` script in the root directory (this step is needed because current dynamodb connector dependency have
   missing import in one of the classes, so therefore we need to clone the repo and build the latest snapshot) Once this
   is fixed and the version 5.1 is release in MAven central this stem could be skipped and an `init.sh` script could be
   removed.
2. Setup Kafka - follow this guide - [Kafka Quickstart](https://kafka.apache.org/quickstart)
3. Create DynamoDB table:
```bash
   aws dynamodb create-table \
    --table-name book-keeper \
    --attribute-definitions AttributeName=bookId,AttributeType=S \
    --key-schema AttributeName=bookId,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
   ```
5. Create Kafka topic according to the documentation from the step number 2
6. In IDE run configuration add the following VM options:
```text
   -DAWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY_ID \
   -DAWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_ACCESS_KEY \
```
where YOUR_AWS_ACCESS_KEY_ID and YOUR_AWS_SECRET_ACCESS_KEY are your AWS credentials.