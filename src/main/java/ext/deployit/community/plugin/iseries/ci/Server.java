package ext.deployit.community.plugin.iseries.ci;

import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;
import com.xebialabs.deployit.plugin.api.udm.base.BaseContainer;

@Metadata(root = Metadata.ConfigurationItemRoot.INFRASTRUCTURE, description = "An iSeries server.")
public class Server extends BaseContainer {

    @Property
    private String address;

    @Property
    private String username;

    @Property(password = true)
    private String password;

    public String getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
