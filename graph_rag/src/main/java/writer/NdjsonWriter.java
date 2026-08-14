package writer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.io.Closeable;

public class NdjsonWriter implements Closeable {
    private final BufferedWriter writer;
    private final ObjectMapper objectMapper;

    public NdjsonWriter(String filePath) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(filePath));
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        this.objectMapper.setDateFormat(sdf);
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