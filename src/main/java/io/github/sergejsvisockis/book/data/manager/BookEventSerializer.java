package io.github.sergejsvisockis.book.data.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class BookEventSerializer implements DeserializationSchema<BookEvent>, SerializationSchema<BookEvent> {

    private final ObjectMapper objectMapper;

    public BookEventSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public BookEvent deserialize(byte[] bytes) throws IOException {
        if (bytes.length == 0) {
            return new BookEvent();
        }
        return objectMapper.readValue(bytes, BookEvent.class);
    }

    @Override
    public boolean isEndOfStream(BookEvent s) {
        return false;
    }

    @Override
    public byte[] serialize(BookEvent bookEvent) {
        try {
            return objectMapper.writeValueAsBytes(bookEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialise book event=" + bookEvent.toString(), e);
        }
    }

    @Override
    public TypeInformation<BookEvent> getProducedType() {
        return BasicTypeInfo.of(BookEvent.class);
    }

}
