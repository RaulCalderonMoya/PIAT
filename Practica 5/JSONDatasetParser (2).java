package piat.opendatasearch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.stream.JsonReader;

public class JSONDatasetParser implements Runnable {
	private String fichero;
	private List<String> lConcepts;
	private Map<String, List<Map<String, String>>> mDatasetConcepts;
	private String nombreHilo;

	public JSONDatasetParser(String fichero, List<String> lConcepts,
			ConcurrentHashMap<String, List<Map<String, String>>> mDatasetConcepts) {
		this.fichero = fichero;
		this.lConcepts = lConcepts;
		this.mDatasetConcepts = mDatasetConcepts;
	}

	@Override
	public void run() {
		List<Map<String, String>> graphs = new ArrayList<Map<String, String>>();
		boolean finProcesar = false;

		Thread.currentThread().setName("JSON " + fichero);
		nombreHilo = "[" + Thread.currentThread().getName() + "] ";
		System.out.println(nombreHilo + "Empezar a descargar de internet el JSON");
		try {
			InputStreamReader inputStream = new InputStreamReader(new URL(fichero).openStream(), "UTF-8");
			// TODO:
			// - Crear objeto JsonReader a partir de inputStream
			// - Consumir el primer "{" del fichero
			// - Procesar los elementos del fichero JSON, hasta el final de fichero o hasta
			// que finProcesar=true
			// Si se encuentra el objeto @graph, invocar a procesar_graph()
			// Descartar el resto de objetos
			// - Si se ha llegado al fin del fichero, consumir el último "}" del fichero
			// - Cerrar el objeto JsonReader
			JsonReader jsonReader = new JsonReader(inputStream);
			jsonReader.setLenient(true); //setLenient(true) se usa para hacer skipValue en el default de los switches al procesar el JSON
			
			// inicio del consumo de los evantos del fichero json
			
			//Nota:{ --> beginObject 
			//Nota:}--> endObject
			//Nota:[ --> beginArray
            //Nota:] --> endArray
			
			jsonReader.beginObject();//Inicio de { que es begin object
			while (jsonReader.hasNext()) {
				// jsonReader.beginObject(); // Inicio de { que es begin object
				String name = jsonReader.nextName();
				if (name.equals("@graph")) {
					finProcesar = procesar_graph(jsonReader, graphs, lConcepts);
					if (finProcesar)
						break;
				} else
					jsonReader.skipValue();//Equivale al default del switch esto se pondría ahí
				
			}
			// jsonReader.endObject(); // Fin del }
			
			//- Cerrar el objeto JsonReader
			//Cerramos también el inputStream
			jsonReader.close();
			inputStream.close();
			
		} catch (FileNotFoundException e) {
			System.out.println(nombreHilo + "No se ha localizado o no existe le fichero.......");
		} catch (IOException e) {
			System.out.println(nombreHilo + "No se puedo abrir el fichero......" + e);
		}
		
		mDatasetConcepts.put(fichero, graphs); // Se añaden al Mapa de concepts de los Datasets

	}

	/*
	 * procesar_graph() Procesa el array @graph Devuelve true si ya se han añadido 5
	 * objetos a la lista graphs
	 */
	private boolean procesar_graph(JsonReader jsonReader, List<Map<String, String>> graphs, List<String> lConcepts)
			throws IOException {
		boolean finProcesar = false;
		// TODO:
		// - Consumir el primer "[" del array @graph
		// - Procesar todos los objetos del array, hasta el final de fichero o hasta que
		// finProcesar=true
		// - Consumir el primer "{" del objeto
		// - Procesar un objeto del array invocando al método procesar_un_graph()
		// - Consumir el último "}" del objeto
		// - Ver si se han añadido 5 graph a la lista, para en ese caso poner la
		// variable finProcesar a true
		// - Si se ha llegado al fin del array, consumir el último "]" del array
		jsonReader.beginArray();
		while (jsonReader.hasNext()) {
			jsonReader.beginObject();
			procesar_un_graph(jsonReader, graphs, lConcepts);
			jsonReader.endObject();
			if (graphs.size() == 5) {
				finProcesar = true;
				break;
			}//Con poner break; lo que hacemos es decir que si se llega a dicho break; termine ahí la ejecución
			 //Con lo cual cuando graphs tiene tamaño 5 entra pone el flag a true y deja de realizar tareas ya que no debe seguir realizando
			 //mas tareas si graphs llega a 5 
			
		}
		// jsonReader.endArray();
		return finProcesar;
	}

	/*
	 * procesar_un_graph() Procesa un objeto del array @graph y lo añade a la lista
	 * graphs si en el objeto de nombre @type hay un valor que se corresponde con
	 * uno de la lista lConcepts
	 */

	private void procesar_un_graph(JsonReader jsonReader, List<Map<String, String>> graphs, List<String> lConcepts) throws IOException {
		// TODO:
		//	- Procesar todas las propiedades de un objeto del array @graph, guardándolas en variables temporales
		//	- Una vez procesadas todas las propiedades, ver si la clave @type tiene un valor igual a alguno de los concept de la lista lConcepts. Si es así
		//	  guardar en un mapa Map<String,String> todos los valores de las variables temporales recogidas en el paso anterior y añadir este mapa al mapa graphs
		Map<String, String> map = new HashMap<String, String>();
		
		while (jsonReader.hasNext()){
			switch(jsonReader.nextName()){
			   //Inicio de procesado del JSON
			   //Se debe procesar teniento en cuenta que :
			//Nota:{ --> beginObject 
			//Nota:}--> endObject
			//Nota:[ --> beginArray
            //Nota:] --> endArray
			
			//Por ejemplo en address tenemos más subcampos que son object({})con lo que debemos hacer el bucle de recorrido correcto
			//
			/*while(jsonReader.hasNext()){
			   switch(jsonReader.nextName()){
			       Valores posibles del switch a procesar en Json
			   }
			}
			
			*
			*/
			
				case "@type":
					String type = jsonReader.nextString();
					//En el campo tipo se debe antes de introducir en el mapa que realmente type posee valor contenido en lConcepts
					
					if (lConcepts.contains(type)){
						map.put("@type", type);
					}
					break;
					
				case "@id":
					map.put("@id", jsonReader.nextString());
					break;
					
				case "link":
					map.put("link", jsonReader.nextString());
					break;
					
				case "title":
					map.put("title", jsonReader.nextString());
					break;
					
				case "event-location":
					map.put("eventLocation", jsonReader.nextString());
					break;
					
				case "address":
					jsonReader.beginObject();//{
					while(jsonReader.hasNext()){
						switch(jsonReader.nextName()){
						
							case "area":
							jsonReader.beginObject();//{
							while (jsonReader.hasNext()) {
								switch (jsonReader.nextName()) {				// Se procesan las propiedades que interean
								
								case "@id":	
									map.put("area", jsonReader.nextString());
									break;
									
								case "locality":	
									 map.put("locality",jsonReader.nextString());
										break;
										
								case "street-address":
									 map.put("street-address",jsonReader.nextString());
									break;
									
								default:	
									jsonReader.skipValue();//Valor que sirve para que todo aquello que no pertenezca a lo que buscamos no sea procesado
								}
							}
							jsonReader.endObject();//}
							
							break;		
						default:
							jsonReader.skipValue();//Valor que sirve para que todo aquello que no pertenezca a lo que buscamos no sea procesado
						}
					}
					jsonReader.endObject();//}
					break;
					
				case "dtstart":
					map.put("dtstart", jsonReader.nextString());
					break;
					
				case "dtend":
					map.put("dtend", jsonReader.nextString());
					break;
					
				case "location":
					jsonReader.beginObject();//{
					
					while (jsonReader.hasNext()) {	
						switch (jsonReader.nextName()) {
						
						case "latitude":
						map.put("latitude",jsonReader.nextString());
							break;
							
						case "longitude":
							map.put("longitude",jsonReader.nextString());
							break;
							
						default:	
							jsonReader.skipValue();//Esta linea no llega a ejecutarse nunca segun el debug aunque esta bien ponerla para no procesar lo que no hace falta procesar
						}
						
					}
					
					jsonReader.endObject();//}
					break;
					
				case "description":
					map.put("description", jsonReader.nextString());
					break;
					
				default:
					jsonReader.skipValue();
				}
			}
		
	    //	- Una vez procesadas todas las propiedades, ver si la clave @type tiene un valor igual a alguno de los concept de la lista lConcepts. Si es así
		//	  guardar en un mapa Map<String,String> todos los valores de las variables temporales recogidas en el paso anterior y añadir este mapa al mapa graphs
		if (lConcepts.contains(map.get("@type")) && graphs.contains(map) == false){
			graphs.add(map);
		}
	}
}