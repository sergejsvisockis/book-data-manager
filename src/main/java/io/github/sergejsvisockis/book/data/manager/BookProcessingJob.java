package io.github.sergejsvisockis.book.data.manager;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.connector.aws.config.AWSConfigConstants;
import org.apache.flink.connector.dynamodb.sink.DefaultDynamoDbElementConverter;
import org.apache.flink.connector.dynamodb.sink.DynamoDbSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

public class BookProcessingJob {

    private static final Logger LOG = LoggerFactory.getLogger(BookProcessingJob.class);

    public static void main(String[] args) throws Exception {
        JobExecutionResult execute = execute();
        LOG.info("Job execution results: {}", execute.getAllAccumulatorResults());
    }

    private static JobExecutionResult execute() {
        KafkaSource<BookEvent> source = KafkaSource.<BookEvent>builder()
                .setBootstrapServers(System.getProperty("KAFKA_BROKER"))
                .setTopics("books-topic")
                .setGroupId("sv-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new BookEventSerializer())
                .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.fromSource(source, WatermarkStrategy.noWatermarks(), "Book Kafka source")
                .map(transformBookEvent())
                .returns(BookEvent.class)
                .sinkTo(getDynamoDbSink());
        try {
            return env.execute("Book ETL");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute the pipeline", e);
        }
    }

    private static MapFunction<BookEvent, BookEvent> transformBookEvent() {
        return event -> {
            event.setTitle(event.getTitle() + "-HaH");
            return event;
        };
    }

    private static DynamoDbSink<BookEvent> getDynamoDbSink() {
        return DynamoDbSink.<BookEvent>builder()
                .setDynamoDbProperties(getDynamoDbProperties())
                .setTableName("books")
                .setElementConverter(new DefaultDynamoDbElementConverter<>())
                .setOverwriteByPartitionKeys(Collections.singletonList("bookId"))
                .build();
    }

    private static Properties getDynamoDbProperties() {
        Properties properties = new Properties();
        properties.setProperty(AWSConfigConstants.AWS_REGION, "eu-north-1");
        properties.setProperty(AWSConfigConstants.AWS_ACCESS_KEY_ID, System.getProperty("AWS_ACCESS_KEY_ID"));
        properties.setProperty(AWSConfigConstants.AWS_SECRET_ACCESS_KEY, System.getProperty("AWS_SECRET_ACCESS_KEY"));
        return properties;
    }
}
