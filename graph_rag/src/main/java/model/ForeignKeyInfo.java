package model;

import java.util.List;

public class ForeignKeyInfo {
	
	private final String targetTable;
	private final List<String> sourceColumns;
	private final List<String> targetColumns;
	
	 public ForeignKeyInfo(String targetTable, List<String> sourceColumns, List<String> targetColumns) {
	        this.targetTable = targetTable;
	        this.sourceColumns = sourceColumns;
	        this.targetColumns = targetColumns;
	    }

	    public String getTargetTable() {
	        return targetTable;
	    }

	    public List<String> getSourceColumns() {
	        return sourceColumns;
	    }

	    public List<String> getTargetColumns() {
	        return targetColumns;
	    }

	    @Override
	    public String toString() {
	        return "FK(" + sourceColumns + " -> " + targetTable + "." + targetColumns + ")";
	    }

}
