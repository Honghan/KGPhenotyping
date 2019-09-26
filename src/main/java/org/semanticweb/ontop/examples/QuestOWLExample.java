package org.semanticweb.ontop.examples;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;
public class QuestOWLExample {

    private final String owlfile = "src/main/resources/example/exampleBooks.owl";
    private final String obdafile = "src/main/resources/example/exampleBooks.obda";
    private final String propertiesfile = "src/main/resources/example/exampleBooks.properties";
    public void runQuery() throws Exception {

        /*
         * Create the instance of Quest OWL reasoner.
         */
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = (OntopSQLOWLAPIConfiguration) OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .propertyFile(propertiesfile)
                .enableTestMode()
                .build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);

        /*
         * Get the book information that is stored in the database
         */
        String sparqlQuery = "PREFIX : <http://meraka/moss/exampleBooks.owl#> \n" +
                " SELECT DISTINCT ?x ?title ?author ?genre ?edition \n" +
                " WHERE { ?x a :Book; :title ?title; :genre ?genre; :writtenBy ?y; :hasEdition ?z. \n" +
                "         ?y a :Author; :name ?author. \n" +
                "         ?z a :Edition; :editionNumber ?edition }";

        try (/*
         * Prepare the data connection for querying.
         */
                OntopOWLConnection conn = reasoner.getConnection();
                OntopOWLStatement st = conn.createStatement()) {

            long t1 = System.currentTimeMillis();
            TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
            int columnSize = rs.getColumnCount();
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                for (int idx = 1; idx <= columnSize; idx++) {
                    OWLObject binding = bindingSet.getOWLObject(idx);
                    System.out.print(ToStringRenderer.getInstance().getRendering(binding) + ", ");
                }
                System.out.print("\n");
            }
            rs.close();
            long t2 = System.currentTimeMillis();

            /*
             * Print the query summary
             */
            String sqlQuery = st.getExecutableQuery(sparqlQuery).toString();

            System.out.println();
            System.out.println("The input SPARQL query:");
            System.out.println("=======================");
            System.out.println(sparqlQuery);
            System.out.println();

            System.out.println("The output SQL query:");
            System.out.println("=====================");
            System.out.println(sqlQuery);

            System.out.println("Query Execution Time:");
            System.out.println("=====================");
            System.out.println((t2 - t1) + "ms");

        } finally {
            reasoner.dispose();
        }
    }


    /**
     * Main client program
     */
    public static void main(String[] args) {
        try {
            QuestOWLExample example = new QuestOWLExample();
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
