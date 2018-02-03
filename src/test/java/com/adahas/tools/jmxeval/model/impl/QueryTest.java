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
import java.util.Arrays;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

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
 * Test for {@link Query}.
 */
@RunWith(MockitoJUnitRunner.class)
public class QueryTest {

  @Mock private Context context;
  @Mock private Node node, attrVar, attrObjectName, attrAttribute, attrCompositeAttribute, attrValueOnFailure;
  @Mock private NamedNodeMap namedNodeMap;
  @Mock private Element child1, child2;
  @Mock private MBeanServerConnection connection;
  @Mock private CompositeData compositeData;

  /**
   * Test querying simple attribute.
   */
  @Test
  public void testProcessQuerySimpleAttribute() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("var")).thenReturn(attrVar);
    when(namedNodeMap.getNamedItem("objectName")).thenReturn(attrObjectName);
    when(namedNodeMap.getNamedItem("attribute")).thenReturn(attrAttribute);

    when(attrVar.getNodeValue()).thenReturn("var1");
    when(attrObjectName.getNodeValue()).thenReturn("java.lang:type=Memory");
    when(attrAttribute.getNodeValue()).thenReturn("used");

    when(context.getConnection()).thenReturn(connection);
    when(connection.getAttribute(any(ObjectName.class), eq("used"))).thenReturn("attrVal1");

    final Query query = new Query(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    query.process();

    // then
    verify(child1).process();
    verify(child2).process();
    verify(context).setVar("var1", "attrVal1");

    // verify performance data
    assertEquals("Var", "var1", query.getVar().get());
    assertNull("Critial", query.getCritical().get());
    assertNull("Warning", query.getWarning().get());
  }

  /**
   * Test querying array attribute.
   */
  @Test
  public void testProcessQueryArrayAttribute() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("var")).thenReturn(attrVar);
    when(namedNodeMap.getNamedItem("objectName")).thenReturn(attrObjectName);
    when(namedNodeMap.getNamedItem("attribute")).thenReturn(attrAttribute);

    when(attrVar.getNodeValue()).thenReturn("var1");
    when(attrObjectName.getNodeValue()).thenReturn("java.lang:type=Memory");
    when(attrAttribute.getNodeValue()).thenReturn("used");

    when(context.getConnection()).thenReturn(connection);
    when(connection.getAttribute(any(ObjectName.class), eq("used"))).thenReturn(new String[] { "attrVal1", "attrVal2" });

    final Query query = new Query(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    query.process();

    // then
    verify(child1).process();
    verify(child2).process();
    verify(context).setVar("var1", Arrays.asList("attrVal1", "attrVal2"));
  }

  /**
   * Test querying composite attribute.
   */
  @Test
  public void testProcessQueryCompositeAttribute() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("var")).thenReturn(attrVar);
    when(namedNodeMap.getNamedItem("objectName")).thenReturn(attrObjectName);
    when(namedNodeMap.getNamedItem("attribute")).thenReturn(attrAttribute);
    when(namedNodeMap.getNamedItem("compositeAttribute")).thenReturn(attrCompositeAttribute);

    when(attrVar.getNodeValue()).thenReturn("var1");
    when(attrObjectName.getNodeValue()).thenReturn("java.lang:type=Memory");
    when(attrAttribute.getNodeValue()).thenReturn("used");
    when(attrCompositeAttribute.getNodeValue()).thenReturn("HeapMemoryUsage");

    when(context.getConnection()).thenReturn(connection);
    when(connection.getAttribute(any(ObjectName.class), eq("HeapMemoryUsage"))).thenReturn(compositeData);
    when(compositeData.get("used")).thenReturn("attrVal1");

    final Query query = new Query(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    query.process();

    // then
    verify(child1).process();
    verify(child2).process();
    verify(context).setVar("var1", "attrVal1");
  }

  /**
   * Test querying without a valid connection.
   */
  @Test
  public void testProcessWithNoConnection() throws Exception {
    // given

    final Query query = new Query(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    try {
      query.process();
      fail("Should fail as a connection is not present");
    } catch (JMXEvalException e) {
      assertEquals("Could not connect to server", e.getMessage());
    }

    // then
    verify(child1, never()).process();
    verify(child2, never()).process();
    verify(context, never()).setVar(any(String.class), any());
  }

  /**
   * Test querying without a valid connection.
   */
  @Test
  public void testProcessQueryFailure() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("objectName")).thenReturn(attrObjectName);
    when(namedNodeMap.getNamedItem("attribute")).thenReturn(attrAttribute);

    when(attrObjectName.getNodeValue()).thenReturn("java.lang:type=Memory");
    when(attrAttribute.getNodeValue()).thenReturn("used");

    when(context.getConnection()).thenReturn(connection);
    doThrow(new IOException("something went wrong")).when(connection).getAttribute(any(ObjectName.class), eq("used"));

    final Query query = new Query(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    try {
      query.process();
      fail("Should fail as a connection is not present");
    } catch (JMXEvalException e) {
      assertEquals("Failed to get [used] from [java.lang:type=Memory]", e.getMessage());
    }

    // then
    verify(child1, never()).process();
    verify(child2, never()).process();
    verify(context, never()).setVar(any(String.class), any());
  }

  /**
   * Test query failure when a valueOnFailure attribute is specified
   */
  @Test
  public void testProcessQueryFailureWithValueOnFailure() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("var")).thenReturn(attrVar);
    when(namedNodeMap.getNamedItem("objectName")).thenReturn(attrObjectName);
    when(namedNodeMap.getNamedItem("attribute")).thenReturn(attrAttribute);
    when(namedNodeMap.getNamedItem("valueOnFailure")).thenReturn(attrValueOnFailure);

    when(attrVar.getNodeValue()).thenReturn("var1");
    when(attrObjectName.getNodeValue()).thenReturn("java.lang:type=Memory");
    when(attrAttribute.getNodeValue()).thenReturn("used");
    when(attrValueOnFailure.getNodeValue()).thenReturn("FAILED");

    when(context.getConnection()).thenReturn(connection);
    doThrow(new IOException("something went wrong")).when(connection).getAttribute(any(ObjectName.class), eq("used"));

    final Query query = new Query(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    query.process();

    // then
    verify(child1, never()).process();
    verify(child2, never()).process();
    verify(context).setVar("var1", "FAILED");
  }
}

