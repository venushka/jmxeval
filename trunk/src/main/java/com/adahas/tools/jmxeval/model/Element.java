package com.adahas.tools.jmxeval.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.EvalException;

/**
 * AbstractElement implementation for configuration elements
 */
public class Element {
  
  /**
   * Reference to the parent element
   */
  private transient final Element parentElement;
  
  /**
   * References to the child elements
   */
  private transient final List<Element> childElements = new ArrayList<Element>();

  /**
   * Regex to match content within square braces (variable names within strings)
   */
  private final static String REGEX_INNER_BRACES = "\\$\\{([^\\{\\}]*)\\}";
  
  /**
   * Pattern to match REGEX_INNER_BRACES regex
   */
  private final static Pattern PATTERN_INNER_BRACES = Pattern.compile(REGEX_INNER_BRACES);
  
  /**
   * Constructs the element
   * 
   * @param parentElement Parent element
   */
  protected Element(final Element parentElement) {
    this.parentElement = parentElement;
  }
  
  /**
   * Process method to be overridden to implement different behaviour,
   * and defaults to executing child element process() methods
   * 
   * @param context Execution context
   * @throws EvalException When processing fails
   */
  public void process(final Context context) throws EvalException {
    for (Element element : childElements) {
      element.process(context);
    }
  }
  
  /**
   * Get the parent element
   * 
   * @return the parentElement
   */
  protected Element getParentElement() {
    return parentElement;
  }

  /**
   * Get the child elements
   * 
   * @return the childElements
   */
  protected List<Element> getChildElements() {
    return childElements;
  }

  /**
   * Adds a child element
   * 
   * @param childElement adds a childElement
   */
  protected void addChildElement(final Element childElement) {
    this.childElements.add(childElement);
  }
  
  /**
   * Replaces variable names mentioned within square brackets within a string with
   * their actual values using the the global variables set
   * 
   * @param context Parsing context
   * @param source String to check for variable names
   * @return String with variable names replaced with values
   * @throws EvalException When the mentioned variable is not defined
   */
  protected String replaceWithVars(final Context context, final String source) throws EvalException {
    final Matcher matcher = PATTERN_INNER_BRACES.matcher(source);
    
    final StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      final Object result = context.getVar(matcher.group(1));
      matcher.appendReplacement(buffer, String.valueOf(result));
    }
    matcher.appendTail(buffer);
    
    return buffer.toString();
  }
  
  /**
   * Replaces variable names mentioned within square brackets within a string with
   * their actual values using the the global variables set
   * 
   * @param source String to check for variable names
   * @return String with variable names replaced with values
   * @throws EvalException When the mentioned variable is not defined
   */
  protected String replaceWithVars(final String source) {
    final Matcher matcher = PATTERN_INNER_BRACES.matcher(source);
    
    final StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      final String varName = matcher.group(1);
      final Object result = System.getProperty(varName);
      if (result != null) {
        matcher.appendReplacement(buffer, String.valueOf(result));
      }
    }
    matcher.appendTail(buffer);
    
    return buffer.toString();
  }

  /**
   * Get an attribute value from a node
   * 
   * @param node Node to fetch the attribute value from
   * @param attribute Attribute name
   * @param defaultValue Default value
   * @return Attribute value
   */
  protected String getNodeAttribute(final Node node, final String attribute, final String defaultValue) {
    final NamedNodeMap serverNodeAttributes = node.getAttributes();
    final Node attributeNode = serverNodeAttributes.getNamedItem(attribute);
    
    String returnValue;
    
    if (attributeNode == null) {
      returnValue = defaultValue;
    } else {
      returnValue = attributeNode.getNodeValue();
    }
    
    // Replace any variables set via system properties
    if (returnValue != null) {
      returnValue = replaceWithVars(returnValue);
    }
    
    return returnValue;
  }
  
  /**
   * Get an attribute value from a node
   * 
   * @param node Node to fetch the attribute value from
   * @param attribute Attribute name
   * @return Attribute value
   */
  protected String getNodeAttribute(final Node node, final String attribute) {
    return getNodeAttribute(node, attribute, null);
  }
}
