package piat.opendatasearch;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Position;

import com.google.gson.stream.JsonWriter;

import piat.opendatasearch.XPATH_Evaluador.Propiedad;

/*
 * @author Raul Calderon Moya 04264712Y
 * **/

/**
 * Clase usada para crear un fichero JSON
 * Contiene un método estático, llamado generar() que se encarga de generar un String en formato JSON a partir de la lista de propiedades que se le pasa como parámetro 
 */
public class GenerarJSON {

	/**
	 * Método que se encarga de generar un String en formato JSON a partir de la lista de propiedades que se le pasa como parámetro
	 * @param	ficheroJSONSalida	Nombre del fichero JSON de salida
	 * @param	listaPropiedades	Lista de propiedades
	 */
	public static void generar(String ficheroJSONSalida,List<Propiedad> listaPropiedades) {
		// TODO:
		// - Crear objeto de la clase JsonWriter usando como fichero el parámetro ficheroJSONSalida
		// - Crear los arrays y objetos que debe tener el fichero JSON de salida
		
		try {
			int recorredora;
			JsonWriter writer = new JsonWriter(new FileWriter(ficheroJSONSalida));
			
			writer.setIndent("\t");
			writer.beginObject();
			if((recorredora = valorPropiedad("query", listaPropiedades))>= 0) {
				writer.name(listaPropiedades.get(recorredora).nombre).value(listaPropiedades.get(recorredora).valor);
			}
			
			if((recorredora = valorPropiedad("numResources", listaPropiedades))>= 0) {
				writer.name(listaPropiedades.get(recorredora).nombre).value(Integer.parseInt(listaPropiedades.get(recorredora).valor));
				//writer.name(listaPropiedades.get(posicion).nombre).value(Integer.parseInt(listaPropiedades.get(posicion).valor)); // ---->  "numeroResources": "10",

			}
			
			if((recorredora = valorPropiedad("id", listaPropiedades))>= 0) {
				writer.name("infDataset");
				writer.beginArray();
				while(recorredora<listaPropiedades.size() && listaPropiedades.get(recorredora).nombre.equals("id")) {
					writer.beginObject();
					writer.name(listaPropiedades.get(recorredora).nombre).value(listaPropiedades.get(recorredora).valor);
					recorredora++;	
					writer.name(listaPropiedades.get(recorredora).nombre).value(listaPropiedades.get(recorredora).valor); 
					writer.endObject();
				    recorredora++;
				}
				
				writer.endArray();
			}
			
			if((recorredora = valorPropiedad("eventLocation", listaPropiedades))>= 0) {
				List<String> listaUbicaciones = new ArrayList<String>();
				for (int i=0;i<listaPropiedades.size();i++){
					if(listaPropiedades.get(i).nombre.equals("eventLocation") && listaUbicaciones.contains(listaPropiedades.get(i).valor)==false
					&& !listaPropiedades.get(i).valor.equals("")){
						listaUbicaciones.add(listaPropiedades.get(i).valor);
					}
				}
				writer.name("ubicaciones");        
				writer.beginArray();               
				for (int i=0;i<listaUbicaciones.size();i++){
					writer.value(listaUbicaciones.get(i));  
				}
				writer.endArray();    
			}
			
			
			/* 	writer.value(listaPropiedades.get(posicion).valor); // ----> "Centro Cultural Antonio Machado (San Blas - Canillejas)",
			//writer.beginObject();
			//writer.name(listaPropiedades.get(posicion).nombre).value(listaPropiedades.get(posicion).valor);
			//writer.endObject();
			posicion++;              
			while (posicion<listaPropiedades.size() && listaPropiedades.get(posicion).nombre.equals("eventLocation")){
				writer.value(listaPropiedades.get(posicion).valor);  //  -----> "segundo", "cuarto"
				//writer.beginObject();
				//writer.name(listaPropiedades.get(posicion).nombre).value(listaPropiedades.get(posicion).valor);
				//writer.endObject();
				posicion++;
			}          
			writer.endArray();     // ----> ]
		} */
			
			

			writer.endObject();
			writer.close();
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private static int valorPropiedad(String nombre, List<Propiedad> listaPropiedades){
		for (int i=0;i<listaPropiedades.size();i++) {
			if(listaPropiedades.get(i).nombre.equals(nombre)) {
				System.out.println("Valor de i:" + i);
				System.out.println("Nombre buscado: "+nombre);
				System.out.println("Valor de la lista" + listaPropiedades.get(i).toString());
				return i;
			}
		}
		return -1;
	}

}

