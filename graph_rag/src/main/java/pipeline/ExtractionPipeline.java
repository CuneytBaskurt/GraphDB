package pipeline;

import model.*;
import mapper.RowToEdgeMapper;
import mapper.RowToNodeMapper;
import util.DatabaseChunkedReader;
import util.TableChunk;
import writer.NdjsonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ExtractionPipeline {
    private static final Logger logger = LoggerFactory.getLogger(ExtractionPipeline.class);

    private final DatabaseChunkedReader reader;
    private final Map<String, TableSchema> schemas;
    private final String tenantId;
    private final String dbType;
    private final NdjsonWriter writer;

    public ExtractionPipeline(DatabaseChunkedReader reader,
                              Map<String, TableSchema> schemas,
                              String tenantId,
                              String dbType,
                              NdjsonWriter writer) {
        this.reader = reader;
        this.schemas = schemas;
        this.tenantId = tenantId;
        this.dbType = dbType;
        this.writer = writer;
    }

    public void execute() {
        logger.info("Çıkarma hattı (Extraction Pipeline) çalıştırılıyor...");
        reader.open();
        long nodeCount = 0;
        long edgeCount = 0;

        try {
            TableChunk chunk;
            while ((chunk = reader.nextChunk()) != null) {
                String tableName = chunk.getTableName();
                TableSchema schema = schemas.get(tableName);
                if (schema == null) {
                    logger.warn("Tablo şeması bulunamadı, atlanıyor: {}", tableName);
                    continue;
                }

                for (Map<String, Object> row : chunk.getRows()) {
                    String originalPk = RowToNodeMapper.extractPkValue(row, schema.getPrimaryKeyColumns());
                    SourceMeta meta = new SourceMeta(dbType, tableName, originalPk);

                    // 1. Düğüm Oluşturma ve Yazma
                    GraphNode node = RowToNodeMapper.map(row, schema, tenantId);
                    if (node != null) {
                        GraphEvent nodeEvent = GraphEvent.forNode(tenantId, Operation.INSERT, node, meta);
                        writer.writeEvent(nodeEvent);
                        nodeCount++;
                    }

                    // 2. Kenar Oluşturma ve Yazma
                    List<GraphEdge> edges = RowToEdgeMapper.map(row, schema, tenantId);
                    for (GraphEdge edge : edges) {
                        GraphEvent edgeEvent = GraphEvent.forEdge(tenantId, Operation.INSERT, edge, meta);
                        writer.writeEvent(edgeEvent);
                        edgeCount++;
                    }
                }
            }
            writer.flush();
            logger.info("İşlem tamamlandı. Toplam Üretilen -> Node: {}, Edge: {}", nodeCount, edgeCount);
        } catch (Exception e) {
            logger.error("Pipeline çalıştırma hatası!", e);
            throw new RuntimeException("Pipeline yürütülemedi", e);
        } finally {
            reader.close();
        }
    }
}