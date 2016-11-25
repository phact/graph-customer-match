package cma.managed;

import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphOptions;
import io.dropwizard.lifecycle.Managed;
import cma.CMAConfiguration;

/**
 * Created by sebastianestevez on 11/22/16.
 */
public class Dse implements Managed {
    private DseSession session;

    public DseCluster getCluster() {
        return cluster;
    }

    public DseSession getSession() {
        return session;
    }

    private DseCluster cluster;
    final private String host;
    final private int port;
    final private String graphName;

    public Dse(CMAConfiguration conf){
        this.port = conf.getPort();
        this.graphName = conf.getGraphName();
        this.host = conf.getHost();

    }
    public void start() throws Exception {
        cluster = DseCluster.builder().
                addContactPoints(host).
                withPort(port).
                withCredentials("cassandra", "cassandra").
                withGraphOptions(new GraphOptions().
                        setReadTimeoutMillis(15000).
                        setGraphName(graphName)).
                build();
        session = cluster.connect();
    }

    public void stop() throws Exception {
        session.close();
        cluster.close();
    }
}
