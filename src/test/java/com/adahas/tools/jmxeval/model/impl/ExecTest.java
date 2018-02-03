package com.adahas.tools.jmxeval.model.impl;

import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
  @Mock private Node node, attrVar, attrObjectName, attrOperation, attrCompositeAttribute, attrValueOnFailure;
  @Mock private NamedNodeMap namedNodeMap;
  @Mock private Element child1, child2;
  @Mock private MBeanServerConnection connection;
  @Mock private CompositeData compositeData;

  @Captor private ArgumentCaptor<String[]> argTypes;
  @Captor private ArgumentCaptor<Object[]> argValues;

  private Node arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10;

  @Before
  public void setUp() {
    arg1 = arg("java.lang.String", "s1");
    arg2 = arg("boolean", "true");
    arg3 = arg("char", "H");
    arg4 = arg("byte", "3");
    arg5 = arg("short", "34");
    arg6 = arg("int", "223");
    arg7 = arg("long", "3002");
    arg8 = arg("float", "5.2");
    arg9 = arg("double", "3.2");
    arg10 = arg("java.lang.String", null);
  }


  private Node arg(String type, String value) {
    final Node argNode = mock(Node.class);
    final Node argType = mock(Node.class);
    final Node argValue = mock(Node.class);
    final NamedNodeMap nodeMap = mock(NamedNodeMap.class);

    when(argNode.getAttributes()).thenReturn(nodeMap);
    when(nodeMap.getNamedItem("type")).thenReturn(argType);
    when(nodeMap.getNamedItem("value")).thenReturn(argValue);
    when(argType.getNodeValue()).thenReturn(type);
    when(argValue.getNodeValue()).thenReturn(value);

    return argNode;
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

    when(attrVar.getNodeValue()).thenReturn("var1");
    when(attrObjectName.getNodeValue()).thenReturn("java.lang:type=Threading");
    when(attrOperation.getNodeValue()).thenReturn("getThreadInfo");

    when(context.getConnection()).thenReturn(connection);
    when(connection.invoke(any(ObjectName.class), eq("getThreadInfo"), any(Object[].class), any(String[].class))).thenReturn("returnVal1");

    final Exec exec = new Exec(context, node) {{
      addChildElement(new Arg(context, arg1));
      addChildElement(new Arg(context, arg2));
      addChildElement(new Arg(context, arg3));
      addChildElement(new Arg(context, arg4));
      addChildElement(new Arg(context, arg5));
      addChildElement(new Arg(context, arg6));
      addChildElement(new Arg(context, arg7));
      addChildElement(new Arg(context, arg8));
      addChildElement(new Arg(context, arg9));
      addChildElement(new Arg(context, arg10));
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    exec.process();

    // then
    verify(connection).invoke(any(ObjectName.class), eq("getThreadInfo"), argValues.capture(), argTypes.capture());

    assertThat(argValues.getValue(), arrayContaining(
      "s1",
      Boolean.TRUE,
      Character.valueOf('H'),
      Byte.valueOf("3"),
      Short.valueOf("34"),
      Integer.valueOf(223),
      Long.valueOf(3002),
      Float.valueOf("5.2"),
      Double.valueOf("3.2"),
      null
    ));
    assertThat(argTypes.getValue(), arrayContaining(
      "java.lang.String",
      "boolean",
      "char",
      "byte",
      "short",
      "int",
      "long",
      "float",
      "double",
      "java.lang.String"
    ));

    verify(child1).process();
    verify(child2).process();
    verify(context).setVar("var1", "returnVal1");

    // verify performance data
    assertEquals("Var", "var1", exec.getVar().get());
    assertNull("Critial", exec.getCritical().get());
    assertNull("Warning", exec.getWarning().get());
  }

  /**
   * Test invoking method that returns a composite.
   */
  @Test
  public void testCompositeDataFetch() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);
    when(namedNodeMap.getNamedItem("var")).thenReturn(attrVar);
    when(namedNodeMap.getNamedItem("objectName")).thenReturn(attrObjectName);
    when(namedNodeMap.getNamedItem("operation")).thenReturn(attrOperation);
    when(namedNodeMap.getNamedItem("compositeAttribute")).thenReturn(attrCompositeAttribute);

    when(attrVar.getNodeValue()).thenReturn("var1");
    when(attrObjectName.getNodeValue()).thenReturn("java.lang:type=Threading");
    when(attrOperation.getNodeValue()).thenReturn("getThreadInfo");
    when(attrCompositeAttribute.getNodeValue()).thenReturn("threadState");

    when(context.getConnection()).thenReturn(connection);
    when(connection.invoke(any(ObjectName.class), eq("getThreadInfo"), any(Object[].class), any(String[].class))).thenReturn(compositeData);
    when(compositeData.get("threadState")).thenReturn("RUNNING");

    final Exec exec = new Exec(context, node);

    // when
    exec.process();

    // then
    verify(connection).invoke(any(ObjectName.class), eq("getThreadInfo"), argValues.capture(), argTypes.capture());

    verify(context).setVar("var1", "RUNNING");

    // verify performance data
    assertEquals("Var", "var1", exec.getVar().get());
    assertNull("Critial", exec.getCritical().get());
    assertNull("Warning", exec.getWarning().get());
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

    when(attrObjectName.getNodeValue()).thenReturn("java.util.logging:type=Logging");
    when(attrOperation.getNodeValue()).thenReturn("setLoggerLevel");

    when(context.getConnection()).thenReturn(connection);
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
      assertEquals("Executing operation failed [setLoggerLevel] on object [java.util.logging:type=Logging]", e.getMessage());
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
  public void testProcessFailedMethodInvocationWithValueOnFailure() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("var")).thenReturn(attrVar);
    when(namedNodeMap.getNamedItem("objectName")).thenReturn(attrObjectName);
    when(namedNodeMap.getNamedItem("operation")).thenReturn(attrOperation);
    when(namedNodeMap.getNamedItem("valueOnFailure")).thenReturn(attrValueOnFailure);

    when(attrVar.getNodeValue()).thenReturn("var1");
    when(attrObjectName.getNodeValue()).thenReturn("java.util.logging:type=Logging");
    when(attrOperation.getNodeValue()).thenReturn("setLoggerLevel");
    when(attrValueOnFailure.getNodeValue()).thenReturn("FATAL");

    when(context.getConnection()).thenReturn(connection);
    doThrow(new IOException("something went wrong")).when(connection).invoke(any(ObjectName.class), eq("setLoggerLevel"), any(Object[].class), any(String[].class));

    final Exec exec = new Exec(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    exec.process();

    // then
    verify(child1, never()).process();
    verify(child2, never()).process();
    verify(context).setVar("var1", "FATAL");
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

