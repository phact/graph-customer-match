+++
date = "2017-04-10T14:21:00-05:00"
title = "Introduction"
weight = 1
+++

This is a guide for how to use the customer matching graph entity extraction / enrichment asset brought to you by the Vanguard team.

### Motivation

A comon graph use case is enrichment of data as it flows in. This asset shows a way of creating vertices and edges using the dse graph java driver in a restful application. The application shows sync / async / and groovystring methods for loading the data and performing the enrichment.

### What is included?

This field asset includes a working application for customer matching leveraging the following DSE functionality:

* DSE Graph
* DSE Graph Search indexes
* Graph driver async api
* Graph driver syncronous api
* Graph driver groovystring api

### Business Take Aways

Entity resolution and enrichment can be done many different ways. For mission critical customer facing applications being able to populate enriched graph data closer to realtime can enable high value use cases and improve customer experience.

### Technical Take Aways

Leverage this asset when you need a:

1) reference implementation for graph based resful API’s
2) examples of usage for the graph java driver (sync, async, groovystring)
3) example of performing entity resolution on ingest
