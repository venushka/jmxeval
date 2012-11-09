package com.adahas.tools.jmxeval.model.impl;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.EvalException;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.model.PerfDataSupport;
import com.adahas.tools.jmxeval.response.Status;

/**
 * Element to configure JMX calls
 */
public class Query extends Element implements PerfDataSupport {
  
  /**
   * Variable name
   */
  private transient final String var;
  
  /**
   * MBean object name
   */
  private transient final String objectName;
  
  /**
   * MBean attribute
   */
  private transient final String attribute;
  
  /**
   * Composite MBean attribute name (optional)
   */
  private transient final String compositeAttribute;
  
  /**
   * Constructs the element
   * 
   * @param node XML node
   * @param parentElement Parent element
   */
  public Query(final Node node, final Element parentElement) {
    super(parentElement);
    
    this.var = getNodeAttribute(node, "var");
    this.objectName = getNodeAttribute(node, "objectName");
    this.attribute = getNodeAttribute(node, "attribute");
    this.compositeAttribute = getNodeAttribute(node, "compositeAttribute");
  }
  
  /**
   * @see Element#process(Context)
   */
  @Override
  public void process(final Context context) throws EvalException {
    try {
      if (context.getConnection() == null) {
        throw new EvalException(Status.UNKNOWN, "Can not connect to server");
      }
      
      final ObjectName mbeanName = new ObjectName(objectName);
      Object attributeValue;
      
      // retrieve attribute value
      if (compositeAttribute == null) {
        final Object attributeVal = context.getConnection().getAttribute(mbeanName, attribute);
        if (attributeVal instanceof String[]) {
          attributeValue = Arrays.asList((String[]) attributeVal);
        } else {
          attributeValue = attributeVal;
        }
      } else {
        final CompositeDataSupport compositeAttributeValue = 
            (CompositeDataSupport) context.getConnection().getAttribute(mbeanName, compositeAttribute);
        attributeValue = compositeAttributeValue.get(attribute);
      }
    
      // set query result as variable
      context.setVar(var, attributeValue);
      
      // process child elements
      super.process(context);
      
    } catch (IOException e) {
      throw new EvalException(Status.UNKNOWN, "Reading attribute failed [" + attribute + "] from object [" +
          objectName + "]" + (compositeAttribute == null ? "" : " composite result [" + compositeAttribute + "]"), e);
    } catch (JMException e) {
      throw new EvalException(Status.UNKNOWN, "Can not read attribute [" + attribute + "] from object [" +
          objectName + "]" + (compositeAttribute == null ? "" : " composite result [" + compositeAttribute + "]"), e);
    } 
  }

  /**
   * @see PerfDataSupport#getVar()
   */
  public String getVar() {
    return var;
  }

  /**
   * @see PerfDataSupport#getCritical()
   */
  public String getCritical() {
    return null;
  }

  /**
   * @see PerfDataSupport#getWarning()
   */
  public String getWarning() {
    return null;
  }
}
