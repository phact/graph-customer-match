package graphcustomermatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.hibernate.validator.constraints.NotEmpty;


/**
 * Created by sebastianestevez on 11/22/16.
 */
public class CMAConfiguration extends Configuration{

    @NotEmpty
    @JsonProperty
    private String host = "localhost";

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    @Min(1)
    @Max(65535)
    @JsonProperty
    private Integer port = 9042;

    @NotEmpty
    @JsonProperty
    private String graphName = "customer_match";

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getGraphName() {
        return graphName;
    }


}
