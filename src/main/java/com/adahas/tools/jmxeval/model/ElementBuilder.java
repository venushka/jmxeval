package com.adahas.tools.jmxeval.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;

/**
 * Builder for configuration elements by parsing XML configuration.
 */
public class ElementBuilder {

  /**
   * XML element parser mapping to locate related class names for nodes
   */
  private final Properties mappings = new Properties();

  /**
   * Builds elements provided the configuration via the execution context
   *
   * @param context Execution context
   * @return Root element
   * @throws JMXEvalException When a configuration error is encountered
   */
  public Element build(final Context context) throws JMXEvalException {
    try {
      mappings.load(ElementBuilder.class.getResourceAsStream("/mapping.properties"));
    } catch (IOException e) {
      throw new JMXEvalException("Error reading mapping.properties", e);
    }

    final String xmlFileName = context.getFilename();
    final File xmlFile = new File(xmlFileName);

    if (!xmlFile.canRead()) {
      throw new JMXEvalException("Error: Can not read configuration file: " + xmlFile.getAbsolutePath());
    }

    // Read the XML document and validate
    final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);
    builderFactory.setValidating(context.isValidate());
    builderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

    // Schema validation
    if (context.isValidate()) {
      final URL schemaURL = ElementBuilder.class.getResource("/schema/jmxeval-" + context.getSchemaVersion() + ".xsd");
      builderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", schemaURL.toString());
    }

    try {
      // create document builder and set error handler to throw any exceptions out
      final DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      documentBuilder.setErrorHandler(new ConfigurationErrorHandler());

      // process the configuration
      final Document document = documentBuilder.parse(xmlFile);
      return build(context, document.getDocumentElement(), null);

    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new JMXEvalException("Error reading configuration file: " + e.getMessage(), e);
    }
  }

  /**
   * Builds and element from a given XML node
   *
   * @param context Execution context
   * @param node XML node
   * @param parentElement Parent element
   * @return build element
   * @throws JMXEvalException When a configuration error is encountered
   */
  protected Element build(final Context context, final Node node, final Element parentElement) throws JMXEvalException {
    final String className = (String) mappings.get(node.getNodeName());

    if (className == null) {
      throw new JMXEvalException("Can not find mapping for element: " + node.getNodeName());
    }

    final Element elem = createElementInstance(className, context, node, parentElement);

    // build child elements
    final NodeList childElementsList = node.getChildNodes();
    for (int i = 0; i < childElementsList.getLength(); i++) {
      final Node childNode = childElementsList.item(i);

      if (childNode.getNodeType() == Node.ELEMENT_NODE) {
        final Element childElement = build(context, childNode, elem);
        elem.addChildElement(childElement);
      }
    }

    return elem;
  }

  /**
   * Creates and instance of an element class
   *
   * @param className Class name
   * @param context Execution context
   * @param node XML node
   * @param parentElement Parent element
   * @return Instantiated element
   * @throws JMXEvalException When a configuration error is encountered
   */
  protected Element createElementInstance(final String className, final Context context, final Node node, final Element parentElement) throws JMXEvalException {
    try {
      final Constructor<?>[] constructors = Class.forName(className).getConstructors();
      if (constructors.length != 1) {
        throw new JMXEvalException(className + " has more than one constructor, it should only have one constructor which can optionally accept the following types: "
            + Context.class.getName() + " (execution context), " + Node.class.getName() + " (XML Node for the element), " + Element.class.getName() + " (parent Element)");
      }

      final List<Object> args = new ArrayList<>();
      for (final Class<?> argClass : constructors[0].getParameterTypes()) {
        if (argClass.equals(Context.class)) {
          args.add(context);
        } else if (argClass.equals(Node.class)) {
          args.add(node);
        } else if (Element.class.equals(argClass)) {
          args.add(parentElement);
        }
      }
      return (Element) constructors[0].newInstance(args.toArray());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new JMXEvalException("Could create instance of " + className, e);
    }
  }
}
