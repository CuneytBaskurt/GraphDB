package model;

import java.time.Instant;

public class GraphEvent {
	
	private String schemaVersion = "1.0";
    private String tenantId;
    private EntityType entityType;
    private Operation operation;
    private String eventTimestamp;  
    private SourceMeta sourceMeta;

    private GraphNode node;

    private GraphEdge edge;

    public GraphEvent() {}

    public static GraphEvent forNode(String tenantId, Operation operation, GraphNode node,
                                      SourceMeta sourceMeta) {
        GraphEvent event = new GraphEvent();
        event.tenantId = tenantId;
        event.entityType = EntityType.NODE;
        event.operation = operation;
        event.eventTimestamp = Instant.now().toString();
        event.sourceMeta = sourceMeta;
        event.node = node;
        return event;
    }

    public static GraphEvent forEdge(String tenantId, Operation operation, GraphEdge edge,
                                      SourceMeta sourceMeta) {
        GraphEvent event = new GraphEvent();
        event.tenantId = tenantId;
        event.entityType = EntityType.EDGE;
        event.operation = operation;
        event.eventTimestamp = Instant.now().toString();
        event.sourceMeta = sourceMeta;
        event.edge = edge;
        return event;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(String eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public SourceMeta getSourceMeta() {
        return sourceMeta;
    }

    public void setSourceMeta(SourceMeta sourceMeta) {
        this.sourceMeta = sourceMeta;
    }

    public GraphNode getNode() {
        return node;
    }

    public void setNode(GraphNode node) {
        this.node = node;
    }

    public GraphEdge getEdge() {
        return edge;
    }

    public void setEdge(GraphEdge edge) {
        this.edge = edge;
    }
    
}
