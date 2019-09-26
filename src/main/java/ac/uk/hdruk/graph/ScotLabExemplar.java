package ac.uk.hdruk.graph;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.semanticweb.ontop.examples.QuestOWLExample;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;

public class ScotLabExemplar {
	 private final String owlfile = "src/main/resources/lab/BiochemistryOntologyNew.ttl";
	 private final String noKnowledge = "src/main/resources/lab/BiochemistryOntology.ttl";
	    private final String labODBAfile = "src/main/resources/lab/BiochemistryOntology.obda";
	    private final String propertiesfile = "src/main/resources/lab/BiochemistryOntologyNew.properties";
	    private final String labQueryFile = "src/main/resources/lab/testLabQueries.txt";
	    
	    private final String smr01ODBAfile = "src/main/resources/lab/condition.obda";
	    private final String conditionQueryFile = "src/main/resources/lab/testConditionQueries.txt";
	    
	    public static final String querySplitter = "============";
	    
	    public static String[] readSPARQLQueries(String filePath) {
	    	try {
				String s = FileUtils.readFileToString(new File(filePath));
				return s.split(querySplitter);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return null;
	    }
	    
	    public void runConditionbQuery(boolean useKnowledge, String queryFileName) throws Exception {
	    	String ontoFile = owlfile;
	    	if (!useKnowledge) ontoFile = noKnowledge;
	    	runQuery(smr01ODBAfile, ontoFile, propertiesfile, 
	    			String.format("src/main/resources/lab/queries/", queryFileName));
	    }
	    
	    public void runLabQuery(boolean useKnowledge, String queryFileName) throws Exception {
	    	String ontoFile = owlfile;
	    	if (!useKnowledge) ontoFile = noKnowledge;
	    	runQuery(labODBAfile, ontoFile, propertiesfile, 
	    			String.format("src/main/resources/lab/queries/", queryFileName));
	    }
	    
	    public void runQuery(String cmd) throws Exception {
	    	String[] arr = cmd.split("-");
	    	String ontoFile = owlfile;
	    	String odbaFile = labODBAfile;
	    	if (arr[0].equals("d")) ontoFile = noKnowledge;
	    	if (arr[1].equals("smr")) odbaFile = smr01ODBAfile;
	    	System.out.println(String.format("working on [%s \n %s \n %s]", odbaFile, ontoFile, arr[2]));
	    	runQuery(odbaFile, ontoFile, propertiesfile, 
	    			String.format("src/main/resources/lab/queries/%s.txt", arr[2]));
	    }
	    
	    public void runQuery(String obdaFile, String owlFile, String propertyFile, String queryFile) throws Exception {

	        /*
	         * Create the instance of Quest OWL reasoner.
	         */
	        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
	        OntopSQLOWLAPIConfiguration config = (OntopSQLOWLAPIConfiguration) OntopSQLOWLAPIConfiguration.defaultBuilder()
	                .nativeOntopMappingFile(obdaFile)
	                .ontologyFile(owlFile)
	                .propertyFile(propertyFile)
	                .enableTestMode()
	                .build();
	        OntopOWLReasoner reasoner = factory.createReasoner(config);
	        String[] queries = readSPARQLQueries(queryFile);
	        

        	for(int i=0;i<queries.length; i++) {
        		String sparqlQuery = queries[i];
        		if(sparqlQuery.trim().length() == 0)
        			continue;
//        		System.out.println(String.format("querying\n:[%s]", sparqlQuery));
        		try (
        		        /*
        		         * Prepare the data connection for querying.
        		         */
        		        	
        	                OntopOWLConnection conn = reasoner.getConnection();
        	                OntopOWLStatement st = conn.createStatement()) {
        					
		        			 /*
				             * Print the query summary
				             */
				            String sqlQuery = st.getExecutableQuery(sparqlQuery).toString();
		
				            System.out.println();
				            System.out.println("The input SPARQL query:");
				            System.out.println("=======================");
				            System.out.println(sparqlQuery);
				            System.out.println();
				            
				            System.out.println("The query result:");
				            System.out.println("=======================");

        		            long t1 = System.currentTimeMillis();
        		            
        		            TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
        		            int columnSize = rs.getColumnCount();
        		            int count = 0;
        		            while (rs.hasNext()) {
        		                final OWLBindingSet bindingSet = rs.next();
        		                for (int idx = 1; idx <= columnSize; idx++) {
        		                    OWLObject binding = bindingSet.getOWLObject(idx);
        		                    System.out.print(ToStringRenderer.getInstance().getRendering(binding) + ", ");
        		                }
        		                System.out.print("\n");
        		                count ++;
        		            }
        		            System.out.println(String.format("total %d results", count));
        		            rs.close();
        		            System.out.println();
        		            
        		            
        		            long t2 = System.currentTimeMillis();
        		           
//        		            System.out.println("The output SQL query:");
//        		            System.out.println("=====================");
//        		            System.out.println(sqlQuery);

        		            System.out.println("Query Execution Time:");
        		            System.out.println("=====================");
        		            System.out.println((t2 - t1) + "ms");

        		        } finally {
        		            reasoner.dispose();
        		        }
        	}
	        
	    }


	    /**
	     * Main client program
	     */
	    public static void main(String[] args) {
	        try {
	        	ScotLabExemplar example = new ScotLabExemplar();
	        	System.out.println("what to do? Command: [(d|k)-(smr|lab)-(query)]");
	        	Scanner cmd = new Scanner(System.in);
	        	while(true) {
	        		String s = cmd.nextLine();
	        		if (s.matches("[^\\-]{1,}\\-[^\\-]{1,}\\-[^\\-]{1,}")) {
	        			try {
							example.runQuery(s);
						} catch (Exception e) {
							// TODO: handle exception
							System.out.println(e.toString());
						}
	        		}else {
	        			System.out.println(String.format("I dont understand [%s]", s));
	        		}
	        		System.out.println("\n^^^^^^^^^^^^^^^^^^^^^^^^\n\n");
	        		System.out.println("what to do? Command: [(d|k)-(smr|lab)-(query)]");
	        	}
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
}
