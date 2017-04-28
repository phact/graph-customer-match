### Agenda:

1) DataStax intro - <5 minutes
2) 360 VIP example - <5 minutes
3) Brief graph intro (scalable operational graph database for large - real time data problems) vs. data lake with solr indexes < 10 minutes
4) 360 VIP DataModel - < 5 minutes
5) Proposed DataModel < 10 minutes
6) Notebook, how would we build and query this graph? - 30 minutes
7) Business logic
8) Business / application logic and architecture - 30 minutes
9) Next steps / action items


### Schema:

vertex label Global customer
  first name
  last name
  ssn
  phone
  gender
  address
  DOB

If necessary (if this is a one to many) pull out the address:
vertex label address
  type
  address
  zip
  city
  country

edge from global customer to address

vertex label source customer ID
  custom id (for uniqueness constraint) - source customer ID
  system name
  first name
  last name
  ssn
  phone
  address
  gender
  DOB

(Could also do a source vertex label per system)

edge label IS from global customer to customer (Single Cardinality Edge - in case there's an update from B to A)
  confidence A, B, or C
     secondary index


### Logic:

1) Create the source vertex with the custom ID (For free: if there are new or updated fields those will update the existing vertex)
2) If there's no global match, create a global vertex with the new info and create a type A edge. On success, drop any B's
3) If there's a match create the edge with the match confidence depending on the type of match
