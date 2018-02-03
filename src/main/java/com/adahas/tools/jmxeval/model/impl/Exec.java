package com.adahas.tools.jmxeval.model.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.model.PerfDataSupport;

/**
 * Element to configure JMX calls
 */
public class Exec extends Element implements PerfDataSupport {

  /**
   * Variable name
   */
  private final Field var;

  /**
   * MBean object name
   */
  private final Field objectName;

  /**
   * MBean operation
   */
  private final Field operation;

  /**
   * Composite MBean attribute name (optional)
   */
  private final Field compositeAttribute;

  /**
   * MBean value to use if JMX failure.
   */
  private final Field valueOnFailure;

  /**
   * Constructs the element
   *
   * @param context Execution context
   * @param node XML node
   */
  public Exec(final Context context, final Node node) {
    super(context);

    this.var = getNodeAttr(node, "var");
    this.objectName = getNodeAttr(node, "objectName");
    this.operation = getNodeAttr(node, "operation");
    this.compositeAttribute = getNodeAttr(node, "compositeAttribute");
    this.valueOnFailure = getNodeAttr(node, "valueOnFailure");
  }

  /**
   * @see Element#process()
   */
  @Override
  public void process() throws JMXEvalException {

    final List<Arg> arguments = new ArrayList<>();
    for (final Element child : childElements) {
      if (child instanceof Arg) {
        arguments.add((Arg) child);
      }
    }


    if (context.getConnection() == null) {
      throw new JMXEvalException("Could not connect to server");
    }

    try {
      final ObjectName mbeanName = new ObjectName(objectName.get());

      // prepare arguments
      final String operationName = operation.get();
      final String[] argTypes = new String[arguments.size()];
      final Object[] argValues = new Object[arguments.size()];

      for (int i = 0; i < arguments.size(); i++) {
        final Arg arg = arguments.get(i);
        argTypes[i] = arg.getType().get();
        argValues[i] = getValue(arg.getType().get(), arg.getValue().get());
      }

      // invoke the method
      final Object returnValue = context.getConnection().invoke(mbeanName, operationName, argValues, argTypes);

      // set execution result as variable
      if (compositeAttribute.get() == null) {
        context.setVar(var.get(), returnValue);
      } else if (returnValue instanceof CompositeData) {
        final CompositeData compositeData = (CompositeData) returnValue;
        context.setVar(var.get(), compositeData.get(compositeAttribute.get()));
      } else {
        throw new JMXEvalException("Unable to get composite attribute");
      }

      // process child elements
      super.process();

    } catch (IOException | JMException | JMXEvalException e) {
      if (valueOnFailure.get() == null) {
        throw new JMXEvalException("Executing operation failed [" + operation + "] on object [" + objectName + "]", e);
      }

      context.setVar(var.get(), valueOnFailure.get());
    }
  }

  /**
   * Convert argument values to the appropriate data type.
   *
   * @param argType Type of the argument
   * @param argValue String value of the argument
   * @return argument value in the specified type
   * @throws JMXEvalException if the type is not supported
   */
  private Object getValue(final String argType, final String argValue) throws JMXEvalException { // NOSONAR Each data type needs to be mapped, and this is the simplest way of mapping
    if (argValue == null) {
      return null;
    } else if ("java.lang.String".equals(argType)) {
      return argValue;
    } else if ("boolean".equals(argType)) {
      return Boolean.valueOf(argValue);
    } else if ("char".equals(argType)) {
      return argValue.charAt(0);
    } else if ("byte".equals(argType)) {
      return Byte.valueOf(argValue);
    } else if ("short".equals(argType)) {
      return Short.valueOf(argValue);
    } else if ("int".equals(argType)) {
      return Integer.valueOf(argValue);
    } else if ("long".equals(argType)) {
      return Long.valueOf(argValue);
    } else if ("float".equals(argType)) {
      return Float.valueOf(argValue);
    } else if ("double".equals(argType)) {
      return Double.valueOf(argValue);
    } else {
      throw new JMXEvalException("Unsupported argument type: " + argType);
    }
  }

  /**
   * @see PerfDataSupport#getVar()
   */
  @Override
  public Field getVar() {
    return var;
  }

  /**
   * @see PerfDataSupport#getCritical()
   */
  @Override
  public Field getCritical() {
    return literalNull();
  }

  /**
   * @see PerfDataSupport#getWarning()
   */
  @Override
  public Field getWarning() {
    return literalNull();
  }
}
