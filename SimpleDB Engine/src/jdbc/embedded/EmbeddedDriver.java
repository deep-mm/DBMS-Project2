package src.jdbc.embedded;

import java.util.Properties;
import java.sql.SQLException;
import src.server.SimpleDB;
import src.jdbc.DriverAdapter;

/**
 * The RMI server-side implementation of RemoteDriver.
 * @author Edward Sciore
 */

public class EmbeddedDriver extends DriverAdapter {   
   /**
    * Creates a new RemoteConnectionImpl object and 
    * returns it.
    * @see src.jdbc.network.RemoteDriver#connect()
    */
   public EmbeddedConnection connect(String url, Properties p) throws SQLException {
      String dbname = url.replace("jdbc:src:", "");
      SimpleDB db = new SimpleDB(dbname);
      return new EmbeddedConnection(db);
   }
}

