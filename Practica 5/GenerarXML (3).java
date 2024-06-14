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
 */public class GenerarXML {
		private static final String title			= "\n\t\t\t\t<title>#valor#</title>";
		private static final String description	= "\n\t\t\t\t<description>#description#</description>";
		private static final String theme			= "\n\t\t\t\t<theme>#valor#</theme>";
		private static final String concept		= "\n\t\t\t<concept>#ID#</concept>" ;
		private static final String dataset		= "\n\t\t\t<dataset id=\"#ID#\">";

		/******************************************* Practica 4 *****************************************/
		private static final String resource		= "\n\t\t\t<resource id=\"#ID#\">";
		private static final String concept2		= "\n\t\t\t<concept id=\"#ID#\"/>" ;
		
		private static final String start= "\n\t\t\t\t\t\t\t<start>#start#</start>";
		private static final String end= "\n\t\t\t\t\t\t\t<end>#end#</end>";
		private static final String georeference= "\n\t\t\t\t\t\t<georeference>#georeference#</georeference>";
		
		private static final String street= "\n\t\t\t\t\t\t\t\t<street>#street#</street>";
		private static final String eventLocation= "\n\t\t\t\t\t\t<eventLocation>#valor#</eventLocation>";
		private static final String link= "\n\t\t\t\t<link> <![CDATA[#pLink#]]> </link>" ;
		
		private static final String area= "\n\t\t\t\t\t\t\t\t<area>#area#</area>";
		private static final String locality= "\n\t\t\t\t\t\t\t\t<locality>#locality#</locality>";
		/**  
		 * Método que deberá ser invocado desde el programa principal
		 * Le metemos la lista y mapa gordo para que nos lo ponga en formato XML , y lo convierte en un String, que habra que meter 
		 * en nuestro fichero de salida
		 * 
		 * @param Colecciones con la información obtenida del documento XML de entrada
		 * @return String con el documento XML de salida
		 */	
		public static String generar (List<String> lConcepts, Map<String, Map<String, String>> hDatasets,
						String [] args, Map<String, List<Map<String, String>>> map){
			StringBuilder salidaXML= new StringBuilder();
			salidaXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			salidaXML.append("<searchResults \txmlns=\"http://piat.dte.upm.es/practica4\"");
			salidaXML.append("\n\t\t\t\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
			salidaXML.append("\n\t\t\t\txsi:schemaLocation=\"http://piat.dte.upm.es/practica4 ResultadosBusquedaP4.xsd\">");
			salidaXML.append("\n\t<summary>");
			salidaXML.append("\n\t\t<query>"+args[0]+"</query>");
			salidaXML.append("\n\t\t<numConcepts>"+lConcepts.size()+"</numConcepts>");
			salidaXML.append("\n\t\t<numDatasets>"+hDatasets.size()+"</numDatasets>");
			salidaXML.append("\n\t</summary>");
			salidaXML.append("\n\t<results>");

			salidaXML.append("\n\t\t<concepts>");
			for (String aux : lConcepts) {
				salidaXML.append(concept.replace("#ID#", aux));
			}
			salidaXML.append("\n\t\t</concepts>");
			if(hDatasets.size() != 0){

				salidaXML.append("\n\t\t<datasets>");
				for (Map.Entry<String, Map<String, String>> entry : hDatasets.entrySet()) {
					Map<String, String> mapaValor = entry.getValue();
					if (mapaValor.containsKey("title") && mapaValor.containsKey("description") && mapaValor.containsKey("theme")) {
						salidaXML.append(dataset.replace("#ID#", entry.getKey()));
						salidaXML.append(title.replace("#valor#", mapaValor.get("title")));
						salidaXML.append(description.replace("#valor#", mapaValor.get("description")));
						salidaXML.append(theme.replace("#valor#", mapaValor.get("theme")));
						salidaXML.append("\n\t\t\t</dataset>");
					}
				}
				salidaXML.append("\n\t\t</datasets>");
				generarResources(salidaXML, map);
			}
			salidaXML.append("\n\t</results>");
			salidaXML.append("\n</searchResults>");
			return salidaXML.toString();
		}

		/******************************************************************************** Práctica 4  Continuacion *********************/

		public static void generarResources (StringBuilder salidaXML, Map<String, List<Map<String,String>>> mapa){
			salidaXML.append("\n\t\t<resources>");
			
			for (Map.Entry<String, List<Map<String, String>>> entry : mapa.entrySet()) {
				List<Map<String, String>> lista = entry.getValue();
				for (Map<String, String> mapaValor : lista) {
						
					salidaXML.append (resource.replace("#ID#", entry.getKey()));
					if(mapaValor.get("@type") == null) {
						//salidaXML.append (concept2.replace("#ID#", ""));
					}else {
						salidaXML.append (concept2.replace("#ID#", mapaValor.get("@type")));
					}
					
					if(mapaValor.get("link") == null) {
						//salidaXML.append (link.replace("#pLink#", ""));
					}else {
						salidaXML.append (link.replace("#pLink#", mapaValor.get("link")));
					}
					
					if(mapaValor.get("title") == null) {
						//salidaXML.append (title.replace("#valor#", ""));
						
					}else{
						salidaXML.append (title.replace("#valor#", mapaValor.get("title")));
					}
					
					
					
					
					//Inicio location en XML
					salidaXML.append("\n\t\t\t\t<location>" );
					
					if(mapaValor.get("eventLocation") == null) {
						//salidaXML.append (eventLocation.replace("#valor#", ""));
						
					}else{
						salidaXML.append (eventLocation.replace("#valor#", mapaValor.get("eventLocation")));
					}
					
					
					
					//Inicion Address XML
					salidaXML.append("\n\t\t\t\t\t\t<address>" );
					
					if(mapaValor.get("area") == null) {
						//salidaXML.append (area.replace("#area#", ""));
						
					}else{
						salidaXML.append (area.replace("#area#", mapaValor.get("area")));
					}
					
					if( mapaValor.get("locality") == null) {
						//salidaXML.append (locality.replace("#locality#", ""));
						
					}else{
						salidaXML.append (locality.replace("#locality#", mapaValor.get("locality")));
					}
					
					if( mapaValor.get("street-address") == null) {
						//salidaXML.append (street.replace("#street#", ""));
						
					}else{
						salidaXML.append (street.replace("#street#", mapaValor.get("street-address")));
					}
					
					
					salidaXML.append("\n\t\t\t\t\t\t</address>" );
					//Fin Address XML
					
					//Inicion Timetable
					salidaXML.append("\n\t\t\t\t\t\t<timetable>" );
					
					if( mapaValor.get("dtstart") == null) {
						//salidaXML.append (start.replace("#start#", ""));
						
					}else{
						salidaXML.append (start.replace("#start#", mapaValor.get("dtstart")));
					}
					
					if(  mapaValor.get("dtend") == null) {
						//salidaXML.append (end.replace("#end#", ""));
						
					}else{
						salidaXML.append (end.replace("#end#", mapaValor.get("dtend")));
					}
					
					
					salidaXML.append("\n\t\t\t\t\t\t</timetable>" );
					//Fin timetable
					
					if(  mapaValor.get("latitude") == null || mapaValor.get("longitude") == null) {
						//salidaXML.append (georeference.replace("#georeference#", ""));
						
					}else{
						salidaXML.append (georeference.replace("#georeference#", mapaValor.get("latitude") + " " + mapaValor.get("longitude")));
					}
					
					
					salidaXML.append("\n\t\t\t\t</location>" );
					//Fin location XML
					if(  mapaValor.get("description") == null ) {
						//salidaXML.append (description.replace("#description#", ""));
						
					}else{
						salidaXML.append (description.replace("#description#", mapaValor.get("description")));
					}
					
					
					
					salidaXML.append("\n\t\t\t</resource>");//Fin resource
					
				}
				
			}
			
			salidaXML.append("\n\t\t</resources>");//Fin del resources 
		}
		

	}