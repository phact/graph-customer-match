package cma;

import cma.managed.Dse;
import cma.resources.GraphResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Created by sebastianestevez on 11/22/16.
 */
public class CMAApplication extends Application<CMAConfiguration> {
    public static void main(String[] args) throws Exception {
        new CMAApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<CMAConfiguration> bootstrap) {

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