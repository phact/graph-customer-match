+++
date = "2017-04-10T14:21:00-05:00"
title = "Architecture"
weight = 3
+++

This section details the architecture demonstrated in this reference field asset.

### Architecture Diagram

<div title="rendered dynamically" align="middle">
{{< mermaid >}}
graph LR
A["Web Browser / Mobile Client"]
A["Java Application - DataStax Driver"]--"Enriched Data (gremlin)"-->B["DSE Graph"]
C--"Restful requests"-->A
{{< /mermaid >}}
</div>

### Architeceture and design

This application is built using [dropwizard](http://www.dropwizard.io/) a framework for developing production ready Java applications.

The [java driver](https://github.com/datastax/java-dse-driver) allows us to make sync, async, and groovystring based requests against DSE Graph.
