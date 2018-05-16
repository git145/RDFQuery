 /*
  * File: ExtendAndQuery.java
  * Author: Richard Kneale
  * Student ID: 200790336 m6rk
  * COMP318: Advanced Web Technologies
  * Date created: 10th March 2017
  * Description: Can extend and query an N-Triples file
  */

package assignment1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.query.*;

public class ExtendAndQuery {
	
	/*
	 * NOTE that the file is loaded from the class-path and so requires that
	 * the data-directory, as well as the directory containing the compiled
	 * class, must be added to the class-path when running this and
	 * subsequent examples.
    */

	public static void main(String[] args) {
    	
		// Define the file to be imported
		final String inputFileName  = "dump2017.nt";
    	
		// Create an empty model
        Model model1 = ModelFactory.createDefaultModel();
        
        addBlankLine();

        // Read the file
        readFile(inputFileName, model1, "N-TRIPLE");
        
        // Define the prefixes to be used in the SPARQL queries
        final String prefixes  = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
        					   + "PREFIX nobel: <http://data.nobelprize.org/terms/>"
        					   + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"
        					   + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
        
        // Define the variables to be returned by the SPARQL queries
        String variable1 = "?discipline";
        String variable234 = "?name";
        
        // Query 1: Find the discipline for which Marie Curie won the Nobel Prize (Tip: first check through the SPARQL endpoint Marie Curie's complete name).
        String string1 = prefixes
        		+ " SELECT DISTINCT " + variable1 + " WHERE { "
        		+ "?laureate foaf:name \"Marie Curie, née Sklodowska\";"
        		+ "nobel:nobelPrize ?prize."
        		+ "?prize nobel:category ?category."
        		+ "?category rdfs:label ?discipline. }";
        
        // Query 2: Find all the Nobel Laureates who are female and that died in the USA.
        String string2 = prefixes
        		+ " SELECT DISTINCT " + variable234 + " WHERE {"
        		+ "?country rdfs:label \"USA\"."
        		+ "?laureate dbpedia-owl:deathPlace ?country;"
        		+ "foaf:gender \"female\";"
        		+ "foaf:name " + variable234 + ". }";
        
        // Query 3: Find all the Nobel laureates who where awarded the prize either in the “Physiology or Medicine” category or in Literature.
        String string3 = prefixes
        		+ " SELECT DISTINCT " + variable234 + " WHERE{"
        		+ "{ ?category rdfs:label \"Physiology or Medicine\". } "
        		+ "UNION { ?category rdfs:label \"Literature\". }."
        		+ "?laureate nobel:nobelPrize ?prize;"
        		+ "foaf:name " + variable234 + "."
        		+ "?prize nobel:category ?category. }";
        
        // Query 4: List all the Nobel Laureates ordering them by discipline for which they were awarded the prize. List the names in alphabetical order.
        String string4 = prefixes
        		+ " SELECT DISTINCT " + variable234 + " WHERE{"
        		+ "?laureate nobel:nobelPrize ?prize;"
        		+ "foaf:name " + variable234 + "."
        		+ "?prize nobel:category ?category."
        		+ "?category rdfs:label ?discipline. }"
        		+ "ORDER BY ?discipline " + variable234;
        
        // Execute the SELECT queries
        runQuery("QUERY 1", string1, variable1, model1);
        runQuery("QUERY 2", string2, variable234, model1);
        runQuery("QUERY 3", string3, variable234, model1);
        runQuery("QUERY 4", string4, variable234, model1);
        
        // Define the CONSTRUCT query
        String string5 = prefixes
        		+ " CONSTRUCT {"
        		+ "?laureate nobel:nobelPrize ?prize;"
        		+ "foaf:name ?name;"
        		+ "foaf:gender ?gender;"
        		+ "dbpedia-owl:birthPlace ?birthPlace;"
        		+ "dbpedia-owl:deathPlace ?deathPlace."
        		+ "?birthPlace rdfs:label ?birthPlaceLabel."
        		+ "?deathPlace rdfs:label ?deathPlaceLabel."
        		+ "?prize nobel:category ?category;"
        		+ "nobel:year ?year."
        		+ "?category rdfs:label ?discipline.}"
        		+ "WHERE{"
        		+ "?laureate nobel:nobelPrize ?prize;"
        		+ "foaf:name ?name;"
        		+ "foaf:gender ?gender;"
        		+ "dbpedia-owl:birthPlace ?birthPlace;"
        		+ "dbpedia-owl:deathPlace ?deathPlace."
        		+ "?birthPlace rdfs:label ?birthPlaceLabel."
        		+ "?deathPlace rdfs:label ?deathPlaceLabel."
        		+ "?prize nobel:category ?category;"
        		+ "nobel:year ?year."
        		+ "?category rdfs:label ?discipline."
        		+ "FILTER (?year >=2000) }";
        
        // Execute the CONSTRUCT query
        Model model2 = runQuery(string5, model1);
                    
        // Write the result of the CONSTRUCT query to a .ttl file
        writeFile(model2, "ModifiedNoble.ttl", "TURTLE"); 
        
        // Write the result of the CONSTRUCT query to the command window for reference
        model2.write(System.out, "TURTLE", "");
        
        addBlankLine();
        
        // Inform the user that the program has completed
        System.out.println("End of program.");
	}
	
	// Use to import a linked data file
	private static void readFile(String fileName, Model modelName, String format)
	{
		// Connect the file
		InputStream in = FileManager.get().open(fileName);
		
        // Inform the user if there is an error opening the file
		if (in == null) 
        {
            throw new IllegalArgumentException( "File: " + fileName + " not found");
        }
        
        // Read the file
        modelName.read(in, null, format);
        
        // Inform the user that the file has been read
        System.out.println("The " + format + " file has been read.\n");
	}
	
	// Use to execute a SPARQL SELECT query
	private static void runQuery(String queryName, String queryString, String variable, Model modelName)
	{
		// Print the name of the query
		System.out.println("-- " + queryName.toUpperCase() + " RESULTS --\n");
		
		// Connect the query string
		Query query = QueryFactory.create(queryString);
        
		// Attempt to execute the query
		try (QueryExecution qexec = QueryExecutionFactory.create(query, modelName)) 
        {
        	// Create the set of results
			ResultSet results = qexec.execSelect();
        	
			ResultSetFormatter.out(System.out, results, query);
			
			// Close the query
			qexec.close();
        }
        
		addBlankLine();
	}
	
	// Use to execute a SPARQL CONSTRUCT query
	private static Model runQuery(String queryString, Model modelName)
	{
		// Connect the query string
		Query query = QueryFactory.create(queryString);
        
		// Execute the query
		QueryExecution qexec = QueryExecutionFactory.create(query, modelName);
        
		// Create the model
		Model resultModel = qexec.execConstruct();
        
		// Close the query
		qexec.close();
        
		return resultModel;
	}
	
	// Writes the model to an RDF file
	private static void writeFile(Model modelName, String fileName, String format)
	{
		// Create a reference to the output file
	    File file = new File(fileName);
	        
	    // If the file does not already exist
	    if (!file.exists())
	    {
	    	try
	    	{
	    		// Create the file
	    		FileWriter writer = new FileWriter(file);

	    		// Writes the model to the RDF file
	    		modelName.write(writer, format, "");

	    		// Write any buffered output bytes
	    		writer.flush();

	    		// Close the stream
	    		writer.close();

	    		// Inform the user that the file was written successfully
	    		System.out.println("The model has been written to a " + format + " file.\n");
	    	} 
	    	catch (IOException e) 
	    	{
	    		// Inform the user that there was a problem writing the file
	    		System.out.println("There was a problem writing the file.\n");
	    	}
	    }
	    else
	    {
	    	// Inform the user that a file with the same name already exists
	    	System.out.println("The file already exists and will not be overwritten.\n");
	    }
	}
	
	// Adds a blank line to the output window for readability
	private static void addBlankLine()
	{
		
        System.out.println("");
	}
}