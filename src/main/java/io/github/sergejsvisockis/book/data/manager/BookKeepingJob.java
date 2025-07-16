package io.github.sergejsvisockis.book.data.manager;

import io.github.sergejsvisockis.bookmanager.lib.BookEvent;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.aws.config.AWSConfigConstants;
import org.apache.flink.connector.dynamodb.sink.DefaultDynamoDbElementConverter;
import org.apache.flink.connector.dynamodb.sink.DynamoDbSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class BookKeepingJob {

    private static final Logger LOG = LoggerFactory.getLogger(BookKeepingJob.class);

    public static void main(String[] args) throws Exception {
        JobExecutionResult execute = execute();
        LOG.info("Accumulated results: {}", execute.getAllAccumulatorResults());
    }

    private static JobExecutionResult execute() {
        KafkaSource<BookEvent> source = KafkaSource.<BookEvent>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("book-keeping")
                .setGroupId("sv-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new BookEventSerializer())
                .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.fromSource(source, WatermarkStrategy.noWatermarks(), "Book Kafka source")
                .map(event -> {
                    LOG.info("En event received: {}", event);
                    event.setTitle(event.getTitle() + "-HaH");
                    return event;
                })
                .returns(BookEvent.class)
                .sinkTo(DynamoDbSink.<BookEvent>builder()
                        .setDynamoDbProperties(getDynamoDbProperties())
                        .setTableName("book-keeper")
                        .setElementConverter(new DefaultDynamoDbElementConverter<>())
                        .build());
        try {
            return env.execute();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute the pipeline", e);
        }
    }

    private static Properties getDynamoDbProperties() {
        Properties properties = new Properties();
        properties.setProperty(AWSConfigConstants.AWS_REGION, "eu-north-1");
        properties.setProperty(AWSConfigConstants.AWS_ACCESS_KEY_ID, System.getProperty("AWS_ACCESS_KEY_ID"));
        properties.setProperty(AWSConfigConstants.AWS_SECRET_ACCESS_KEY, System.getProperty("AWS_SECRET_ACCESS_KEY"));
        return properties;
    }
}
