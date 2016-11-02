package com.adahas.tools.jmxeval.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;
import com.adahas.tools.jmxeval.model.Element;

/**
 * Test for {@link Exec}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecTest {

  @Mock private Context context;
  @Mock private Node node, attrVar, attrObjectName, attrOperation, attrArg1, attrArg2, attrArg3, attrArg4, attrArg5, attrArg6;
  @Mock private NamedNodeMap namedNodeMap;
  @Mock private Element child1, child2;
  @Mock private MBeanServerConnection connection;

  @Mock private MBeanInfo mbeanInfo;
  @Mock private MBeanOperationInfo operation1, operation2, operation3;

  @Before
  public void setUp() {
    when(mbeanInfo.getOperations()).thenReturn(new MBeanOperationInfo[] {
      operation1, operation2, operation3
    });

    when(operation1.getName()).thenReturn("setConfig");
    when(operation2.getName()).thenReturn("setLoggerLevel");
    when(operation3.getName()).thenReturn("setLoggerLevel");

    when(operation1.getSignature()).thenReturn(new MBeanParameterInfo[] {
      new MBeanParameterInfo("fileArg", "java.io.File", "")
    });
    when(operation2.getSignature()).thenReturn(new MBeanParameterInfo[] {
      new MBeanParameterInfo("longArg", "long", "")
    });
    when(operation3.getSignature()).thenReturn(new MBeanParameterInfo[] {
      new MBeanParameterInfo("stringArg", "java.lang.String", ""),
      new MBeanParameterInfo("longArg", "long", ""),
      new MBeanParameterInfo("intArg", "int", ""),
      new MBeanParameterInfo("shortArg", "short", ""),
      new MBeanParameterInfo("byteArg", "byte", ""),
      new MBeanParameterInfo("booleanArg", "boolean", "")
    });
  }

  /**
   * Test invoke with all supported argument types.
   */
  @Test
  public void testProcess() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("var")).thenReturn(attrVar);
    when(namedNodeMap.getNamedItem("objectName")).thenReturn(attrObjectName);
    when(namedNodeMap.getNamedItem("operation")).thenReturn(attrOperation);
    when(namedNodeMap.getNamedItem("arg1")).thenReturn(attrArg1);
    when(namedNodeMap.getNamedItem("arg2")).thenReturn(attrArg2);
    when(namedNodeMap.getNamedItem("arg3")).thenReturn(attrArg3);
    when(namedNodeMap.getNamedItem("arg4")).thenReturn(attrArg4);
    when(namedNodeMap.getNamedItem("arg5")).thenReturn(attrArg5);
    when(namedNodeMap.getNamedItem("arg6")).thenReturn(attrArg6);

    when(attrVar.getNodeValue()).thenReturn("var1");
    when(attrObjectName.getNodeValue()).thenReturn("java.util.logging:type=Logging");
    when(attrOperation.getNodeValue()).thenReturn("setLoggerLevel(java.lang.String, long, int, short, byte, boolean)");
    when(attrArg1.getNodeValue()).thenReturn("logger1");
    when(attrArg2.getNodeValue()).thenReturn(String.valueOf(Long.MAX_VALUE));
    when(attrArg3.getNodeValue()).thenReturn(String.valueOf(Integer.MAX_VALUE));
    when(attrArg4.getNodeValue()).thenReturn(String.valueOf(Short.MAX_VALUE));
    when(attrArg5.getNodeValue()).thenReturn(String.valueOf(Byte.MAX_VALUE));
    when(attrArg6.getNodeValue()).thenReturn("true");

    when(context.getConnection()).thenReturn(connection);
    when(connection.getMBeanInfo(any(ObjectName.class))).thenReturn(mbeanInfo);
    when(connection.invoke(any(ObjectName.class), eq("setLoggerLevel"), any(Object[].class), any(String[].class))).thenReturn("returnVal1");

    final Exec exec = new Exec(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    exec.process();

    // then
    verify(child1).process();
    verify(child2).process();
    verify(context).setVar("var1", "returnVal1");

    // verify performance data
    assertEquals("Var", "var1", exec.getVar().get());
    assertNull("Critial", exec.getCritical().get());
    assertNull("Warning", exec.getWarning().get());
  }

  /**
   * Test invoke non existing method.
   */
  @Test
  public void testProcessInvokeNonExistingMethod() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("objectName")).thenReturn(attrObjectName);
    when(namedNodeMap.getNamedItem("operation")).thenReturn(attrOperation);

    when(attrObjectName.getNodeValue()).thenReturn("java.util.logging:type=Logging");
    when(attrOperation.getNodeValue()).thenReturn("nonExistingMethod(java.lang.String)");

    when(context.getConnection()).thenReturn(connection);
    when(connection.getMBeanInfo(any(ObjectName.class))).thenReturn(mbeanInfo);

    final Exec exec = new Exec(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    try {
      exec.process();
      fail("Should fail attempt to call non-existing method");
    } catch (JMXEvalException e) {
      assertEquals("Could not find the operation: [nonExistingMethod(java.lang.String)]", e.getMessage());
    }

    // then
    verify(child1, never()).process();
    verify(child2, never()).process();
    verify(context, never()).setVar("var1", "returnVal1");
  }

  /**
   * Test method invocation failure.
   */
  @Test
  public void testProcessFailedMethodInvocation() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("objectName")).thenReturn(attrObjectName);
    when(namedNodeMap.getNamedItem("operation")).thenReturn(attrOperation);
    when(namedNodeMap.getNamedItem("arg1")).thenReturn(attrArg1);

    when(attrObjectName.getNodeValue()).thenReturn("java.util.logging:type=Logging");
    when(attrOperation.getNodeValue()).thenReturn("setLoggerLevel(long)");
    when(attrArg1.getNodeValue()).thenReturn("2");

    when(context.getConnection()).thenReturn(connection);
    when(connection.getMBeanInfo(any(ObjectName.class))).thenReturn(mbeanInfo);
    doThrow(new IOException("something went wrong")).when(connection).invoke(any(ObjectName.class), eq("setLoggerLevel"), any(Object[].class), any(String[].class));

    final Exec exec = new Exec(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    try {
      exec.process();
      fail("Should fail as the remote method invocation fails");
    } catch (JMXEvalException e) {
      assertEquals("Executing operation failed [setLoggerLevel(long)] on object [java.util.logging:type=Logging]", e.getMessage());
    }

    // then
    verify(child1, never()).process();
    verify(child2, never()).process();
    verify(context, never()).setVar("var1", "returnVal1");
  }

  /**
   * Test method invocation with unsupported argument type.
   */
  @Test
  public void testProcessWithUnsuportedArgType() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("objectName")).thenReturn(attrObjectName);
    when(namedNodeMap.getNamedItem("operation")).thenReturn(attrOperation);
    when(namedNodeMap.getNamedItem("arg1")).thenReturn(attrArg1);

    when(attrObjectName.getNodeValue()).thenReturn("java.util.logging:type=Logging");
    when(attrOperation.getNodeValue()).thenReturn("setConfig(java.io.File)");
    when(attrArg1.getNodeValue()).thenReturn("2");

    when(context.getConnection()).thenReturn(connection);
    when(connection.getMBeanInfo(any(ObjectName.class))).thenReturn(mbeanInfo);

    final Exec exec = new Exec(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    try {
      exec.process();
      fail("Should fail as the argument type is not supported");
    } catch (JMXEvalException e) {
      assertEquals("Unsupported argument type: java.io.File", e.getMessage());
    }

    // then
    verify(child1, never()).process();
    verify(child2, never()).process();
    verify(context, never()).setVar("var1", "returnVal1");
  }

  /**
   * Test method invocation without a valid connection.
   */
  @Test
  public void testProcessWithNoConnection() throws Exception {
    // given
    final Exec exec = new Exec(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    try {
      exec.process();
      fail("Should fail as a connection is not present");
    } catch (JMXEvalException e) {
      assertEquals("Could not connect to server", e.getMessage());
    }

    // then
    verify(child1, never()).process();
    verify(child2, never()).process();
    verify(context, never()).setVar(any(String.class), any());
  }
}

