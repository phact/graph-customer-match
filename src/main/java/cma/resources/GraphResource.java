package cma.resources;

import cma.api.SourceCustomer;
import cma.managed.Dse;
import com.codahale.metrics.annotation.Timed;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.*;
import com.datastax.dse.graph.api.DseGraph;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.tinkerpop.gremlin.process.traversal.P.eq;

/**
 * Created by sebastianestevez on 11/22/16.
 */

 /*

 ###Schema definition:

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


     ###Logic:

    1) Create the source vertex with the custom ID (For free: if there are new or updated fields those will update the existing vertex)
    2) If there's no global match, create a global vertex with the new info and create a type A edge. On success, drop any B's
    3) If there's a match create the edge with the match confidence depending on the type of match

    The DataStewardship UI will provide manual intervention for matches of type B
    1) Pull properties for Global and Source for every type B
    2) User will select if a match is valid and be able to correct missing / incorrect inputs
    3) User's final record will be inserted thorugh standard workflow above and be upgraded to type A

    http://54.183.185.50:9091/

      */

@Path("/api/v0/cma")
public class GraphResource {

    private Dse dse;
    private DseSession session;
    private GraphTraversalSource g;


    public GraphResource(Dse dse) {
        this.dse=dse;
        this.session = dse.getSession();
        this.g = DseGraph.traversal();
    }

    //sync approach is easier to troubleshoot and will generate fewer race conditions at the cost of higher throughput
    @PUT
    @Timed
    @Path("/addCustomer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SourceCustomer addCustomer(final SourceCustomer sourceCustomer) {

        //Upsert
        GraphStatement graphStatement = DseGraph.statementFromTraversal(g.addV(T.label,"source_customer_record", "source_id", sourceCustomer.getSourceid(), "dob", sourceCustomer.getDob(), "system_name", sourceCustomer.getSystem_name(), "firstname",sourceCustomer.getFirstname(),"lastname",sourceCustomer.getLastname(),"ssn",sourceCustomer.getSsn(),"phone", sourceCustomer.getPhone(),"gender", sourceCustomer.getGender(),"address",sourceCustomer.getAddress()));
        Vertex sourceCustomerVertex = session.executeGraph(graphStatement).one().asVertex();

        //Try to match and create edge
        graphStatement = DseGraph.statementFromTraversal(g.V().hasLabel("global_customer_record")
                .has("firstname",eq(sourceCustomerVertex.getProperty("firstname").getValue().asString())).
                        has("lastname",eq(sourceCustomerVertex.getProperty("lastname").getValue().asString())).
                        has("dob",eq(sourceCustomerVertex.getProperty("dob").getValue().asInt())).
                        has("address",eq(sourceCustomerVertex.getProperty("address").getValue().asString())).
                        has("ssn",eq(sourceCustomerVertex.getProperty("ssn").getValue().asString()))
                            .as("to")
                .V("id",sourceCustomerVertex.getId().toString())
                            .as("from")
                .addE("is")
                .from("from")
                .to("to")
                .property("confidence","A"));

        try {
            GraphResultSet rs = session.executeGraph(graphStatement);
            //if there was no match, create new Global and edge
            if (rs.one() == null) {

                graphStatement = DseGraph.statementFromTraversal(g.addV("global_customer_record").
                        property("dob", sourceCustomerVertex.getProperty("dob").getValue().asInt()).
                        property("system_name", sourceCustomerVertex.getProperty("system_name").getValue().asString()).
                        property("firstname", sourceCustomerVertex.getProperty("firstname").getValue().asString()).
                        property("lastname", sourceCustomerVertex.getProperty("lastname").getValue().asString()).
                        property("ssn", sourceCustomerVertex.getProperty("ssn").getValue().asString()).
                        property("phone", sourceCustomerVertex.getProperty("phone").getValue().asString()).
                        property("address", sourceCustomerVertex.getProperty("address").getValue().asString()).
                        property("gender", sourceCustomerVertex.getProperty("gender").getValue().asString()).as("to").
                        V(sourceCustomerVertex.getId()).
                        addE("is").to("to").
                        property("confidence", "A")
                );

                session.executeGraph(graphStatement);
            }

            return sourceCustomer;
        }catch(InvalidQueryException e){
            System.out.println(e.getMessage());
            return sourceCustomer;
        }

    }

    //async is singificantly more performant though it may produce more global duplicates due to race conditions.
    //good option if there is a cleanup job in place and if global vertices are mostly populated
    @PUT
    @Timed
    @Path("/addCustomerAsync")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SourceCustomer addCustomerAsync(final SourceCustomer sourceCustomer) {

        //Step 1: first create a source customer regardless. Repeated entries will be upserts since sourceid is the key.
        //SourceCustomer sourceCustomer = new SourceCustomer(sourceid, dob, system_name, firstname, lastname, ssn, phone, gender, address);

        final AtomicReference<Object> cache = new AtomicReference<Object>();

        upsertSource(sourceCustomer, cache);

        return sourceCustomer;
    }

    private void upsertSource(SourceCustomer sourceCustomer, final AtomicReference<Object> cache){
        GraphStatement graphStatement = DseGraph.statementFromTraversal(g.addV(T.label,"source_customer_record", "source_id", sourceCustomer.getSourceid(), "dob", sourceCustomer.getDob(), "system_name", sourceCustomer.getSystem_name(), "firstname",sourceCustomer.getFirstname(),"lastname",sourceCustomer.getLastname(),"ssn",sourceCustomer.getSsn(),"phone", sourceCustomer.getPhone(),"gender", sourceCustomer.getGender(),"address",sourceCustomer.getAddress()));

        ListenableFuture<GraphResultSet> future = session.executeGraphAsync(graphStatement);

        Futures.addCallback(future,
                new FutureCallback<GraphResultSet>() {
                    public void onSuccess(GraphResultSet result) {
                        Vertex sourceCustomerVertex = (Vertex) result.one().asVertex();


                        System.out.println("Succeeded in upserting source vetex " + sourceCustomerVertex.toString());
                        System.out.println("ID : " + sourceCustomerVertex.getId());
                        System.out.println("Properties : " + sourceCustomerVertex.getProperties().toString());

                        matchCustomerAndCreateEdge(sourceCustomerVertex, cache);
                    }

                    public void onFailure(Throwable t) {
                        System.out.println("Error writing vertex: " + t.getMessage());
                    }
                },
                MoreExecutors.sameThreadExecutor()
        );

    }

    private void matchCustomerAndCreateEdge(Vertex sourceCustomerVertex, final AtomicReference<Object> cache){

        cache.set(sourceCustomerVertex);
        //sourceCustomerVertex[0] = (Vertex) result;

        System.out.println("Let's check the properties");
        System.out.println("firstname: "+ sourceCustomerVertex.getProperty("firstname").getValue().asString());
        System.out.println("id: "+ sourceCustomerVertex.getId().get("source_id").toString());
        //Step 2: Try the match, if there is a match, create the A rated edge
        //I'm using the match step to be able to get at my sourceCustomerVertex
        GraphStatement graphStatement = DseGraph.statementFromTraversal(g.V().hasLabel("global_customer_record")
                .has("firstname",eq(sourceCustomerVertex.getProperty("firstname").getValue().asString())).
                        has("lastname",eq(sourceCustomerVertex.getProperty("lastname").getValue().asString())).
                        has("dob",eq(sourceCustomerVertex.getProperty("dob").getValue().asInt())).
                        has("address",eq(sourceCustomerVertex.getProperty("address").getValue().asString())).
                        has("ssn",eq(sourceCustomerVertex.getProperty("ssn").getValue().asString())).as("to")
                .V("id",sourceCustomerVertex.getId().toString()).as("from")
                .addE("is")
                .from("from")
                .to("to")
                .property("confidence","A"));

        System.out.println("Query: " + graphStatement.toString());

        ListenableFuture<GraphResultSet> future = session.executeGraphAsync(graphStatement);

        Futures.addCallback(future,
                new FutureCallback<GraphResultSet>() {
                    public void onSuccess(GraphResultSet result) {
                        System.out.println("Match / add edge query returned.");
                        boolean isempty = true;
                        for (GraphNode graphNode : result) {
                            isempty = false;
                            System.out.println("Matched and inserted edge.");
                            Edge myEdge = (Edge) graphNode.asEdge();
                        }
                        if (isempty){
                            //Note: there is a race condition where we will generate duplicate globals here if the global customer
                            // is in the indexing queue.
                            //Real-time indexing will help here but a periodic job is needed to clean these up.
                            System.out.println("Match / add query returned with no error but empty");
                            createNewGlobalVertexAndEdge(cache);
                        }else{
                            System.out.println("Found a match and created an edge liking me to that match. Great job we'e done.");
                        }
                    }

                    public void onFailure(Throwable t) {
                        //There was no match or
                        //maybe this edge already exists
                        System.out.println("No Match or edge already exists: " + t.getMessage());

                    }
                },
                MoreExecutors.sameThreadExecutor()
        );
    }

    private void createNewGlobalVertexAndEdge(AtomicReference<Object> cache){
        Vertex sourceCustomerVertex = (Vertex) cache.get();
        //Do we try match types B and C at this point?
        //Step 3: If there is no match, create a new global vertex with a type A edge.
        System.out.println(sourceCustomerVertex.getProperty("lastname").getValue().asString());

        GraphStatement graphStatement = DseGraph.statementFromTraversal(g.addV("global_customer_record").
                property("dob", sourceCustomerVertex.getProperty("dob").getValue().asInt()).
                property("system_name", sourceCustomerVertex.getProperty("system_name").getValue().asString()).
                property("firstname",sourceCustomerVertex.getProperty("firstname").getValue().asString()).
                property("lastname",sourceCustomerVertex.getProperty("lastname").getValue().asString()).
                property("ssn", sourceCustomerVertex.getProperty("ssn").getValue().asString()).
                property("phone", sourceCustomerVertex.getProperty("phone").getValue().asString()).
                property("address", sourceCustomerVertex.getProperty("address").getValue().asString()).
                property("gender", sourceCustomerVertex.getProperty("gender").getValue().asString()).as("to").
                V(sourceCustomerVertex.getId()).
                addE("is").to("to").
                property("confidence", "A")
        );

        ListenableFuture<GraphResultSet> future = session.executeGraphAsync(graphStatement);

        Futures.addCallback(future,
                new FutureCallback<GraphResultSet>() {
                    public void onSuccess(GraphResultSet result) {
                        System.out.println("Created a new Global Customer with an edge.");
                        System.out.println("Summary: Didn't find a match so created a new Global Customer with an edge linking to it. Great job we're done.");
                    }

                    public void onFailure(Throwable t) {
                        System.out.println("Error creating global and edge:" + t.getMessage());
                        t.printStackTrace();
                    }
                },
                MoreExecutors.sameThreadExecutor()
        );
    }


    //get scored matches, this passes a groovy string to be executed in the graph engine
    //this allows for server side score computation (for performance) and simplifies the application code.
    @PUT
    @Timed
    @Path("/getScoredMatches")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String getScoredMatches(final SourceCustomer sourceCustomer) {

            String statementString = "def scores = [:]\n" +
                "    def test;\n" +
                "    g.V().hasLabel(\"global_customer_record\").has(\"firstname\", \"" + sourceCustomer.getFirstname() + "\").forEachRemaining{\n" +
                "        matchCandidate ->\n" +
                "                test = matchCandidate\n" +
                "        if (scores[matchCandidate] == null){\n" +
                "            scores[matchCandidate] = 0 ;\n" +
                "        }\n" +
                "        if (matchCandidate.value(\"lastname\") == \"" + sourceCustomer.getLastname() + "\"){\n" +
                "            scores[matchCandidate] = scores[matchCandidate] + 1;\n" +
                "        }\n" +
                "        if (matchCandidate.value(\"firstname\") == \""+ sourceCustomer.getFirstname() +"\"){\n" +
                "            scores[matchCandidate] = scores[matchCandidate] + 2;\n" +
                "        }\n" +
                "        if (matchCandidate.value(\"address\") == \""+ sourceCustomer.getAddress() +"\"){\n" +
                "            scores[matchCandidate] = scores[matchCandidate] + 3;\n" +
                "        }\n" +
                "        if (matchCandidate.value(\"ssn\") == \""+ sourceCustomer.getSsn() +"\"){\n" +
                "            scores[matchCandidate] = scores[matchCandidate] + 4;\n" +
                "        }else{\n" +
                "            scores[matchCandidate] = scores[matchCandidate] + 0;\n" +
                "        }\n" +
                "    }\n" +
                "    scores;";

        GraphStatement graphStatement = new SimpleGraphStatement(statementString);

        GraphResultSet rs = session.executeGraph(graphStatement);

        String result  = rs.one().toString();

        if (result==null){
            return null;
        }
        return "no matches";

    }

    @PUT
    @Timed
    @Path("/test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String test(final SourceCustomer sourceCustomer) {

        String statementString = "def scores = ['1':'2']\n" +
                "    scores;";

        GraphStatement graphStatement = new SimpleGraphStatement(statementString);

        GraphResultSet rs = session.executeGraph(graphStatement);

        return rs.one().toString();

    }

}