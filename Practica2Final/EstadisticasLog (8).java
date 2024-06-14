package piat.regExp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * @author Raúl Calderón Moya 04264712Y
 *
 */
public class EstadisticasLog {

	private final static int numMaxMsg = 500;	// Nº máximo de mensajes enviados por un usuario para mostrarlo en las estadísticas
	
	// Expresiones regulares para la fecha y la hora
	private final static String FECHA = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
	private final static String HORA = "[0-9]{2}:[0-9]{2}:[0-9]{2}";
	// Expresiones regulares para el nombre del servidor. El nombre del servidor está formado por el tipo + un número. Ejemplo smtp-in3
	private final static String TIPO_SERVIDOR = "[^0-9]+";
	private final static String NUMERO_SERVIDOR = "[0-9]+";
	
	// Patrón de una traza cualquiera correcta de la que podemos extraer, en el grupo 1, el nombre del servidor
	//TODO: Modificar este patrón para que tenga más grupos y así se pueda extraer más información y no solo el nombre del servidor
	private final static String patronTraza = "^("+FECHA+")\\s+(" + HORA + ")\\s+(" + TIPO_SERVIDOR +")(" + NUMERO_SERVIDOR+")+\\s+(\\[\\w+\\]:)(.*)";	//Ya modificado
	
	/* Patrones que se usan en las estadísticas agregadas */
	
	//Estos mensajes no valen y hay que hacer nuevos mensajes(estan en la hoja de guia propia)
	//private final static String msgBLOQUEADOS = ".*SEC-BLOCKED.*"; 	// Los mensajes bloqueados son los que tienen la palabra SEC-BLOCKED en la traza	//Si pero para que se a virus debe poner INFECTED ya que tambien puede ser CLEAN
	//private final static String msgPASADOS = ".*SEC-PASSED.*";		// Los mensajes que pasan al siguiente servidor son los que tienen la palabra SEC-PASSED en la traza //pero podria ser SPAM o HAM
	//TODO: Cambiar estos patrones por los que se piden en la práctica
    private final static String msgIn = ".*smtp-in.*status=2\\.0\\.0\\s\\(accepted\\).*"; //Seria mejor poner status=2.0.0 (accepted)
	private final static String msgOut = ".*smtp-out.*delivered.*";//delivered mejor
	
	private final static String msgSPAM= ".*security-in.*SPAM.*";
	private final static String msgINFECTED = ".*security-(in|out).*INFECTED.*";
	
	private final static String Code511 = ".*5\\.1\\.1\\s.*";
    private final static String Code432 = ".*4\\.3\\.2\\s.*";
	
	

	public static void main(String[] args) throws InterruptedException, IOException {
	    Thread.currentThread().setName("Principal");
	    // Verificar que se pasa como argumento el directorio con los logs y obtenerlo
	    File directorioFuente=validarArgumentos(args);
		
		// En este array bidimensional se almacenan los nombres de los estadísticos a obtener y el patrón para que luego sea mas fácil recorrerlo 
		// y meter sus valores en el mapa hmPatronesEstadisticasAgregadas 
		final String[][] patronesEstadisticasAgregadas= {
							{"msgIn",msgIn},
							{"msgOut",msgOut},
							{"msgINFECTED", msgINFECTED},
							{"msgSPAM", msgSPAM},
							{"code 5.1.1",Code511},
							{"code 4.3.2",Code432}
						};
		//TODO: Cambiar estos patrones por los que se piden en la práctica
		
		// Mapa donde se guarda como clave el nombre de la estadística agregada, y como valor, el patrón para detectar el String de la traza que contiene ese estadístico
		final ConcurrentHashMap <String,Pattern> hmPatronesEstadisticasAgregadas = new ConcurrentHashMap <String,Pattern> ();
		
		// Guardar en el mapa hmPatronesEstadisticasAgregadas todos los patrones que se usarán para las estadísticas agregadas.
		// Los patrones y el nombre del estadístico están en el array bidimensional patronesEstadisticasAgregadas
		Pattern patron;
		String estadistico;		
		for (int i= 0; i < patronesEstadisticasAgregadas.length;i++) {
			estadistico=patronesEstadisticasAgregadas[i][0];
			patron = Pattern.compile(patronesEstadisticasAgregadas[i][1]);
			hmPatronesEstadisticasAgregadas.put(estadistico, patron);
		}		
		
		// Mapa donde recoger los contadores de cada estadísticas agregada.
		// La clave indica el servidor, la fecha y el estadístico, por lo que estará formado por una String con esta estructura: "<tipoServidor> <fecha> <estadístico>"
		// El valor es el contador de las veces que se encuentra el estadístico anterior
		final ConcurrentHashMap<String, AtomicInteger> hmEstadisticasAgregadas = new ConcurrentHashMap <String,AtomicInteger>();	

		// Mapa donde guardar el nombre del servidor y el tipo de servidor
		final ConcurrentHashMap<String, String> hmServidores = new ConcurrentHashMap <String,String>();
		
		// Mapa donde se guarda como clave el nombre de un usuario y como valor un contador, para poder guardar cuantos mensajes envían los usuarios del sistema
		final ConcurrentHashMap <String,AtomicInteger> hmUsuarios = new ConcurrentHashMap <String,AtomicInteger> ();	
		
		// Crear el patrón que se usará para analizar una traza, y del que, además de conocer si es una traza correcta, se pueden extraer ciertos valores mediante los grupos
		Pattern pTraza = Pattern.compile (patronTraza);

		/* Variables usadas para las estadísticas generales */
		int numeroFicherosProcesados;
		final AtomicInteger lineasCorrectas = new AtomicInteger(0);
		final AtomicInteger lineasIncorrectas = new AtomicInteger(0);
		
		/****** Comienzo del procesamiento ******/
		
		System.out.println ("\nLanzar los hilos trabajadores que se encargarán de procesar los ficheros.");
		// Obtener los ficheros de log del directorio
		final File[] ficheros=obtenerFicheros(directorioFuente);
		
		final int numDeNucleos = Runtime.getRuntime().availableProcessors();
		System.out.println ("Se va a crear un pool de hilos para que como máximo haya " + numDeNucleos + " hilos en ejecución simultaneamente.");
		
		// Crear un pool donde ejecutar los hilos. El pool tendrá un tamaño del nº de núcleos del ordenador
		// por lo que nunca podrá haber más hilos que ese número en ejecución simultánea.
		// Si se quiere hacer pruebas con un solo trabajador en ejecución, poner como argumento un 1. Irá mucho más lenta la ejecución porque los ficheros se procesarán secuencialmente
		final ExecutorService ejecutor = Executors.newFixedThreadPool(numDeNucleos);

	    final long tiempoComienzo = System.currentTimeMillis();
	    final AtomicInteger numTrabajadoresTerminados = new AtomicInteger(0);
	    int numTrabajadores=0;
	    numeroFicherosProcesados=0;
	    System.out.print ("Lanzando hilos al pool ");
		for (File fichero: ficheros){
			System.out.print (".");
			ejecutor.execute(new Trabajador(fichero,  pTraza,  lineasCorrectas,  lineasIncorrectas, hmServidores,hmEstadisticasAgregadas, hmPatronesEstadisticasAgregadas,hmUsuarios,numTrabajadoresTerminados));
			numeroFicherosProcesados++;
			numTrabajadores++;
			//break; // Descomentando este break, solo se ejecuta el primer trabajador
		}
		System.out.print ("\nEn total se van a ejecutar "+numTrabajadores+" trabajadores en el pool. Esperar a que terminen ");
		
		// Esperar a que terminen todos los trabajadores
		ejecutor.shutdown();	// Cerrar el ejecutor cuando termine el último trabajador
		// Cada 10 segundos mostrar cuantos trabajadores se han ejecutado y los que quedan
		while (!ejecutor.awaitTermination(10, TimeUnit.SECONDS)) {
			final int terminados=numTrabajadoresTerminados.get();
			System.out.print("\nYa han terminado "+terminados+". Esperando a los "+(numTrabajadores-terminados)+" que quedan ");
		}
		// Mostrar todos los trabajadores que se han ejecutado. Debe coincidir con los creados
		System.out.println("\nYa han terminado los "+numTrabajadoresTerminados.get()+" trabajadores");
		
		/****** Mostrar las estadÃ­sticas ******/
		/* EstadÃ­sticas generales */
		//System.out.println("\n\n  EstadÃ­sticas generales:");
		
		//Ahora aqui nos definimos el txt de salida final
		FileWriter fileW = new FileWriter("SalidaFinal.txt");
	    BufferedWriter buW = new BufferedWriter(fileW);
	    
	    buW.write("\n\n  EstadÃ­sticas generales:"+"\n");
	    
	    

		//System.out.print("\t Servidores: ");
		buW.write("\t Servidores: ");
		// Crear un mapa temporal, llamado numServidores, que se usarÃ¡ para almacenar como clave el tipo de servidor y como valor un contador de cuantos hay de ese tipo en el mapa hmServidores
		HashMap<String, Integer> numServidores = new HashMap <String,Integer>();
		// Recorrer todas las entradas de hmServidores
		for (Map.Entry<String, String> entrada : hmServidores.entrySet()) {
			// Se intenta meter la clave de hmServidores en numServidores:
			// 		si no existe se aÃ±ade con el contador 1
			// 		si ya existe se incrementa el contador en 1
			String nombreServidor = entrada.getValue();
			numServidores.put (nombreServidor, numServidores.get(nombreServidor) == null ? 1 : Integer.valueOf(numServidores.get(nombreServidor)+1));
		}
		// Mostrar por la salida estÃ¡ndar todas las entradas del mapa numServidores
		for (Map.Entry<String, Integer> entrada : numServidores.entrySet()) {
			//System.out.print(entrada.getKey()+"="+entrada.getValue()+" ");
			buW.write(entrada.getKey()+"="+entrada.getValue()+"\s");
		}
		
		//System.out.println("\n\t Numero de ficheros procesados: "+ numeroFicherosProcesados);
		buW.write("\n\t Numero de ficheros procesados: "+ numeroFicherosProcesados+"\n");
		//System.out.println("\t Numero de trazas procesadas: "+ lineasCorrectas.get()+lineasIncorrectas.get());
		buW.write("\t Numero de trazas procesadas: "+ lineasCorrectas.get()+lineasIncorrectas.get()+"\n");
		//System.out.println("\t Numero de trazas incorrectas: "+ lineasIncorrectas.get());
        buW.write("\t Numero de trazas incorrectas: "+ lineasIncorrectas.get()+"\n");
		
		/* EstadÃ­sticas agregadas */
		//System.out.println("\n\n  EstadÃ­sticas agregadas:");
		buW.write("\n\n  EstadÃ­sticas agregadas:"+"\n");
		
		// Para mostrar el contenido del mapa hmEstadisticasAgregadas de forma ordenado, se copia a un TreeMap y se muestra el contenido de este, pues un TreeMap almacena la informaciÃ³n ordenada por la clave
		Map <String, AtomicInteger> mapaOrdenado = new TreeMap<String ,AtomicInteger>(hmEstadisticasAgregadas);
		for (Map.Entry<String, AtomicInteger> entrada : mapaOrdenado.entrySet()) {
			//TODO: La siguiente instrucciÃ³n es correcta, pero se puede cambiar para que salga mejor formateada
			//System.out.println("\t"+entrada.getKey()+" = " +entrada.getValue().get());
			buW.write("\t"+entrada.getKey()+":\s\s\s" +entrada.getValue().get()+"\n");
	        }

		/* EstadÃ­sticas de usuarios */
		//System.out.println("\n  EstadÃ­sticas de usuarios que han enviado mÃ¡s de "+numMaxMsg+" mensajes:");
		buW.write("\n  EstadÃ­sticas de usuarios que han enviado mÃ¡s de "+numMaxMsg+" mensajes:"+"\n");
		// Se reutiliza el TreeMap anterior para mostrar el mapa hmUsuarios ordenado
		mapaOrdenado.clear();
		mapaOrdenado = new TreeMap<String ,AtomicInteger>(hmUsuarios);
		for (Map.Entry<String, AtomicInteger> entrada : mapaOrdenado.entrySet()) {
			if (entrada.getValue().get()>=numMaxMsg) 
				//System.out.printf("\t%-20s %5d %n", entrada.getKey()+":",entrada.getValue().get());
			    buW.write(String.format("\t%-20s %5d %n", entrada.getKey()+":",entrada.getValue().get()));
		}

		
		final long tiempoFinal = System.currentTimeMillis();
		//System.out.println("\nTiempo de ejecuciÃ³n: "+ ((long)(tiempoFinal-tiempoComienzo)/1000) + " segundos");
		buW.write("\nTiempo de ejecuciÃ³n: "+ ((long)(tiempoFinal-tiempoComienzo)/1000) + " segundos");
		System.out.println("\n\n");
		buW.write("\n\n");
		buW.close();
		
	}
	
	
	/* MÃ©todos auxiliares */
	
	/**
	 * Devuelve un array de elementos File que se corresponden con los ficheros con extensiÃ³n log encontrados en el directorio que se pasa como parÃ¡metro
	 *
	 * @param	directorioFuente	Directorio que contiene los ficheros de log
	 */
	private static File[] obtenerFicheros(File directorioFuente) {
	
		final File[] ficheros = directorioFuente.listFiles( new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".log")){
						return true;
					}
					return false;
				}
			}
		);
		return ficheros;
	}
	
 
	/**
	 * Verifica que se ha pasado un argumento con el nombre del directorio y que este existe y se puede leer. En caso contrario aborta la aplicaciÃ³n.
	 *
	 * @param	args	Argumentos a analizar
	 */
	private static File validarArgumentos(String[] args) {
		// ValidaciÃ³n de argumentos
		if (args.length != 1){
			mostrarUso("ERROR: faltan argumentos");
			System.exit(-1);
		}
	
		// VerificaciÃ³n de que el directorio existe y puede ser leÃ­do
		File directorioFuente = new File(args[0]);
		if (!directorioFuente.isDirectory() || !directorioFuente.canRead()){
			mostrarUso("ERROR: El directorio '" + args[0] + "' no existe o no puede ser leÃ­do");
			System.exit(-1);
		}
		return (directorioFuente);
	}
	
	
	/**
	 * Muestra mensaje de uso del programa.
	 *
	 * @param	mensaje	Mensaje adicional informativo en la lÃ­nea inicial (null si no se desea)
	 */
	private static void mostrarUso(String mensaje){
		Class<? extends Object> thisClass = new Object(){}.getClass();
		
		if (mensaje != null)
			System.err.println(mensaje);
		System.err.println(
				"Uso: " + thisClass.getEnclosingClass().getCanonicalName() + " <directorio fuente>\n" +
				"\t\t donde <directorio fuente> es el path al directorio donde estÃ¡n los ficheros de log\n");
	}
}
