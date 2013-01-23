package com.adahas.tools.jmxeval.model.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.EvalException;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.model.PerfDataSupport;
import com.adahas.tools.jmxeval.response.Status;

/**
 * Element to configure JMX calls
 */
public class Exec extends Element implements PerfDataSupport {
  
  /**
   * Variable name
   */
  private transient final String var;
  
  /**
   * MBean object name
   */
  private transient final String objectName;
  
  /**
   * MBean operation
   */
  private transient final String operation;
  
  /**
   * List of arguments
   */
  private transient final List<String> arguments = new ArrayList<String>();
  
  /**
   * Constructs the element
   * 
   * @param node XML node
   * @param parentElement Parent element
   */
  public Exec(final Node node, final Element parentElement) {
    super(parentElement);
    
    this.var = getNodeAttribute(node, "var");
    this.objectName = getNodeAttribute(node, "objectName");
    this.operation = getNodeAttribute(node, "operation");
    
    for (int i = 1; i < 10; i++) {
    	arguments.add(getNodeAttribute(node, "arg" + i));
    }
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
      Object returnValue = null;
      
      final MBeanInfo mbeanInfo = context.getConnection().getMBeanInfo(mbeanName);
      final MBeanOperationInfo[] operationInfo = mbeanInfo.getOperations();
      
      String operationName = null;
      String[] argNames = null;
      Object[] argValues = null;
      
      // find the operation to execute
      for (final MBeanOperationInfo op : operationInfo) {
      	final StringBuilder opSignature = new StringBuilder();
      	opSignature.append(op.getName());
      	opSignature.append("(");
      	for (int i = 0; i < op.getSignature().length; i++) {
      	  if (i > 0) {
      	    opSignature.append(",");
      	  }
      		opSignature.append(String.valueOf(op.getSignature()[i].getType()));
      	}
      	opSignature.append(")");
      	
      	if (operation.equals(opSignature.toString())) {
      		operationName = op.getName();
      		argNames = new String[op.getSignature().length];
      		argValues = new Object[op.getSignature().length];
      		
      		for (int i = 0; i < op.getSignature().length; i++) {
      			argNames[i] = op.getSignature()[i].getType();
      			
      			if (argNames[i].equals(String.class.getName())) {
      				argValues[i] = arguments.get(i);
      			} else if (argNames[i].equals("boolean")) {
      				argValues[i] = Boolean.valueOf(arguments.get(i));
      			} else if (argNames[i].equals("byte")) {
      				argValues[i] = Byte.valueOf(arguments.get(i));
      			} else if (argNames[i].equals("short")) {
      				argValues[i] = Short.valueOf(arguments.get(i));
      			} else if (argNames[i].equals("int")) {
      				argValues[i] = Integer.valueOf(arguments.get(i));
      			} else if (argNames[i].equals("long")) {
      				argValues[i] = Long.valueOf(arguments.get(i));
      			}
        	}
      		
      		break;
      	}
      }
      
      if (operationName != null) {
      	returnValue = context.getConnection().invoke(mbeanName, operationName, argValues, argNames);
      } else {
      	throw new EvalException(Status.UNKNOWN, "Could not locate the operation: [" + operation + "]");
      }
      // set query result as variable
      context.setVar(var, returnValue);
      
      // process child elements
      super.process(context);
      
    } catch (IOException e) {
      throw new EvalException(Status.UNKNOWN, "Executing operation failed [" + operation + "] on object [" +
          objectName + "]", e);
    } catch (JMException e) {
      throw new EvalException(Status.UNKNOWN, "Executing operation failed [" + operation + "] on object [" +
          objectName + "]", e);
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
