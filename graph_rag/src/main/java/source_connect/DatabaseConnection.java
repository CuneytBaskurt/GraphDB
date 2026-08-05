package source_connect;

import java.util.Map;

public interface DatabaseConnection {
	
	void connect(Map<String, String> config);

    void disconnect();

    boolean isConnected();

}
