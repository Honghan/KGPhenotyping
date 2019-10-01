# Evaluation
## Benchmark Setup

## Performance Measure
For measurement, the main metric adopted is the result size. To formalise the result size, numbers of results and numbers of inconsistent results will be given for inconsistency handling.
Since there is no certain criteria for correct and complete data, a constant number a will be given as the number of inconsistent results for the Test Query in examination of each requirement. For instance, if there are 5 more inconsistent results for the Plain SQL Query than for the Test Query, the number of inconsistent result for Plain SQL Query is denoted as a+5. Similarly, numbers of results and numbers of inconsistency will be collected for examination of domain knowledge inference. However, there may be requirements cannot be handled with the SQL queries (Complex SQL Queries or Customised Queries). Under such circumstance, only Test Queries and their number of results will be involved in the evaluation.
### Queries for Data-Level Inconsistency handling

> **_Request 1_** The requirement of consistency checking needs our approach to fill absence of data according to its inner constraints. This contributes to a request, that give all patients in given data source, including those who use drugs but not declared to be patients.

The SPARQL query work as Test Query is:
