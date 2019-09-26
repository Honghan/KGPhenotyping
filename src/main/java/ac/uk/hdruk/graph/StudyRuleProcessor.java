package ac.uk.hdruk.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDataFactory;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLPredicate;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;

/**
 * process rules defined in a study - convert them into OBDA mappings 
 * so that inferences/inconsistency checking can be done at RDB level
 * @author hwu33
 *
 */
public class StudyRuleProcessor {
	public static Logger _logger = Logger.getLogger(StudyRuleProcessor.class.getName());
	public String queryTemplate;
	public final String mappingTemplate = "mappingId	%s\n" + 
			"target		%s\n" + 
			"source		%s\n" + 
			"";
	private String _owlFile = null, _propertyFile = null, _obdaFile = null;
	private OntopOWLReasoner _reasoner = null;
	
	public StudyRuleProcessor(String obdaFile, String owlFile, String propertyFile, String queryTemplateFile) throws IOException, IllegalConfigurationException, OWLOntologyCreationException {
		_obdaFile = obdaFile;
		_owlFile = owlFile;
		_propertyFile = propertyFile;
		
		queryTemplate = FileUtils.readFileToString(new File(queryTemplateFile));
		initReasoner();
	}
	
	/**
	 * initialise reasoner for OBDA
	 * @throws IllegalConfigurationException
	 * @throws OWLOntologyCreationException
	 */
	public void initReasoner() throws IllegalConfigurationException, OWLOntologyCreationException {
		OntopSQLOWLAPIConfiguration config = (OntopSQLOWLAPIConfiguration) OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(_obdaFile)
                .ontologyFile(_owlFile)
                .propertyFile(_propertyFile)
                .enableTestMode()
                .build();
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		_reasoner = factory.createReasoner(config);
	}
	
	/**
	 * process a list of rules in a rule file - JSON format: {ruleName -> rule text}
	 * @param ruleFile
	 * @throws JsonIOException
	 * @throws JsonSyntaxException
	 * @throws OWLOntologyCreationException
	 * @throws SWRLParseException
	 * @throws SWRLBuiltInException
	 * @throws IOException 
	 */
	public String translateRules(String ruleFile, String baseOBDAFile, String newOBDAFile) throws JsonIOException, JsonSyntaxException, OWLOntologyCreationException, SWRLParseException, SWRLBuiltInException, IOException {
		JsonObject jsonObject = new JsonParser().parse(new FileReader(new File(ruleFile))).getAsJsonObject();
		List<String> mappings = new LinkedList<String>();
		for (String name : jsonObject.keySet()) {
			String m = translateRule(name, jsonObject.get(name).getAsString());
			mappings.add(m);
			_logger.info(m);
		}
		String newMappings = String.join("\n\n", mappings);
		populateMappingFile(baseOBDAFile, newMappings, newOBDAFile);
		return newMappings;
	}
	
	/**
	 * translate a SWRL rule into a OBDA mapping string
	 * @param ruleName
	 * @param ruleText
	 * @return
	 * @throws OWLOntologyCreationException
	 * @throws SWRLParseException
	 * @throws SWRLBuiltInException
	 */
	public String translateRule(String ruleName, String ruleText) throws OWLOntologyCreationException, 
			SWRLParseException, SWRLBuiltInException {
		// Create OWLOntology instance using the OWLAPI
		 OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		 OWLOntology ontology 
		   = ontologyManager.loadOntologyFromOntologyDocument(new File(_owlFile));
		 
		 // Create a SWRL rule engine using the SWRLAPI  
		 SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);
		 
		 // Create a SWRL rule  
		 SWRLAPIRule rule = ruleEngine.createSWRLRule(ruleName, ruleText);
		 
		 Map<String, String> v2prefx = new LinkedHashMap<String, String>();
		 String bodySQL = getRuleBodySQLQueries(rule.getBodyAtoms(), v2prefx);
		 return generateMapping(ruleName, bodySQL, rule.getHeadAtoms(), v2prefx);		 
		 
		 // Run the rule engine
//		 ruleEngine.infer();
	}
	
	/**
	 * generate mappings using head atoms
	 * @param bodySQL
	 * @param headAtoms
	 * @return
	 */
	public String generateMapping(String mappingId, String bodySQL, List<SWRLAtom> headAtoms, Map<String, String> v2prefix) {
		List<String> assertions = new LinkedList<String>();
		
		// parse atoms into assertions
		for(SWRLAtom atom : headAtoms) {
			if (atom instanceof SWRLClassAtom) {
				// class assertion
				String cls = atom.getPredicate().toString();
				String var = null;
				for(SWRLArgument arg : atom.getAllArguments()) {
					if (arg instanceof SWRLVariable) {
						 var = ((SWRLVariable)arg).getIRI().getRemainder().get();
					}
				}
				assertions.add(String.format("<%s{%s}> a %s .", (v2prefix.containsKey(var)? v2prefix.get(var) : ""), var, cls));
			}else if(atom instanceof SWRLDataPropertyAtom) {
				//data value property
				String pred = atom.getPredicate().toString();
				List<String> vars = new LinkedList<String>();
				String val = "";
				for(SWRLArgument arg : atom.getAllArguments()) {
					if (arg instanceof SWRLVariable) {
						vars.add(((SWRLVariable)arg).getIRI().getRemainder().get());
					}else if (arg instanceof SWRLLiteralArgument) {
						 val = arg.toString();
					}
				}
				if (vars.size() == 2)
					assertions.add(String.format("<%s{%s}> %s <%s{%s}> .", 
							(v2prefix.containsKey(vars.get(0))? v2prefix.get(vars.get(0)) : ""), vars.get(0),
							pred, 
							(v2prefix.containsKey(vars.get(1))? v2prefix.get(vars.get(1)) : ""), vars.get(1)));
				else
					assertions.add(String.format("%s<{%s}> %s %s .", (v2prefix.containsKey(vars.get(0))? v2prefix.get(vars.get(0)) : ""), vars.get(0), pred, val));
			}else if(atom instanceof SWRLObjectPropertyAtom) {
				// object property
				String pred = atom.getPredicate().toString();
				List<String> vars = new LinkedList<String>();
				for(SWRLArgument arg : atom.getAllArguments()) {
					if (arg instanceof SWRLVariable) {
						vars.add(((SWRLVariable)arg).getIRI().getRemainder().get());
					}
				}
				if (vars.size() == 2)
					assertions.add(String.format("%s{%s} %s %s{%s} .", 
							(v2prefix.containsKey(vars.get(0))? v2prefix.get(vars.get(0)) : ""), vars.get(0), 
							pred, 
							(v2prefix.containsKey(vars.get(1))? v2prefix.get(vars.get(1)) : ""), vars.get(1)));
			}
			
		}
		String mapping = String.format(mappingTemplate, mappingId, String.join(" ", assertions), bodySQL);
		_logger.info(String.format("generated mapping: [%s]", mapping));
		return mapping;
	}
	
	/**
	 * translate body part into a SQL query using OBDA APIs
	 * @param bodyAtoms
	 * @return
	 */
	public String getRuleBodySQLQueries(List<SWRLAtom> bodyAtoms, Map<String, String> v2prefix) {
		OntopOWLConnection conn = null;
		OntopOWLStatement st = null;
		String sql = null;
		try{
			conn = _reasoner.getConnection();
			st = conn.createStatement();
			List<String> lines = new LinkedList<String>();
			
			// parse atoms into sparql query constraints
			for(SWRLAtom atom : bodyAtoms) {
				if (atom instanceof SWRLClassAtom) {
					// class assertion
					String cls = atom.getPredicate().toString();
					String var = null;
					for(SWRLArgument arg : atom.getAllArguments()) {
						if (arg instanceof SWRLVariable) {
							 var = ((SWRLVariable)arg).getIRI().getRemainder().get();
						}
					}
					lines.add(String.format("?%s a %s .", var, cls));
				}else if(atom instanceof SWRLDataPropertyAtom) {
					//data value property
					String pred = atom.getPredicate().toString();
					List<String> vars = new LinkedList<String>();
					String val = "";
					for(SWRLArgument arg : atom.getAllArguments()) {
						if (arg instanceof SWRLVariable) {
							vars.add(((SWRLVariable)arg).getIRI().getRemainder().get());
						}else if (arg instanceof SWRLLiteralArgument) {
							 val = arg.toString();
						}
					}
					if (vars.size() == 2)
						lines.add(String.format("?%s %s ?%s .", vars.get(0), pred, vars.get(1)));
					else
						lines.add(String.format("?%s %s %s .", vars.get(0), pred, val));
				}else if(atom instanceof SWRLObjectPropertyAtom) {
					// object property
					String pred = atom.getPredicate().toString();
					List<String> vars = new LinkedList<String>();
					for(SWRLArgument arg : atom.getAllArguments()) {
						if (arg instanceof SWRLVariable) {
							vars.add(((SWRLVariable)arg).getIRI().getRemainder().get());
						}
					}
					if (vars.size() == 2)
						lines.add(String.format("?%s %s ?%s .", vars.get(0), pred, vars.get(1)));
				}
				
			}
			String sparqlQuery = String.format(queryTemplate, String.join("\n", lines));
			_logger.info(String.format("SPARQL: [%s]", sparqlQuery));
	        sql = st.getExecutableQuery(sparqlQuery).toString();
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
		if (null != sql) {
			sql = clearGeneratedSQl(sql);
			sql = fixURIPrefixIssue(sql, v2prefix);
		}
		_logger.info(String.format("translated SQL is: [%s]", sql));
		return sql;
	}
	
	/**
	 * remove Ontop special character replacements, which
	 * cause syntax errors in OBDA settings
	 * @param sql
	 * @return
	 */
	public static String clearGeneratedSQl(String sql) {
		return sql.replaceAll("\n", " ")
				.replace("REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(", "")
				.replace(", '%', '%25'), ' ', '%20'), '!', '%21'), '\"', '%22'), '#', '%23'), '$', '%24'), '&', '%26'), '\\'', '%27'), '(', '%28'), ')', '%29'), '*', '%2A'), '+', '%2B'), ',', '%2C'), '/', '%2F'), ':', '%3A'), ';', '%3B'), '<', '%3C'), '=', '%3D'), '>', '%3E'), '?', '%3F'), '@', '%40'), '[', '%5B'), '\\\\', '%5C'), ']', '%5D'), '^', '%5E'), '`', '%60'), '{', '%7B'), '|', '%7C'), '}', '%7D')", "");
		
	}
	
	/**
	 * Ontop implementation requires the URI prefix to be explicitly in the source part. 
	 * This function changes the automatically created sql queries by removing variable prefixes, 
	 * and record which prefix used by which variable so that the rule head part can be changed
	 * accordingly.
	 * @param sql
	 * @param v2prefix
	 * @return
	 */
	public String fixURIPrefixIssue(String sql, Map<String, String> v2prefix) {
		String varPtn = "CAST\\(CONCAT\\('(http[^,]*)', (((?!CHAR\\(8000\\) CHARACTER SET utf8\\) AS `).)*)\\) AS CHAR\\(8000\\) CHARACTER SET utf8\\) AS `([^`]*)`";
		Pattern r = Pattern.compile(varPtn);
		
		Matcher m = r.matcher(sql);
		String newSQL = "";
		int curPos = 0;
		while(m.find()) {
			String v = m.group(4);
			String prefix = m.group(1);
			String fieldConstruct = m.group(2);
			if (fieldConstruct.indexOf(",") > 0) {
				fieldConstruct = String.format("concat(%s)", fieldConstruct);
			}
			newSQL += String.format("%s%s AS `%s`", sql.substring(curPos, m.start()),
					fieldConstruct, v);
			curPos = m.end();
			v2prefix.put(v, prefix);
		}
		newSQL += sql.substring(curPos);
		_logger.info(String.format("[%s] converted to new SQL: [%s]", sql, newSQL));
		return newSQL;
	}
	
	/**
	 * append generated mappings to base mappings and save it as a file
	 * @param baseOBDAFile
	 * @param newMappings
	 * @param newOBDAFile
	 * @throws IOException
	 */
	public static void populateMappingFile(String baseOBDAFile, String newMappings, String newOBDAFile) throws IOException {
		String s = FileUtils.readFileToString(new File(baseOBDAFile));
		int pos = s.lastIndexOf("]]");
		if (pos > 0) {
			FileUtils.writeStringToFile(new File(newOBDAFile), s.substring(0, pos) + "\n\n" + newMappings + s.substring(pos));
			_logger.info(String.format("new obda file saved to [%s]", newOBDAFile));
		}else
			_logger.warning(String.format("cannot find the right location in file [%s]", baseOBDAFile));
	}
	
	public static void main(String[] args) {
		Logger.getGlobal().setLevel(Level.INFO);
		final String miniOWLFile = "src/main/resources/lab/rule-mappings/mini-biochem-ontology.ttl";
		final String baseOBDAMappingFile = "src/main/resources/lab/rule-mappings/base-mapping.obda";
	    final String connPropertyFile = "src/main/resources/lab/rule-mappings/db-property.properties";
	    final String generatedOBDAFile = "src/main/resources/lab/generated_mappings/generated-mapping.obda";
	    final String queryTemplateFile = "src/main/resources/lab/rule-mappings/query-template.txt";
		try {
			StudyRuleProcessor p = new StudyRuleProcessor(baseOBDAMappingFile, miniOWLFile, connPropertyFile, queryTemplateFile);
			p.translateRules("src/main/resources/lab/test/swrl-rules/t2dm-rules.json",
					baseOBDAMappingFile, generatedOBDAFile);
		} catch (OWLOntologyCreationException | SWRLParseException | SWRLBuiltInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 
}
