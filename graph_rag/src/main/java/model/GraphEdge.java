package model;

import java.util.LinkedHashMap;
import java.util.Map;

public class GraphEdge {

	 	private String id;
	    private String type;   
	    private String source;
	    private String target;
	    
	    private Map<String, Object> properties = new LinkedHashMap<>();

	    public GraphEdge() {}

	    public GraphEdge(String id, String type, String source, String target) {
	        this.id = id;
	        this.type = type;
	        this.source = source;
	        this.target = target;
	    }

	    public String getId() {
	        return id;
	    }

	    public void setId(String id) {
	        this.id = id;
	    }

	    public String getType() {
	        return type;
	    }

	    public void setType(String type) {
	        this.type = type;
	    }

	    public String getSource() {
	        return source;
	    }

	    public void setSource(String source) {
	        this.source = source;
	    }

	    public String getTarget() {
	        return target;
	    }

	    public void setTarget(String target) {
	        this.target = target;
	    }

	    public Map<String, Object> getProperties() {
	        return properties;
	    }

	    public void putProperty(String key, Object value) {
	        if (value != null) {
	            this.properties.put(key, value);
	        }
	    }

	    @Override
	    public String toString() {
	        return "GraphEdge{id=" + id + ", type=" + type + ", " + source + "->" + target
	                + ", properties=" + properties + "}";
	    }
	    
}
