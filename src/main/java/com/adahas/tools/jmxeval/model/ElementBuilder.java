package com.adahas.tools.jmxeval.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.ConfigurationException;

/**
 * Builder for configuration elements by parsing XML
 * configuration 
 */
public class ElementBuilder {
  
  /**
   * XML element parser mapping to locate related class names for nodes
   */
  private final transient Properties mappings = new Properties();
  
  /**
   * Constructs the element builder
   * 
   * @throws ConfigurationException When a configuration error is encountered
   */
  public ElementBuilder() throws ConfigurationException {
    try {
      mappings.load(ElementBuilder.class.getResourceAsStream("/mapping.properties"));
    } catch (IOException e) {
      throw new ConfigurationException("Error reading mapping.properties", e);
    }
  }
  
  /**
   * Builds elements provided the configuration via the execution context
   * 
   * @param context Execution context
   * @return Root element
   * @throws ConfigurationException When a configuration error is encountered
   */
  public Element build(final Context context) throws ConfigurationException {
    
    final String xmlFileName = context.getConfigValue(Context.CONFIG_FILENAME);
    final File xmlFile = new File(xmlFileName);
    
    if (!xmlFile.exists() || !xmlFile.canRead()) {
      throw new ConfigurationException("Error: Can not read configuration file: " + xmlFile.getAbsolutePath());
    }
    
    final boolean validate = Boolean.valueOf(context.getConfigValue(Context.CONFIG_VALIDATE));
    
    // Read the XML document and validate
    final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);
    builderFactory.setValidating(validate);
    
    builderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

    // Schema validation
    if (validate) {
      final URL schemaURL = ElementBuilder.class.getResource("/schema/jmxeval-" + context.getConfigValue(Context.CONFIG_SCHEMA) + ".xsd");
      builderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", schemaURL.toString());
    }

    try {
      final DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      
      // set error handler to throw any exceptions out
      documentBuilder.setErrorHandler(new ConfigurationErrorHandler());
      
      final Document document = documentBuilder.parse(xmlFile);
  
      // process the configuration
      return build(document.getDocumentElement(), null);
      
    } catch (ParserConfigurationException e) {
      throw new ConfigurationException("XML Parser configuration error: " + e.getMessage(), e);
    } catch (SAXException e) {
      throw new ConfigurationException("Exception while parsing configuration file: " + e.getMessage(), e);
    } catch (IOException e) {
      throw new ConfigurationException("Error reading configuration file: " + e.getMessage(), e);
    }
  }
  
  /**
   * Builds and element from a given XML node
   * 
   * @param node XML node
   * @param parentElement Parent element
   * @return build element
   * @throws ConfigurationException When a configuration error is encountered
   */
  protected Element build(final Node node, final Element parentElement) throws ConfigurationException {
    final String className = (String) mappings.get(node.getNodeName());
    
    if (className == null) {
      throw new ConfigurationException("Can not find mapping for element: " + node.getNodeName());
    }
    
    final Element elem = createElementInstance(className, node, parentElement);
    
    // build child elements
    if (elem != null) {
      final NodeList childElementsList = node.getChildNodes();
      for (int i = 0; i < childElementsList.getLength(); i++) {
        final Node childNode = childElementsList.item(i);
        
        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
          final Element childElement = build(childNode, elem);
          elem.addChildElement(childElement);
        }
      }
    }
    
    return elem;
  }
  
  /**
   * Creates and instance of an element class
   * 
   * @param className Class name
   * @param node XML node
   * @param parentElement Parent element
   * @return Instantiated element
   * @throws ConfigurationException When a configuration error is encountered
   */
  protected Element createElementInstance(final String className,
      final Node node, final Element parentElement) throws ConfigurationException {
    
    try {
      final Constructor<?> constructor = Class.forName(className).getConstructor(Node.class, Element.class);
      return (Element) constructor.newInstance(node, parentElement);
      
    } catch (ClassNotFoundException e) {
      throw new ConfigurationException("Can not find class: " + className, e);
    } catch (NoSuchMethodException e) {
      throw new ConfigurationException("Non compaible class: " + className, e);
    } catch (InstantiationException e) {
      throw new ConfigurationException("Can not instanciate: " + className, e);
    } catch (IllegalAccessException e) {
      throw new ConfigurationException("Can access constructor on class: " + className, e);
    } catch (InvocationTargetException e) {
      throw new ConfigurationException("Can not invoke constructor on class: " + className, e);
    }
  }
}
