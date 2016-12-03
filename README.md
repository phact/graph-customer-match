###Create graph schema:

In DSE Studio execute the following schema creation (this will prevent duplicate records in the graph since we use custom ID's for `source_customer_record`s (the default graph is cma_dev):

```
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
```


###Download and Run:

    java -jar cma-1.0-SNAPSHOT.jar server conf/cma.yaml

###Run from Source:

    mvn package exec:java "-Dexec.args=server conf/cma.yaml"

###Build and run:

    mvn package

    java -jar cma-1.0-SNAPSHOT.jar server conf/cma.yaml
