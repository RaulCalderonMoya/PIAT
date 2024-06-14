package piat.opendatasearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Clase para evaluar las expresiones XPath
 * Contiene un método estático, llamado evaluar() que se encarga de realizar las consultas XPath al fichero XML que se le pasa como parámetro 
 */
public class XPATH_Evaluador{
	
	//private static Document doc;

	/**
	 * Método que se encarga de evaluar las expresiones XPath sobre el fichero XML generado en la práctica 4
	 * @param	ficheroXML	Fichero XML a evaluar
	 * @return	Una lista con la propiedad resultante de evaluar cada expresión XPath
	 * @throws	IOException
	 * @throws	XPathExpressionException 
	 * @throws	ParserConfigurationException 
	 * @throws	SAXException 
	 */
	public static List<Propiedad> evaluar (String ficheroXML) throws IOException, XPathExpressionException , ParserConfigurationException, SAXException{
		// TODO: 
		// Realiza las 4 consultas XPath al documento XML de entrada que se indican en el enunciado en el apartado "3.2 Búsqueda de información y generación del documento de resultados."
		// Cada consulta devolverá una información que se añadirá a la colección List <Propiedad>, que es la que devuelve este método
		// Una consulta puede devolver una propiedad o varias
		List<Propiedad> listado = new ArrayList<Propiedad>();
		
		//Primero usamos DOM para poder leer el XML creado para despues
		//mediante el uso de XPath poder hacer un buen tratamiento de la 
		//informacion
		Propiedad propiedad;
		
		InputSource input = new InputSource(ficheroXML);//Abrimos el XML
		
		DocumentBuilderFactory factoryDom = DocumentBuilderFactory.newInstance();
		//factoryDom.setValidating(true);
		//factoryDom.setNamespaceAware(true);
		
		DocumentBuilder builderDom = factoryDom.newDocumentBuilder();
		Document doc = builderDom.parse(input);//Le pasamos el fichero XML en cuestion
		
		//Hasta aqui con Dom ahora pasamos a XPath
		
		XPath xPath = XPathFactory.newInstance().newXPath();
		
        String expresionXPathQuery = "//summary/query/text()";        
        //text() se usa para obtener el contenido textual de un elemento
        String queryPropiedad = (String) xPath.evaluate(expresionXPathQuery, doc, XPathConstants.STRING);
	     propiedad = new Propiedad("query", queryPropiedad);
        listado.add(propiedad);
        
        //Ahora numero de elementos<resource> hijos de <resources>
        String expresionXPathResource = "count(//resources/resource)";
        // String expresionXPathResource = "//count(//resource)";
        Double numeroResources = (Double) xPath.evaluate(expresionXPathResource, doc, XPathConstants.NUMBER);
        String res = String.valueOf(numeroResources.intValue());
        propiedad = new Propiedad("numResources", res);
        listado.add(propiedad);
        
        //Ahora eventLocation
        String expresionEventLocation= "//resources/resource/location/eventLocation//text()";//-->>Otra forma  //resource//eventLocation/text()
        NodeList events = (NodeList) xPath.evaluate (expresionEventLocation, doc, XPathConstants.NODESET );
        //List <String> auxListEventLocation= new ArrayList<>();
        
        for (int i=0; i<events.getLength(); i++){
           // Element event= (Element)events.item(i);
           // NamedNodeMap atributos = event.getAttributes(); // Obtener los atributos de un nodo <alu:nombreAsignatura>
        	propiedad = new Propiedad("eventLocation", events.item(i).getTextContent());
        	listado.add(propiedad);
        }
        
		final String xPathDatasetID="//datasets/dataset[@id=//resources/resource/@id]/@id"; //me coge todos los id de dataset que aparecen también en algún resoruce		

        NodeList resources = (NodeList) xPath.evaluate (xPathDatasetID, doc, XPathConstants.NODESET );

        for(int i= 0; i<resources.getLength(); i++) {
        	propiedad = new Propiedad("id", resources.item(i).getTextContent());
        	listado.add(propiedad);
        	
        	String sPathCountResourceId = "count(//resources/resource[@id=\""+resources.item(i).getTextContent()+"\"])";//Contar las veces que aparece ese id en resources
			propiedad = new Propiedad("num", (String) xPath.evaluate(sPathCountResourceId, doc, XPathConstants.STRING));
			listado.add(propiedad);
        	
        }
		return listado;		
	}
	/**
	 * Esta clase interna define una propiedad equivalente a "nombre":"valor" en JSON
	 */
	public static class Propiedad {
		public final String nombre;
		public final String valor;

		public Propiedad (String nombre, String valor){
			this.nombre=nombre;
			this.valor=valor;		
		}

		@Override
		public String toString() {
			return this.nombre+": "+this.valor;

		}

	} //Fin de la clase interna Propiedad

} //Fin de la clase XPATH_Evaluador
