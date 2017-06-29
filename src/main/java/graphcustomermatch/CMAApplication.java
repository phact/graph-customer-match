package graphcustomermatch;

import graphcustomermatch.managed.Dse;
import graphcustomermatch.resources.GraphResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

/**
 * Created by sebastianestevez on 11/22/16.
 */
public class CMAApplication extends Application<CMAConfiguration> {
    public static void main(String[] args) throws Exception {
        new CMAApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<CMAConfiguration> bootstrap) {

        SwaggerBundle<CMAConfiguration> swaggerBundle;
        swaggerBundle = new SwaggerBundle<CMAConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(CMAConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        };
        bootstrap.addBundle(swaggerBundle);
   }
    @Override
    public void run(CMAConfiguration cmaConfiguration, Environment environment) throws Exception {

        Dse dse = new Dse(cmaConfiguration);
        dse.start();

        GraphResource graphResource = new GraphResource(dse);
        System.out.println("test");
        environment.jersey().register(graphResource);

    }
}