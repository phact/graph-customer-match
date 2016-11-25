package cma;

import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.datastax.driver.dse.graph.GraphStatement;
import com.datastax.dse.graph.api.DseGraph;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Created by sebastianestevez on 10/27/16.
 *
 *Schema definition:

 schema.clear()

 schema.propertyKey("firstname").Text().single().create()
 schema.propertyKey("address").Text().single().create()
 schema.propertyKey("CODS").Text().single().create()
 schema.propertyKey("gender").Text().single().create()
 schema.propertyKey("phone").Text().single().create()
 schema.propertyKey("confidence").Text().single().create()
 schema.propertyKey("system_name").Text().single().create()
 schema.propertyKey("source_id").Text().single().create()
 schema.propertyKey("lastname").Text().single().create()
 schema.propertyKey("ssn").Text().single().create()
 schema.propertyKey("dob").Int().single().create()
 schema.edgeLabel("is").single().properties("confidence").create()
 schema.vertexLabel("source_customer_record").partitionKey("source_id").properties("system_name", "firstname", "address", "gender", "phone", "lastname", "ssn","dob").create()
 schema.vertexLabel("global_customer_record").properties("firstname", "address", "gender", "phone", "lastname", "ssn","dob").create()
 schema.edgeLabel("is").connection("source_customer_record", "global_customer_record").add()

 //add some indexes, search for high cardinality and partial match
 schema.vertexLabel("source_customer_record").index("search").search().by("firstname").asString().by("lastname").asString().by("address").asString().by("ssn").asString().by("dob").add()
 schema.vertexLabel("global_customer_record").index("search").search().by("firstname").asString().by("lastname").asString().by("address").asString().by("ssn").asString().by("dob").add()
 //secondary index for low cardinality
 schema.vertexLabel("source_customer_record").index("by_gender").secondary().by("gender").add()
 schema.vertexLabel("global_customer_record").index("by_gender").secondary().by("gender").add()

 */

public class CMAPoc {
    private GraphTraversalSource g;
    private DseSession session;
    public void cmaPoc(){

        session = DseCluster.builder().
                addContactPoints("localhost").
                withGraphOptions(new GraphOptions().
                        setReadTimeoutMillis(15000).
                        setGraphName("cma_graph")).
                build().
                connect();

        processFile();

    }

    public void processFile(){

        GraphTraversalSource g = DseGraph.traversal();
        GraphStatement graphStatement = DseGraph.statementFromTraversal(createSource("8395738271", 19870725, "CODS","TAYLA","MORGAN","581618206","8645397984","F","7601 UMBER PANDA POINT"));
        //GraphStatement graphStatement = DseGraph.statementFromTraversal(g.V(T.label,"source_customer_record", "source_id", "8395738271", "dob", 19870725, "system_name", "CODS", "firstname","TAYLA","lastname","MORGAN","ssn","581618206","phone", "8645397984","gender", "F","address","7601 UMBER PANDA POINT"));

        ListenableFuture<GraphResultSet> future = session.executeGraphAsync(graphStatement);

        Futures.addCallback(future,
                new FutureCallback<GraphResultSet>() {
                    public void onSuccess(GraphResultSet result) {
                        System.out.println("Cassandra version is " + result.toString());
                    }

                    public void onFailure(Throwable t) {
                        System.out.println("Error while reading Cassandra version: " + t.getMessage());
                    }
                },
                MoreExecutors.sameThreadExecutor()
        );

    }

    GraphTraversal<Vertex, Vertex> createSource(String sourceid, int dob, String system_name, String firstname, String lastname, String ssn, String phone, String gender, String address){
        return g.V(T.label,"source_customer_record", "source_id", sourceid, "dob", dob, "system_name", system_name, "firstname",firstname,"lastname",lastname,"ssn",ssn,"phone", phone,"gender", gender,"address", address);
    }

    GraphTraversal<Vertex, Vertex> createGlobal(String sourceid, int dob, String system_name, String firstname, String lastname, String ssn, String phone, String gender, String address){
        return g.V(T.label,"global_customer_record", "dob", 19870725, "firstname","TAYLA","lastname","MORGAN","ssn","581618206","phone", "8645397984","gender", "F","address","7601 UMBER PANDA POINT");
    }

}