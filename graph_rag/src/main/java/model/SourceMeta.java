package model;

public class SourceMeta {
	
	 	private String dbType;         
	    private String schemaOrTable;  
	    private String originalPk;
	    
	    public SourceMeta() {}

	    public SourceMeta(String dbType, String schemaOrTable, String originalPk) {
	        this.dbType = dbType;
	        this.schemaOrTable = schemaOrTable;
	        this.originalPk = originalPk;
	    }

	    public String getDbType() {
	        return dbType;
	    }

	    public void setDbType(String dbType) {
	        this.dbType = dbType;
	    }

	    public String getSchemaOrTable() {
	        return schemaOrTable;
	    }

	    public void setSchemaOrTable(String schemaOrTable) {
	        this.schemaOrTable = schemaOrTable;
	    }

	    public String getOriginalPk() {
	        return originalPk;
	    }

	    public void setOriginalPk(String originalPk) {
	        this.originalPk = originalPk;
	    }

}
