package com.adahas.tools.jmxeval.model.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

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
   * List of arguments
   */
  private final List<Field> arguments = new ArrayList<>();

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

    for (int i = 1; i < 10; i++) {
      arguments.add(getNodeAttr(node, "arg" + i));
    }
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
      final MBeanInfo mbeanInfo = context.getConnection().getMBeanInfo(mbeanName);
      final MBeanOperationInfo[] operationInfoArr = mbeanInfo.getOperations();

      // find the operation to execute
      MBeanOperationInfo operationInfo = null;
      for (final MBeanOperationInfo op : operationInfoArr) {
        if (getSignature(op).equals(operation.get().replaceAll(" ", ""))) {
          operationInfo = op;
          break;
        }
      }

      if (operationInfo == null) {
        throw new JMXEvalException("Could not find the operation: [" + operation + "]");
      }

      // prepare arguments
      final String operationName = operationInfo.getName();
      final String[] argTypes = new String[operationInfo.getSignature().length];
      final Object[] argValues = new Object[operationInfo.getSignature().length];

      for (int i = 0; i < operationInfo.getSignature().length; i++) {
        argTypes[i] = operationInfo.getSignature()[i].getType();
        argValues[i] = getValue(argTypes[i], arguments.get(i).get());
      }

      // invoke the method
      final Object returnValue = context.getConnection().invoke(mbeanName, operationName, argValues, argTypes);

      // set query result as variable
      context.setVar(var.get(), returnValue);

      // process child elements
      super.process();

    } catch (IOException | JMException e) {
      throw new JMXEvalException("Executing operation failed [" + operation + "] on object [" + objectName + "]", e);
    }
  }

  /**
   * Get the signature of a {@link MBeanOperationInfo}.
   *
   * @param operationInfo MBean operation
   * @return the method signature
   */
  private String getSignature(final MBeanOperationInfo operationInfo) {
    final StringBuilder signature = new StringBuilder();
    signature.append(operationInfo.getName());
    signature.append("(");
    for (int i = 0; i < operationInfo.getSignature().length; i++) {
      if (i > 0) {
        signature.append(",");
      }
      signature.append(String.valueOf(operationInfo.getSignature()[i].getType()));
    }
    signature.append(")");
    return signature.toString();
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
    if ("java.lang.String".equals(argType)) {
      return argValue;
    } else if ("boolean".equals(argType)) {
      return Boolean.valueOf(argValue);
    } else if ("byte".equals(argType)) {
      return Byte.valueOf(argValue);
    } else if ("short".equals(argType)) {
      return Short.valueOf(argValue);
    } else if ("int".equals(argType)) {
      return Integer.valueOf(argValue);
    } else if ("long".equals(argType)) {
      return Long.valueOf(argValue);
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
