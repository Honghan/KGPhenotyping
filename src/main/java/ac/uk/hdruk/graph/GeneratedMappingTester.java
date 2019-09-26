package ac.uk.hdruk.graph;

import java.io.IOException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.parser.SWRLParseException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;

public class GeneratedMappingTester {
	
	public static void testRuleTranslation() {
		final String miniOWLFile = "src/main/resources/lab/rule-mappings/mini-biochem-ontology.ttl";
		final String generatedOBDAMapping = "src/main/resources/lab/generated_mappings/generated-mapping.obda";
	    final String connPropertyFile = "src/main/resources/lab/BiochemistryOntologyNew.properties";
	    
	    final String[] sparqls = ScotLabExemplar.readSPARQLQueries("src/main/resources/lab/queries/testQueries.txt");
	    
	    OntopSQLOWLAPIConfiguration config = (OntopSQLOWLAPIConfiguration) OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(generatedOBDAMapping)
                .ontologyFile(miniOWLFile)
                .propertyFile(connPropertyFile)
                .enableTestMode()
                .build();
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		OntopOWLReasoner reasoner = null;
		OntopOWLConnection conn = null;
		OntopOWLStatement st = null;
		try{
			reasoner = factory.createReasoner(config);
			conn = reasoner.getConnection();
			st = conn.createStatement();
			
			for(String q : sparqls) {
				String sqlQuery = st.getExecutableQuery(q).toString();
				
				System.out.println(sqlQuery);
				
				TupleOWLResultSet rs = st.executeSelectQuery(q);
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
			}
        } catch(Exception e){
        	e.printStackTrace();
        }finally {
        	if (conn != null) {
        		try {
        			st.close();
					conn.close();
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
	}
	
	public static void testOntoPhenotyping() {
		String configFile = "src/main/resources/lab/onto-phenotyping.properties";
		try {
			OntologyPhenotyping op = OntologyPhenotyping.getInstance(configFile);
			String studyName = "t2dm";
			String ruleJSON = "{\r\n" + 
					"	\"t1-t2-exclusive\": \"E11(?x) ^ E10(?x) -> DiabetesConflictPatient(?x)\"}";
						
			// 1. initialise a study with its own rules (rules can be null or empty)
			// the following create/update function only needs to run once
			try {
//				op.createUpdateStudy(studyName, null);
				op.createUpdateStudy(studyName, ruleJSON);
			} catch (SWRLParseException | SWRLBuiltInException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// 2. run study specific queries
			String[] queries = ScotLabExemplar.readSPARQLQueries("src/main/resources/lab/sample_queries/diabetes-queries.txt");
			for (String sparqlQuery : queries) {
				System.out.println(sparqlQuery);
				System.out.println(op.doStudyQuery(sparqlQuery, studyName));
			}
		} catch (IllegalConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static void main(String[] args) {;
		// test rule translation from SWRL to OBDA
//		testRuleTranslation();
		
		// ontology phenotyping test
		testOntoPhenotyping();
	}
}
