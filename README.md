###Download and Run:

    wget https://github.com/phact/cma-poc/releases/download/v0.01/cma-1.0-SNAPSHOT.jar

    java -jar cma-1.0-SNAPSHOT.jar server conf/cma.yaml

###Run from Source:

    git clone git@github.com:phact/cma-poc.git

    mvn package exec:java "-Dexec.args=server conf/cma.yaml"

###Build and run:

    git clone git@github.com:phact/cma-poc.git

    mvn package

    java -jar cma-1.0-SNAPSHOT.jar server conf/cma.yaml
