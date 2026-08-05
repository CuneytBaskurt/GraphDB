package model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GraphNode {
	
	  private String id;
	  private List<String> labels = new ArrayList<>();
	  private Map<String, Object> properties = new LinkedHashMap<>();
	  
	  public GraphNode() {}
	  
	  public GraphNode(String id) {
	        this.id = id;
	    }

	    public String getId() {
	        return id;
	    }

	    public void setId(String id) {
	        this.id = id;
	    }

	    public List<String> getLabels() {
	        return labels;
	    }

	    public void addLabel(String label) {
	        this.labels.add(label);
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
	        return "GraphNode{id=" + id + ", labels=" + labels + ", properties=" + properties + "}";
	    }

}
