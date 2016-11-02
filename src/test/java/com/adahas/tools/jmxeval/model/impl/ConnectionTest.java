package com.adahas.tools.jmxeval.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.model.Element;

/**
 * Test for {@link Connection}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTest {

  @Mock private Context context;
  @Mock private Node node, attrURL, attrUsername, attrPassword, attrSSL;
  @Mock private NamedNodeMap namedNodeMap;
  @Mock private Element child1, child2;
  @Mock private JMXConnector jmxConnector;
  @Mock private MBeanServerConnection jmxConnection;

  @Captor private ArgumentCaptor<JMXServiceURL> jmxServiceUrlCaptor;
  @Captor private ArgumentCaptor<Map<String, Object>> jmxEnvCaptor;

  /**
   * Test non secure/authenticated connection.
   */
  @Test
  public void testProcess() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);
    when(namedNodeMap.getNamedItem("url")).thenReturn(attrURL);
    when(attrURL.getNodeValue()).thenReturn("service:jmx:rmi:///jndi/rmi://localhost:1199/jmxrmi");

    // spy to intercept calls to Connection.connect(...)
    final Connection connection = spy(new Connection(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }});

    doReturn(jmxConnector).when(connection).connect(ArgumentMatchers.<JMXServiceURL>any(), ArgumentMatchers.<Map<String, Object>>any());
    doReturn(jmxConnection).when(jmxConnector).getMBeanServerConnection();

    // when
    connection.process();

    // then
    verify(connection).connect(jmxServiceUrlCaptor.capture(), jmxEnvCaptor.capture());
    verify(context).setConnection(jmxConnection);

    final InOrder inOrder = Mockito.inOrder(jmxConnector, child1, child2);
    inOrder.verify(child1).process();
    inOrder.verify(child2).process();
    inOrder.verify(jmxConnector).close(); // ensure the connection is close after processing children

    final JMXServiceURL jmxServiceUrl = jmxServiceUrlCaptor.getValue();
    assertEquals("rmi", jmxServiceUrl.getProtocol());
    assertEquals("", jmxServiceUrl.getHost());
    assertEquals(0, jmxServiceUrl.getPort());
    assertEquals("/jndi/rmi://localhost:1199/jmxrmi", jmxServiceUrl.getURLPath());

    final Map<String, Object> jmxEnv = jmxEnvCaptor.getValue();
    assertTrue("Should not have any parameters", jmxEnv.isEmpty());
  }

  /**
   * Test secure/authenticated connection.
   */
  @Test
  public void testProcessWithSecureConnection() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("url")).thenReturn(attrURL);
    when(namedNodeMap.getNamedItem("username")).thenReturn(attrUsername);
    when(namedNodeMap.getNamedItem("password")).thenReturn(attrPassword);
    when(namedNodeMap.getNamedItem("ssl")).thenReturn(attrSSL);

    when(attrURL.getNodeValue()).thenReturn("service:jmx:rmi:///jndi/rmi://localhost:1199/jmxrmi");
    when(attrUsername.getNodeValue()).thenReturn("user1");
    when(attrPassword.getNodeValue()).thenReturn("secret1");
    when(attrSSL.getNodeValue()).thenReturn("true");

    // spy to intercept calls to Connection.connect(...)
    final Connection connection = spy(new Connection(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }});

    doReturn(jmxConnector).when(connection).connect(ArgumentMatchers.<JMXServiceURL>any(), ArgumentMatchers.<Map<String, Object>>any());
    doReturn(jmxConnection).when(jmxConnector).getMBeanServerConnection();

    // when
    connection.process();

    // then
    verify(connection).connect(jmxServiceUrlCaptor.capture(), jmxEnvCaptor.capture());
    verify(context).setConnection(jmxConnection);

    final InOrder inOrder = Mockito.inOrder(jmxConnector, child1, child2);
    inOrder.verify(child1).process();
    inOrder.verify(child2).process();
    inOrder.verify(jmxConnector).close(); // ensure the connection is close after processing children

    final JMXServiceURL jmxServiceUrl = jmxServiceUrlCaptor.getValue();
    assertEquals("rmi", jmxServiceUrl.getProtocol());
    assertEquals("", jmxServiceUrl.getHost());
    assertEquals(0, jmxServiceUrl.getPort());
    assertEquals("/jndi/rmi://localhost:1199/jmxrmi", jmxServiceUrl.getURLPath());

    final Map<String, Object> jmxEnv = jmxEnvCaptor.getValue();
    final String[] authParams = (String[]) jmxEnv.get(JMXConnector.CREDENTIALS);
    assertEquals("Invalid username", "user1", authParams[0]);
    assertEquals("Invalid password", "secret1", authParams[1]);
    assertTrue("Should have SSL socket factory set", jmxEnv.get("com.sun.jndi.rmi.factory.socket") instanceof SslRMIClientSocketFactory);
  }

  /**
   * Test if child elements are processed even if the connection origination fails.
   */
  @Test
  public void testProcessWhenConnectionOriginationFails() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);
    when(namedNodeMap.getNamedItem("url")).thenReturn(attrURL);
    when(attrURL.getNodeValue()).thenReturn("service:jmx:rmi:///jndi/rmi://localhost:1199/jmxrmi");

    // spy to intercept calls to Connection.connect(...)
    final Connection connection = spy(new Connection(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }});

    doThrow(new IOException("something went wrong")).when(connection).connect(ArgumentMatchers.<JMXServiceURL>any(), ArgumentMatchers.<Map<String, Object>>any());

    // when
    connection.process();

    // then
    verify(context, never()).setConnection(jmxConnection);
    verify(child1).process();
    verify(child2).process();
  }
}
