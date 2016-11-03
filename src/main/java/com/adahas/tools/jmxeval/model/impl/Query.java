package com.adahas.tools.jmxeval.model.impl;

import java.io.IOException;
import java.util.Arrays;

import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.model.PerfDataSupport;

/**
 * Element to configure JMX calls
 */
public class Query extends Element implements PerfDataSupport {

  /**
   * Variable name
   */
  private final Field var;

  /**
   * MBean object name
   */
  private final Field objectName;

  /**
   * MBean attribute
   */
  private final Field attribute;

  /**
   * Composite MBean attribute name (optional)
   */
  private final Field compositeAttribute;

  /**
   * Constructs the element
   *
   * @param context Execution context
   * @param node XML node
   */
  public Query(final Context context, final Node node) {
    super(context);

    this.var = getNodeAttr(node, "var");
    this.objectName = getNodeAttr(node, "objectName");
    this.attribute = getNodeAttr(node, "attribute");
    this.compositeAttribute = getNodeAttr(node, "compositeAttribute");
  }

  /**
   * @see Element#process()
   */
  @Override
  public void process() throws JMXEvalException {
    try {
      if (context.getConnection() == null) {
        throw new JMXEvalException("Could not connect to server");
      }

      final ObjectName mbeanName = new ObjectName(objectName.get());
      Object attributeValue;

      // retrieve attribute value
      if (StringUtils.isBlank(compositeAttribute.get())) {
        final Object attributeVal = context.getConnection().getAttribute(mbeanName, attribute.get());
        if (attributeVal instanceof String[]) {
          attributeValue = Arrays.asList((String[]) attributeVal);
        } else {
          attributeValue = attributeVal;
        }
      } else {
        final CompositeData compositeData = (CompositeData) context.getConnection().getAttribute(mbeanName, compositeAttribute.get());
        attributeValue = compositeData.get(attribute.get());
      }

      // set query result as variable
      context.setVar(var.get(), attributeValue);

      // process child elements
      super.process();

    } catch (IOException | JMException e) {
      throw new JMXEvalException("Failed to get [" + attribute + "] from [" + objectName + "]" + (compositeAttribute.get() == null ? "" : " in [" + compositeAttribute + "]"), e);
    }
  }

  /**
   * @see com.adahas.tools.jmxeval.model.PerfDataSupport#getVar()
   */
  @Override
  public Field getVar() {
    return var;
  }

  /**
   * @see com.adahas.tools.jmxeval.model.PerfDataSupport#getCritical()
   */
  @Override
  public Field getCritical() {
    return literalNull();
  }

  /**
   * @see com.adahas.tools.jmxeval.model.PerfDataSupport#getWarning()
   */
  @Override
  public Field getWarning() {
    return literalNull();
  }
}
