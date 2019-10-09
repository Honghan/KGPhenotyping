package ac.uk.hdruk.graph;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
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

/**
 * Command line interface for creating/updating studies and running queries.
 *
 *  Usage:
 *    Create/update study rules:
 *      CLIQuery <study name> -r <rules file>
 *    Run query:
 *      CLIQuery <study name> -q <query file>
 */
public class CLIQuery {
	
	static String configFile = "src/main/resources/lab/onto-phenotyping.properties";

        /**
	 * Initialise a study with its own rules from a file (rules can be null or empty)
	 */
	public static void updateStudy(String studyName, String ruleFile) {
		try {
			OntologyPhenotyping op = OntologyPhenotyping.getInstance(configFile);
			String rules = FileUtils.readFileToString(new File(ruleFile));
			op.createUpdateStudy(studyName, rules);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Run a study specific query
	 */
	public static void runQuery(String studyName, String queryFile) {
		try {
			OntologyPhenotyping op = OntologyPhenotyping.getInstance(configFile);
                        String sparqlQuery = FileUtils.readFileToString(new File(queryFile));
			System.out.println(op.doStudyQuery(sparqlQuery, studyName));
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public static void main(String[] args) {;
		if (args[1].equals("-r")) {
		    updateStudy(args[0], args[2]);
		} else if (args[1].equals("-q")) {
		    runQuery(args[0], args[2]);
		} else {
	            // no action given
		}
	}
}
