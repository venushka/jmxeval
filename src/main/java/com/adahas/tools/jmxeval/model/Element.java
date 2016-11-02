package com.adahas.tools.jmxeval.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;

/**
 * AbstractElement implementation for configuration elements
 */
public class Element {

  /**
   * References to the child elements
   */
  private final List<Element> childElements = new ArrayList<>();

  /**
   * Execution context.
   */
  protected final Context context;

  /**
   * String substituter.
   */
  private final StrSubstitutor substitutor;

  /**
   * Construct with the execution context.
   *
   * @param context Execution context
   */
  protected Element(final Context context) {
    this.context = context;

    // Initialise string substituter for token replacement in variables
    this.substitutor = new StrSubstitutor(new StrLookup<String>() {
      @Override
      public String lookup(final String key) {
        final Object value = context.getVar(key);
        if (value == null) {
          return null;
        } else {
          return String.valueOf(value);
        }
      }
    });
    this.substitutor.setEnableSubstitutionInVariables(true);
  }

  /**
   * Process method to be overridden to implement different behaviour,
   * and defaults to executing child element process() methods
   *
   * @throws JMXEvalException When processing fails
   */
  public void process() throws JMXEvalException {
    for (Element element : childElements) {
      element.process();
    }
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
   * Create a Field.
   *
   * @param node Node to lookup the attribute on
   * @param attribute Attribute to look up
   * @param defaultValue Default value
   * @return the {@link Field} to get the node attribute
   */
  public Field getNodeAttr(final Node node, final String attribute, final Field defaultValue) {
    return new NodeField(node, attribute, defaultValue);
  }

  /**
   * Create a Field.
   *
   * @param node Node to lookup the attribute on
   * @param attribute Attribute to look up
   * @param defaultValue Default value
   * @return the {@link Field} to get the node attribute
   */
  public Field getNodeAttr(final Node node, final String attribute, final String defaultValue) {
    return new NodeField(node, attribute, new Literal(defaultValue));
  }

  /**
   * Create a Field.
   *
   * @param node Node to lookup the attribute on
   * @param attribute Attribute to look up
   * @param defaultValue Default value
   * @return the {@link Field} to get the node attribute
   */
  public Field getNodeAttr(final Node node, final String attribute) {
    return new NodeField(node, attribute, literal(null));
  }

  /**
   * Create a Literal.
   *
   * @param value Literal value
   * @return the {@link Literal} to get the value
   */
  public static Literal literal(final String value) {
    return new Literal(value);
  }

  /**
   * Create a Literal.
   *
   * @param value Literal value
   * @return the {@link Literal} to get the value
   */
  public static Literal literalNull() {
    return new Literal(null);
  }


  /**
   * Value provider interface for {@link Field}.
   */
  public abstract static class Field {

    /**
     * Get the field value.
     */
    public abstract String get();

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return String.valueOf(get());
    }
  }

  /**
   * Field of element where the value is calculated lazily.
   */
  public class NodeField extends Field {

    private final Node node;
    private final String attribute;
    private final Field defaultValue;

    private NodeField(final Node node, final String attribute, final Field defaultValue) {
      super();
      this.node = node;
      this.attribute = attribute;
      this.defaultValue = defaultValue;
    }

    /**
     * @see com.adahas.tools.jmxeval.model.Element.FieldValueProvider#get()
     */
    @Override
    public String get() {
      final NamedNodeMap nodeAttributes = node.getAttributes();
      final Node attributeNode = nodeAttributes.getNamedItem(attribute);

      String attributeValue = null;

      // Get the value of the attribute
      if (attributeNode != null) {
        attributeValue = attributeNode.getNodeValue();
      }

      // If the value is null, set the default
      if (attributeValue == null) {
        attributeValue = defaultValue.get();
      }

      // Replace any variables
      if (attributeValue != null) {
        attributeValue = substitutor.replace(attributeValue);
      }

      return attributeValue;
    }
  }

  /**
   * Literal value field.
   */
  public static class Literal extends Field {

    private final String value;

    private Literal(final String value) {
      super();
      this.value = value;
    }

    /**
     * @see com.adahas.tools.jmxeval.model.Element.Field#get()
     */
    @Override
    public String get() {
      return value;
    }
  }
}
