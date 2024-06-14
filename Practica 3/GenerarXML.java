package piat.opendatasearch;

import java.util.List;
import java.util.Map;
//Practica Realizada entre Iván Pérez Meléndez y Raúl Calderón Moya

/**
 * @author Ivan Perez Melendez - 48160420F
 * @author Raúl Calderón Moya 04264712Y
 */

/**
 * Clase estática para crear un String que contenga el documento xml a partir de
 * la información almacenadas en las colecciones
 *
 */
public class GenerarXML {
	private static final String conceptPattern= "\n\t\t\t<concept>#ID#</concept>" ;//SE USA 
	
	//private static final String summaryPattern= "\n\t\t\t<summary>#SUMMARY#</summary>" ;//SE USA
	private static final String queryPattern= "\n\t\t<query>#QUERY#</query>" ;
	private static final String numConcepts= "\n\t\t<numConcepts>#NUMCONCEPTS#</numConcepts>" ;
	private static final String numDatasets= "\n\t\t<numDatasets>#NUMDATASETS#</numDatasets>" ;
	private static final String title= "\n\t\t\t\t<title>#TITLE#</title>";
	
	private static final String datasetPattern= "\n\t\t\t<dataset id=\"#ID#\">" ;
	private static final String descriptionPattern= "\n\t\t\t\t<description>#DESCRIPTION#</description>" ;
	private static final String themePattern= "\n\t\t\t\t<theme>#THEME#</theme>" ;
	//private static final String theme= "\n\t\t\t<theme> #THEME# </theme>" ;
	//private static final String description= "\n\t\t\t<description> #DESCRIPTION# </description>" ;
	
	//private static final String numdatasetPattern = "\n\t\t\t<numConcepts> #NUMCONCEPTS# </numConcepts>" ;
	//private static final String datasetPattern= "\n\t\t\t<numDatasets> #IDDATASET# </numDatasets>" ;
	
	//private static final String titleDatasetPattern= "\n\t\t\t<results>#RESULTS#</results>" ;
	
	//private static final String descriptionPattern= "\n\t\t\t<concepts> #CONCEPTS# </concepts>" ;
	//private static final String Pattern= "\n\t\t\t<conceptS> #CONCEPTS# </conceptS>" ;
	static String codigo;
	static List<String> lista;
	static Map<String, Map<String, String>> hDatasets;
	
	public GenerarXML(String args, List<String> lista, Map<String, Map<String, String>> hDatasets) {
		// TODO Auto-generated constructor stub
		this.codigo = args;
		this.lista = lista;
		this.hDatasets = hDatasets;
	}
    
	/**  
	 * Método que deberá ser invocado desde el programa principal
	 * 
	 * @param Colecciones con la información obtenida del documento XML de entrada
	 * @return String con el documento XML de salida
	 */	
	public static String generar (List <String> lConcepts){
		StringBuilder sbSalida = new StringBuilder();
		sbSalida.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sbSalida.append("\n<searchResults xmlns=\"http://piat.dte.upm.es/ResultadosBusquedaP3\"\n"
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "xsi:schemaLocation=\"http:/piat.dte.upm.es/ResultadosBusquedaP3 ResultadosBusquedaP3.xsd\">");
		/*
		 * 		sbSalida.append("\n\t<summary>");
		sbSalida.append(queryPattern.replace("#QUERY#", codigo));
		//sbSalida.append(summaryPattern.replace("##");
		sbSalida.append(numConcepts.replace("#NUMCONCEPTS#" , Integer.toString(lista.size())));
		sbSalida.append(numDatasets.replace("#NUMDATASETS#" , Integer.toString(hDatasets.size())));
		sbSalida.append("\n\t</summary>");
		
		*/
		sbSalida.append("\n\t<summary>");
		sbSalida.append(queryPattern.replace("#QUERY#", codigo));
		//sbSalida.append(summaryPattern.replace("##");
		sbSalida.append(numConcepts.replace("#NUMCONCEPTS#" , Integer.toString(lista.size())));
		sbSalida.append(numDatasets.replace("#NUMDATASETS#" , Integer.toString(hDatasets.size())));
		sbSalida.append("\n\t</summary>");
		

		sbSalida.append("\n\t<results>");
		
		sbSalida.append("\n\t\t<concepts>" );
		
		for (String unConcepto : lConcepts){
		sbSalida.append (conceptPattern.replace("#ID#", unConcepto));
		}
		
		sbSalida.append("\n\t\t</concepts>" );
		
		
		
		sbSalida.append("\n\t\t<datasets>");
		Map<String, String> mapaPrincipal;
		
		//sbSalida.append("\n\t\t\t<dataset>");
		for(String id: hDatasets.keySet()){   
			mapaPrincipal = hDatasets.get(id);
			sbSalida.append(datasetPattern.replace("#ID#", id));
			//sbSalida.append()
			sbSalida.append(title.replace("#TITLE#", mapaPrincipal.get("title")));
			sbSalida.append(descriptionPattern.replace("#DESCRIPTION#", mapaPrincipal.get("description")));
			sbSalida.append(themePattern.replace("#THEME#", mapaPrincipal.get("theme")));
			sbSalida.append("\n\t\t\t</dataset>");
        }
		
		sbSalida.append("\n\t\t</datasets>");
		sbSalida.append("\n\t</results>");
		sbSalida.append("\n</searchResults>");
		
		
		return sbSalida.toString(); // Salida de un documento XML a partir de la lista y el mapa 
     }
	
	

	
}
