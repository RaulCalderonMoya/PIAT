package piat.opendatasearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
//Practica Realizada entre Iván Pérez Meléndez y Raúl Calderón Moya
/**
 * @author Ivan Perez Melendez - 48160420F
 * @author Raúl Calderón Moya 04264712Y
 */

/**
 * Clase principal de la aplicación de extracción de información del 
 * Portal de Datos Abiertos del Ayuntamiento de Madrid
 *
 */
public class P3_SAX {


	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
		
		// Verificar nº de argumentos correcto
		if (args.length!=4){
			String mensaje="ERROR: Argumentos incorrectos.";
			if (args.length>0)
				mensaje+=" He recibido estos argumentos: "+ Arrays.asList(args).toString()+"\n";
			mostrarUso(mensaje);
			System.exit(1);
		}		
		
		// TODO
		/* 
		 * Validar los argumentos recibidos en main()
		 * Instanciar un objeto ManejadorXML pasando como parámetro el código de la categoría recibido en el primer argumento de main()
		 * Instanciar un objeto SAXParser e invocar a su método parse() pasando como parámetro un descriptor de fichero, cuyo nombre se recibió en el primer argumento de main(), y la instancia del objeto ManejadorXML 
		 * Invocar al método getConcepts() del objeto ManejadorXML para obtener un List<String> con las uris de los elementos <concept> cuyo elemento <code> contiene el código de la categoría buscado
		 * Invocar al método getDatasets() del objeto ManejadorXML para obtener un mapa con los datasets de la categoría buscada
		 * Crear el fichero de salida con el nombre recibido en el cuarto argumento de main()
		 * Volcar al fichero de salida los datos en el formato XML especificado por ResultadosBusquedaP3.xsd
		 * Validar el fichero generado con el esquema recibido en el tercer argumento de main()
		 */
		
		//Validar los argumentos recibidos en main()
		//Debemos crear una expresion regular para verificar que los codigos de ARG0 son los correctos
		
		//"\\d{3,4}(-[A-Z0-9]{3,8})?"
        final String regex ="^[0-9]{3,4}(-[0-9A-Z]{3,8})*$";
        final String string = args[0];
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(string);
        
//        while (matcher.find()) {
//            System.out.println("Full match: " + matcher.group(0));
//            
//            for (int i = 1; i <= matcher.groupCount(); i++) {
//                System.out.println("Group " + i + ": " + matcher.group(i));
//            }
//        }
        if(!matcher.find()) {
        	System.out.println("ERROR! ARG0 no se corresponde con el valor esperado");
        }
        
        //Una vez verificado ARGO nos ponemos con ARG1
        final String regex1 = ".*\\.xml$";
        final String string1 = args[1];
        
        final Pattern pattern1 = Pattern.compile(regex1, Pattern.MULTILINE);
        final Matcher matcher1 = pattern1.matcher(string1);
        
        if(!matcher1.find()) {
        	System.out.println("ERROR! ARG1 no tiene el formato esperado");
        }
        
        //Ahora pasamos a verificar el ARG2
        final String regex2 = ".*\\.xsd$";
        final String string2 = args[2];
        
        final Pattern pattern2 = Pattern.compile(regex2, Pattern.MULTILINE);
        final Matcher matcher2 = pattern2.matcher(string2);
        
        if(!matcher2.find()) {
        	System.out.println("ERROR! ARG2 no se corresponde con el valor esperado");
        }
        //Verificacion de ARG3
        final String regex3 = ".*\\.xml$";
        final String string3 = args[1];
        
        final Pattern pattern3 = Pattern.compile(regex3, Pattern.MULTILINE);
        final Matcher matcher3 = pattern3.matcher(string3);
        
        if(!matcher3.find()) {
        	System.out.println("ERROR! ARG3 no se corresponde con el valor esperado");
        }
        
        //Una vez verificados se debe mirar que ARG1 Y ARG2 que son documentos .xml y .xsd respectivamente
        //se pueden leer
        File fileArg1 = new File(args[1]);
        if(fileArg1 != null && fileArg1.canRead()) {
           System.out.println("Archivo ARGV1 encontrado y preparando para abrir....");
        }else if(!fileArg1.canRead()){
        	System.out.println("Archivo ARGV1 sin permisos de lectura");
        }else {
        	System.out.println("Archivo ARGV1 no encontrado");
        }
        
        File fileArg2 = new File(args[2]);
        if(fileArg2 != null && fileArg2.canRead()) {
            System.out.println("Archivo ARGV2 encontrado y preparando para abrir....");
         }else if(!fileArg2.canRead()){
         	System.out.println("Archivo ARGV2 sin permisos de lectura");
         }else {
         	System.out.println("Archivo ARGV2 no encontrado");
         }
        
        File fileArg3 = new File(args[3]);
        
        //En este caso debemos crear un nuevo archivo y ver si tiene permisos de escritura
        fileArg3.createNewFile();//Se crea un nuevo archivo, dejar esta linea de manera provisional hasta que se verifique si
        //hay que usar o no esta sentencia u otra cosa        
        if(fileArg3.canWrite()) {
            System.out.println("Archivo ARGV3 con permisos de escritura encontrado");
         }else {
        	 System.out.println("Archivo ARGV3 sin permisos de escritura");
         }    
       //Instanciar un objeto ManejadorXML pasando como parámetro el código de la categoría recibido en el primer argumento de main()
        //Ahora debemos analizar el documento ARG1 con el manejador
        
       
        SAXParserFactory factory =SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
      //Instanciar un objeto SAXParser e invocar a su método parse() pasando como parámetro un descriptor de fichero, cuyo nombre se recibió en el primer argumento de main(), y la instancia del objeto ManejadorXML
        ManejadorXML manejador = new ManejadorXML(args[0]);
        parser.parse(fileArg1, manejador);
        
        //Nos creamos un listado de uri
        
       List<String> lista = new ArrayList<String>();
        lista = manejador.getConcepts();
        if(lista.isEmpty()) {
        	System.out.println("La lista esta vacia");
        }
      //Invocar al método getConcepts() del objeto ManejadorXML para obtener un List<String> con las uris de los elementos <concept> cuyo elemento <code> contiene el código de la categoría buscado
	  System.out.println("Listado con numero de elementos: "+lista.size());
	   int aux = 0;
	  //Recorremos la lista y mostramos su contenido
	  for(String valor: lista) {
		  aux ++;
		  System.out.println(valor);
	  }

	  /*	  Map <String, Map<String,String>> hDatasetsInicio;	// Mapa con información de los dataset que pertenecen a la categoría
	  
	  for(String w: hDatasetsInicio) {
		  
	  }*/
	  
        
  	 //  Map<String, String>mapaInterno = new HashMap<String, String>();

	   /*	  
	    * 	   mapaInterno = manejador.getMapaInterno();
	   System.out.println("Vamos a entrar al mapa interno");
	    *  for (Map.Entry entry : mapaInterno.entrySet()) {
	       System.out.println("La clave es: \s" + entry.getKey()+"\s" + "y el valor es: \s" + entry.getValue());
	   }*/

	   
	   Map <String, Map<String,String>> hDatasets;
	   hDatasets = manejador.getDatasets();
	   System.out.println("El mapa de mapas posee un tamaño de: "+hDatasets.size());
	   
	   for (Map.Entry entry : hDatasets.entrySet()) {
	       System.out.println("La clave es: \s" + entry.getKey()+"\s" + "y el valor es: \s" + entry.getValue());
	   }
         

	  /**CODIGO PARA PRUEBAS
	  
	  List<String>claveMapaGlobal = new ArrayList<String>();
	  claveMapaGlobal = manejador.getAuxUriDatasheet();
	  System.out.println("Hay un total de id`s mapa: "+claveMapaGlobal.size());
	  
	  List<String>titleMapa = new ArrayList<String>();
	  titleMapa = manejador.getTitleAux();
	  System.out.println("Hay un total de title en el mapa de: "+titleMapa.size());
	  
	  List<String>descriptionMapa = new ArrayList<String>();
	  descriptionMapa = manejador.getThemeAux();
	  System.out.println("Hay un total de description en el mapa de: "+descriptionMapa.size());
	  
	  List<String>themeMapa = new ArrayList<String>();
	  themeMapa = manejador.getThemeAux();
	  System.out.println("Hay un total de theme en el mapa de: "+themeMapa.size());
	  */
	   
	   //Por ultimo debemos generar el XML
	   //Le tengo que pasar el codigo y otros datos de la lista
	   
	 //GenerarXML genera = new GenerarXML(args[0], lista, hDatasets);
	  // System.out.println(genera.generar(lista));
	  
	   
	   try {
			
		   GenerarXML genera = new GenerarXML(args[0], lista, hDatasets);
		   //System.out.println(genera.generar(lista));
		   String contenido = genera.generar(lista);
		   File salida = new File(args[3]);
			salida.delete();
			FileWriter writer = new FileWriter(salida, true);
			writer.write(contenido);
			writer.close();	
		   
			System.exit(0);
	   } catch (Exception e) {
		// TODO: handle exception
			e.printStackTrace();
	   }
	   
		System.exit(0);
	}
	

	
	/**
	 * Muestra mensaje de los argumentos esperados por la aplicación.
	 * Deberá invocase en la fase de validación ante la detección de algún fallo
	 *
	 * @param mensaje  Mensaje adicional informativo (null si no se desea)
	 */
	private static void mostrarUso(String mensaje){
		Class<? extends Object> thisClass = new Object(){}.getClass();
		
		if (mensaje != null)
			System.err.println(mensaje+"\n");
		System.err.println(
				"Uso: " + thisClass.getEnclosingClass().getCanonicalName() + " <códigoCategoría> <ficheroCatalogo> <ficheroXSDsalida> <ficheroXMLSalida>\n" +
				"donde:\n"+
				"\t códigoCategoría:\t código de la categoría de la que se desea obtener datos\n" +
				"\t ficheroCatalogo:\t path al fichero XML con el catálogo de datos\n" +
				"\t ficheroXSDsalida:\t nombre del fichero que contiene el esquema contra el que se tiene que validar el documento XML de salida\n"	+
				"\t ficheroXMLSalida:\t nombre del fichero XML de salida\n"
				);				
	}		

}
