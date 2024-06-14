package piat.regExp;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Raúl Calderon Moya 04264712Y
 *
 */
public class Trabajador implements Runnable {

	private final File fichero;
	private final Pattern pTraza;
	private final AtomicInteger lineasCorrectas,lineasIncorrectas;
	private final ConcurrentHashMap<String, String>  hmServidores;
	private final ConcurrentHashMap<String, AtomicInteger> hmEstadisticasAgregadas;	
	private final ConcurrentHashMap <String,Pattern> hmPatronesEstadisticasAgregadas;
	private final ConcurrentHashMap <String,AtomicInteger> hmUsuarios;	
	private final AtomicInteger numTrabajadoresTerminados;
	
	public Trabajador(	File fichero, Pattern pTraza, AtomicInteger lineasCorrectas, AtomicInteger lineasIncorrectas, 
						ConcurrentHashMap<String, String> hmServidores,ConcurrentHashMap<String, AtomicInteger> hmEstadisticasAgregadas, 
						ConcurrentHashMap <String,Pattern> hmPatronesEstadisticasAgregadas,ConcurrentHashMap <String,AtomicInteger> hmUsuarios, AtomicInteger numTrabajadoresTerminados) {
		this.fichero=fichero;
		this.pTraza=pTraza;
		this.lineasCorrectas=lineasCorrectas;
		this.lineasIncorrectas=lineasIncorrectas;
		this.hmServidores=hmServidores;
		this.hmEstadisticasAgregadas=hmEstadisticasAgregadas;
		this.hmPatronesEstadisticasAgregadas=hmPatronesEstadisticasAgregadas;
		this.hmUsuarios=hmUsuarios;
		this.numTrabajadoresTerminados=numTrabajadoresTerminados;
	}
		
	public void run (){
		Thread.currentThread().setName("Trabajador del fichero " + fichero.getName());
	    //System.out.println("["+Thread.currentThread().getName()+"] Empiezo");

		// Abrir el fichero y si no hay errores, procesar cada línea invocando al método procesarLinea()
	    try {
	    	BufferedReader brInput = new BufferedReader(new FileReader(fichero));
			try{
				String linea;
				while ( (linea = brInput.readLine())!= null){
					procesarLinea(linea);
				}
			}catch(Exception e){
				System.err.println("ERROR (leyendo el fichero): "+ e.getMessage());
			}finally{
				try {
					brInput.close();

				} catch (IOException e) {
					System.err.println("ERROR (cerrando el fichero)" + e.getMessage());
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("ERROR (fichero no encontrado): "+ e.getMessage());
		}
		
		//System.out.println("["+Thread.currentThread().getName()+"] Termino");
		System.out.print (".");
		numTrabajadoresTerminados.incrementAndGet();
	}
		
	/**
	 * Procesa una línea que contendrá una traza de un fichero de log.
	 * El procesamiento consiste en:
	 * 		- Verificar si es una traza con el formato correcto
	 * 		- Invocar a los métodos correspondientes para cada estadístico
	 *
	 * @param	sLinea	Línea a analizar
	 */
	public void procesarLinea(String sLinea){		
		Matcher matcher;
				
		if (sLinea.trim().length()>0){
			matcher = pTraza.matcher(sLinea);	// Realizar la casación de la línea con el patrón genérico de una traza
			if (matcher.matches()) {	// Verificar que la línea es correcta
				lineasCorrectas.incrementAndGet();
				estadisticasServidor(matcher);
				estadisticasAgregadas(matcher);
				estadisticasUsuarios(matcher);
			} else {
				//System.out.println("La línea "+sLinea+" no tiene el formato correcto");
				lineasIncorrectas.incrementAndGet();
			}
		}
	}	
	

	/**
	 * Este método sirve para obtener las estadísticas de servidor.
	 * Guarda en el mapa hmServidores como clave el nombre de servidor y como valor su tipo
	 *
	 * @param	matcherLinea	Objeto Matcher al que ya se le ha realizado una casación y por tanto se puede extraer información de sus grupos
	 */
	private void estadisticasServidor(Matcher matcherLinea) {
		// Extraer de matcherLinea el nombre del servidor y el tipo
		// TODO
		String nombreServidor = matcherLinea.group(3)+ matcherLinea.group(4);
		String tipoServidor = matcherLinea.group(3);
		hmServidores.put (nombreServidor, tipoServidor);	// Meter los valores obtenidos en el mapa
		
	}
	
	/**
	 * Este método sirve para obtener las estadísticas agregadas cuyos patrones están en el mapa hmPatronesEstadisticasAgregadas
	 * Para ello recorre el mapa hmPatronesEstadisticasAgregadas y en cada iteración extrae un patron que prueba con la línea que se está analizando.
	 * Si el patrón casa con la información de la línea se actualiza el mapa hmEstadisticasAgregadas
	 *
	 * @param	matcherLinea	Objeto Matcher al que ya se le ha realizado una casación y por tanto se puede extraer información de sus grupos
	 */	
	private void estadisticasAgregadas(Matcher matcherLinea) {
		// Extraer de matcherLinea los valores que se necesitan a partir de los grupos disponibles
		final String traza=matcherLinea.group(0);				// Traza completa
		final String tipoServidor =   matcherLinea.group(3);	// Tipo de servidor
		final String fecha = matcherLinea.group(1);
		
		Matcher comparador;
		String clave, estadistico;
		Pattern patron;
		final AtomicInteger contadorAnterior;
		
		// Recorrer el mapa hmPatronesEstadisticasAgregadas, que contiene los patrones a probar, para ver si cada uno de esos patrones casan con la traza a analizar
		for (Map.Entry<String,Pattern> entrada : hmPatronesEstadisticasAgregadas.entrySet()){
			estadistico=entrada.getKey();	// String con el nombre del estadístico
			patron=entrada.getValue();		// Patrón asociado al estadístico
			comparador=patron.matcher(traza);	// Aplicar el patrón a la traza a analizar
			if (comparador.matches()) {			// Si casa, entonces actualizar el mapa hmEstadisticasAgregadas
				//clave = tipoServidor + " " + estadistico;	// La clave del mapa hmEstadisticasAgregadas está formada por el tipo del servidor y el nombre del estadístico
				// TODO En la aplicación de la práctica la clave del mapa hmEstadisticasAgregadas estará formada por el tipo del servidor, la fecha de la traza y el nombre del estadístico. 
				//Por ejemplo "security-in 2020-02-21 - msgBLOCKED" 
				//Si nos fijamos en el ejemplo anterior podemos hacer la siguiente linea
				clave = tipoServidor + " " + fecha + " " + estadistico;//TERMINADO
				
				// Añadir el estadístico al mapa. Si no existe el valor del contador es 1, pero si existe, se recupera el valor y se incrementa
				contadorAnterior=hmEstadisticasAgregadas.putIfAbsent(clave, new AtomicInteger(1));
				if (contadorAnterior!=null) contadorAnterior.incrementAndGet();
				break;	// Este break sale del for, pues al ser los patrones disjuntos, en cuanto una línea cumple un patrón, no hace falta mirar otros patrones
			}
		}
	}	
	
	//.*msa.*message\\sfrom:\\s<([\\w\\.]*).*
	
	private final static String correoABuscar = ".*msa.*message\\sfrom:\\s<([\\w\\.]*).*";//Pagina 12/17 de la practica para ver la estructura y luego lo otro esta en el tema para el correo
	/**
	 * Este método sirve para obtener las estadísticas del nº de correos que ha enviado cada usuario del sistema
	 * Por cada traza que se reconoce como traza en la que se ha enviado un mensaje, se actualiza el mapa hmUsuarios
	 *
	 * @param	matcherLinea	Objeto Matcher al que ya se le ha realizado una casación y por tanto se puede extraer información de sus grupos
	 */	
	//int contadorPrueba =0 ;
	
	private void estadisticasUsuarios(Matcher matcherLinea) {
		// TODO:
		//	Ver si la traza se corresponde a una traza que indica que se ha enviado un mensaje
		//	En ese caso, guardar en el mapa hmUsuarios el nombre del usuario como clave y como valor el nº 1 si no existía esa clave, pues en el caso de que existiera hay que incrementar el valor
		 String traza;
		
		//Si nos fijamos debemos hacer una busqueda con las expresiones regulares para obtener el correo.
		//Para ello usamos la linea del tema asociada al correo pero no los correos recibidos sino los enviados
		//con lo cual es un mensaje saliente valido
		traza = matcherLinea.group(0);
		Pattern patron = Pattern.compile(correoABuscar);
		Matcher matcher = patron.matcher(traza);
		//String string = "";//string vacio
		
		//final AtomicInteger contador;
		/*		if(matcher.find()) {
			String string= matcher.group(2);
		    contador = hmUsuarios.putIfAbsent(string, new AtomicInteger(0));
		    
			if(contador!=null) {
				contador.incrementAndGet();
			}
		}
		*/
		if(matcher.matches()) {
			String nombreUsuario = matcher.group(1);
			
			final AtomicInteger contador = hmUsuarios.putIfAbsent(nombreUsuario, new AtomicInteger(1));
		    
			if (contador!=null) contador.incrementAndGet();
		}
		
		

		
		//System.out.println("He entrado al for"+ contadorPrueba);
		
		
	}	
	
	
}
