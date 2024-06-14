package piat.opendatasearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
//Practica Realizada entre Iván Pérez Meléndez y Raúl Calderón Moya
/**
 * @author Ivan Perez Melendez - 48160420F
 * @author Raúl Calderón Moya 04264712Y
 */

/**
 * Clase estática, que debe implementar la interfaz ParserCatalogo 
 * Hereda de DefaultHandler por lo que se deben sobrescribir sus métodos para procesar documentos XML 
 *
 */
public class ManejadorXML extends DefaultHandler implements ParserCatalogo {
	private String sNombreCategoria;	// Nombre de la categoría
	private String sCodigoConcepto;
	private List<String> lConcepts; 	// Lista con los uris de los elementos <concept> que pertenecen a la categoría
	private Map <String, Map<String,String>> hDatasets;	// Mapa con información de los dataset que pertenecen a la categoría
	private StringBuilder contenidoElemento;
	//concepts
	private boolean dentroConceptBuscado;
	private String tempId;
	private int categoriaActual;
	//datasets
	private boolean dentroDataset,dentroDatasets,dentroConcepts;
	private String tempIdDataset,tempTitleDataset,tempDescriptionDataset,tempThemeDataset;
	private Map<String,String> mapaInterno;
	private boolean encontrado;
	/**  
	 * @param sCodigoConcepto código de la categoría a procesar
	 * @throws SAXException, ParserConfigurationException 
	 */
	public ManejadorXML (String sCodigoConcepto) throws SAXException, ParserConfigurationException {
		// TODO
		this.sCodigoConcepto = sCodigoConcepto;
		lConcepts = new ArrayList<String>();
		dentroConceptBuscado = false;
		categoriaActual = 0;
		contenidoElemento = new StringBuilder();
		//datasets
		dentroDataset = false;
		hDatasets = new HashMap <String, Map<String,String>>();
		mapaInterno = new HashMap<String,String>();
		encontrado = false;
		dentroDatasets = false; 
		dentroConcepts = false;
		
	}

	 //===========================================================
	 // Métodos a implementar de la interfaz ParserCatalogo
	 //===========================================================

	/**
	 * <code><b>getConcepts</b></code>
	 *	Devuelve una lista con información de los <code><b>concepts</b></code> resultantes de la búsqueda. 
	 * <br> Cada uno de los elementos de la lista contiene la <code><em>URI</em></code> del <code>concept</code>
	 * 
	 * <br>Se considerarán pertinentes el <code><b>concept</b></code> cuyo código
	 *  sea igual al criterio de búsqueda y todos sus <code>concept</code> descendientes.
	 *  
	 * @return
	 * - List  con la <em>URI</em> de los concepts pertinentes.
	 * <br>
	 * - null  si no hay concepts pertinentes.
	 * 
	 */
	@Override	
	public List<String> getConcepts() {
		// TODO 
		return lConcepts;
	}

	/**
	 * <code><b>getDatasets</b></code>
	 * 
	 * @return Mapa con información de los <code>dataset</code> resultantes de la búsqueda.
	 * <br> Si no se ha realizado ninguna  búsqueda o no hay dataset pertinentes devolverá el valor <code>null</code>
	 * <br> Estructura de cada elemento del map:
	 * 		<br> . <b>key</b>: valor del atributo ID del elemento <code>dataset</code>con la cadena de la <code><em>URI</em></code>  
	 * 		<br> . <b>value</b>: Mapa con la información a extraer del <code>dataset</code>. Cada <code>key</code> tomará los valores <em>title</em>, <em>description</em> o <em>theme</em>, y <code>value</code> sus correspondientes valores.

	 * @return
	 *  - Map con información de los <code>dataset</code> resultantes de la búsqueda.
	 *  <br>
	 *  - null si no hay datasets pertinentes.  
	 */	
	@Override
	public Map<String, Map<String, String>> getDatasets() {
		// TODO 
		
		return hDatasets;
	}
	

	 //===========================================================
	 // Métodos a implementar de SAX DocumentHandler
	 //===========================================================
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		// TODO 
		
		
	}

	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		// TODO 
		
				
	}


	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		// TODO 
		contenidoElemento.setLength(0); //Vaciamos el contenido
		
		if(qName.equals("concepts")) {
			dentroConcepts = true;
		}
		if(qName.equals("datasets")) {
			dentroDatasets = true;
		}
		
		
		if(qName.equalsIgnoreCase("concept")) {
			tempId = attributes.getValue("id");
			dentroConceptBuscado = true;
			if(dentroDataset && dentroDatasets && dentroConcepts && lConcepts.contains(attributes.getValue("id"))){
				encontrado = true;
				//mapaInterno.clear();
			}
			
		}
		if(qName.equalsIgnoreCase("concept") && categoriaActual>0) {
			
			lConcepts.add(attributes.getValue("id"));
			categoriaActual++;
		}
		if(qName.equalsIgnoreCase("dataset")) {
			dentroDataset = true;
			tempIdDataset = attributes.getValue("id");
			
		}
		/* esta arriba 
		if(dentroDataset){
			//Si estoy en un concept estoy en un concepts
			if(qName.equalsIgnoreCase("concept") && lConcepts.contains(attributes.getValue("id"))) {
				encontrado = true;
				
				//mapaInterno.put("title", tempTitleDataset);
				//mapaInterno.put("description", tempDescriptionDataset);
				//mapaInterno.put("theme", tempThemeDataset);
				//hDatasets.put(tempIdDataset, mapaInterno);
				//mapaInterno.clear();
				
			}
		}
		*/
		
		
						
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		// TODO 
		if(qName.equalsIgnoreCase("code") && dentroConceptBuscado && contenidoElemento.toString().equals(sCodigoConcepto)) {
			lConcepts.add(tempId.toString());
			categoriaActual = 1;
		}
		if(qName.equals("concept") && dentroConceptBuscado && categoriaActual > 0) {
			categoriaActual--;	
			if(categoriaActual == 0) {
				dentroConceptBuscado = false;
			}	
		}
		if(dentroDataset){
			if(qName.equalsIgnoreCase("title")) {
				tempTitleDataset = contenidoElemento.toString();
				//System.out.println(tempTitleDataset);
			}else if(qName.equalsIgnoreCase("description")) {
				tempDescriptionDataset = contenidoElemento.toString();
			}else if(qName.equalsIgnoreCase("theme")) {
				tempThemeDataset = contenidoElemento.toString();

			}			
		}
		
		if(qName.equals("dataset")) {
			
			if(encontrado) {
				mapaInterno = new HashMap<>();//Cuidado con esto 
				mapaInterno.put("title", tempTitleDataset);
				mapaInterno.put("description", tempDescriptionDataset);
				mapaInterno.put("theme", tempThemeDataset);
				hDatasets.put(tempIdDataset, mapaInterno);
				
				encontrado = false;
			}
			/*			if(!encontrado) {
				mapaInterno.clear();
			}*/

			
			dentroDataset = false;
		}else if(qName.equals("concepts")) {
			dentroConcepts = false;
		}else if(qName.equals("datasets")) {
			dentroDatasets = false;
		}
		

		
		contenidoElemento.setLength(0); //Vaciamos el contenido
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		// TODO 
		//NOTA: En este método NO se debe gestionar la extracción del valor de cadena de los elementos 
		//de información a transformar.
		contenidoElemento.append(ch,start,length);
				
	}

}
