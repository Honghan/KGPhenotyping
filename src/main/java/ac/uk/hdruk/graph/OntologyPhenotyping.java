package ac.uk.hdruk.graph;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.parser.SWRLParseException;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;

public class OntologyPhenotyping {
	Logger _logger = Logger.getLogger(OntologyPhenotyping.class.toString());
	// singleton instance
	static OntologyPhenotyping _instance = null;
	private String _configFile = null, _studiesFolder = null;
	private String _obdaFile, _ontologyFile, _dbPropertyFile, _queryTemplateFile;
	private StudyRuleProcessor ruleProcessor;
	
	/**
	 * private constructor
	 * @param configFile
	 * @throws ConfigurationException 
	 * @throws IOException 
	 * @throws OWLOntologyCreationException 
	 * @throws IllegalConfigurationException 
	 */
	private OntologyPhenotyping(String configFile) throws ConfigurationException, IllegalConfigurationException, OWLOntologyCreationException, IOException {
		_configFile = configFile;
		init();
	}
	
	/**
	 * get the singleton instance
	 * @param configFile
	 * @return
	 * @throws ConfigurationException
	 * @throws IOException 
	 * @throws OWLOntologyCreationException 
	 * @throws IllegalConfigurationException 
	 */
	public static OntologyPhenotyping getInstance(String configFile) throws ConfigurationException, IllegalConfigurationException, OWLOntologyCreationException, IOException {
		if (_instance == null) {
			_instance = new OntologyPhenotyping(configFile);
		}
		return _instance;
	}
	
	/**
	 * initialise basic configurations
	 * @throws ConfigurationException
	 * @throws IOException 
	 * @throws OWLOntologyCreationException 
	 * @throws IllegalConfigurationException 
	 */
	void init() throws ConfigurationException, IllegalConfigurationException, OWLOntologyCreationException, IOException {
		Configurations configs = new Configurations();
		// Read data from this file
		File propertiesFile = new File(_configFile);
		FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
			    configs.propertiesBuilder(propertiesFile);
		Configuration config = builder.getConfiguration();
		
		//init study folder
		_studiesFolder = getMandatoryStringConfig(config, "STUDY_FOLDER");
		File directory = new File(_studiesFolder);
	    if (! directory.exists()){
	        directory.mkdirs();
	    }
	    
	    _obdaFile = getMandatoryStringConfig(config, "OBDA_FILE");
	    _ontologyFile = getMandatoryStringConfig(config, "ONTOLOGY_FILE");
	    _dbPropertyFile = getMandatoryStringConfig(config, "DB_PROPERTY_FILE");
	    _queryTemplateFile = getMandatoryStringConfig(config, "QUERY_TEMPLATE");	    
	    
	    // initialise studyRuleProcessor
	    ruleProcessor = new StudyRuleProcessor(_obdaFile, _ontologyFile, _dbPropertyFile, _queryTemplateFile);
	}
	
	/**
	 * get mandatory string value configuration
	 * @param config
	 * @param key
	 * @return
	 * @throws ConfigurationException
	 */
	static String getMandatoryStringConfig(Configuration config, String key) throws ConfigurationException {
		if (!config.containsKey(key))
	    	throw new ConfigurationException("OBDA_FILE not found in the configuration file");
	    else
	    	return config.getString(key);
	}
	
	/**
	 * create or update a study folder with specified SWRL rules (in JSON)
	 * @param studyName
	 * @param ruleJSON
	 * @throws IOException
	 * @throws JsonIOException
	 * @throws JsonSyntaxException
	 * @throws OWLOntologyCreationException
	 * @throws SWRLParseException
	 * @throws SWRLBuiltInException
	 */
	public void createUpdateStudy(String studyName, String ruleJSON) throws IOException, JsonIOException, JsonSyntaxException, OWLOntologyCreationException, SWRLParseException, SWRLBuiltInException {
		String studyFolder = _studiesFolder + "/" + studyName;
		File directory = new File(studyFolder);
	    if (! directory.exists()){
	    	_logger.info(String.format("study folder [%s] not found, creating...", studyFolder));
	        directory.mkdir();
	    }
	    	    
	    File ruleFile = new File(studyFolder + "/" + "rules.json");
	    FileUtils.writeStringToFile(ruleFile, ruleJSON);
	    
	    String studyOBDAFile = studyFolder + "/" + "generated.obda";
	    if (ruleJSON == null || ruleJSON.length() == 0)
	    	FileUtils.copyFile(new File(_obdaFile), new File(studyOBDAFile));
	    else {
	    	ruleProcessor.translateRules(ruleFile.getAbsolutePath(), _obdaFile, studyOBDAFile);
	    }	    
	}
	
	/**
	 * List the existing studies
	 * @return List of studies where each study is a JSON String 
	 */
	public List<String> listStudy() throws IOException {
                ArrayList<String> result = new ArrayList<String>(); 
                File directory = new File(_studiesFolder);
                if (! directory.exists()){
			return result;
                }
                File[] dirList = directory.listFiles();
	        for (File dir: dirList) {
			File rulesFile = new File(dir.getAbsolutePath()+"/rules.json");
			if (rulesFile.exists()){
				String rules = new String (Files.readAllBytes(Paths.get(rulesFile.getPath())));
	 	                result.add(new String("{\"name\":\""+dir.getName()+"\",\"rules\":"+rules+"}"));
			}
	        }
		return result;
	}

	/**
	 * Get an existing studies
	 * @return JSON String representing a study 
	 */
	public String getStudy(String name) throws IOException {
                String result = "";
                File directory = new File(_studiesFolder+"/"+name);
                if (! directory.exists()){
			throw new IOException("NOT FOUND");
                }
		File rulesFile = new File(_studiesFolder+"/"+name+"/rules.json");
		if (rulesFile.exists()){
		        String rules = new String (Files.readAllBytes(Paths.get(rulesFile.getPath())));
	                result = "{\"name\":\""+name+"\",\"rules\":"+rules+"}";
		}
		return result;
	}

	public String doStudyQuery(String sparqlQuery, String studyName) {
		String studyOBDAFile = _studiesFolder + "/" + studyName + "/" + "generated.obda";
		// initialise obda reasoner
	    OntopSQLOWLAPIConfiguration ontopConfig = (OntopSQLOWLAPIConfiguration) OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(studyOBDAFile)
                .ontologyFile(_ontologyFile)
                .propertyFile(_dbPropertyFile)
                .enableTestMode()
                .build();
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		OntopOWLReasoner reasoner = null;
		OntopOWLConnection conn = null;
		OntopOWLStatement st = null;
		try{
			reasoner = factory.createReasoner(ontopConfig);
			conn = reasoner.getConnection();
			st = conn.createStatement();			
			TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);			
			SPARQLResult result = new SPARQLResult();
			result.setVariables(rs.getSignature().toArray(new String[0]));			
			
            int columnSize = rs.getColumnCount();
            while (rs.hasNext()) {
            	List<Object> row = new LinkedList<Object>();
                final OWLBindingSet bindingSet = rs.next();
                for (int idx = 1; idx <= columnSize; idx++) {
                    OWLObject binding = bindingSet.getOWLObject(idx);
                    row.add(ToStringRenderer.getInstance().getRendering(binding));
                }
                result.addResults(row);
            }
            rs.close();
            
            return new Gson().toJson(result);
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
		return null;
	}
	
	static class SPARQLResult{
		private String[] variables;
		private List<List<Object>> rows;
		
		public SPARQLResult(){
			rows= new LinkedList<List<Object>>();
		}
		
		public String[] getVariables() {
			return variables;
		}
		public void setVariables(String[] variables) {
			this.variables = variables;
		}
		public List<List<Object>> getRows() {
			return rows;
		}
		public void addResults(List<Object> row) {
			this.rows.add(row);
		}
		
	}
}
