package writer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Closeable;

public class NdjsonWriter implements Closeable {
    private final BufferedWriter writer;
    private final ObjectMapper objectMapper;

    public NdjsonWriter(String filePath) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(filePath));
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public synchronized void writeEvent(Object event) throws IOException {
        if (event == null) return;
        String json = objectMapper.writeValueAsString(event);
        writer.write(json);
        writer.newLine();
    }

    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}