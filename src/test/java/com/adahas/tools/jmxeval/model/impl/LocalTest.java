package com.adahas.tools.jmxeval.model.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.model.Element;

/**
 * Test for {@link Local}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalTest {

  @Mock private Context context;
  @Mock private Node node, attrPidFile;
  @Mock private NamedNodeMap namedNodeMap;
  @Mock private Element child1, child2;
  @Mock private JMXConnector jmxConnector;
  @Mock private JMXServiceURL jmxServiceURL;
  @Mock private MBeanServerConnection jmxConnection;

  /**
   * Test attach.
   */
  @Test
  public void testProcess() throws Exception {
    final File tempFile = new File(FileUtils.getTempDirectory(), LocalTest.class.getName() + ".testProcess_pidfile_" + System.currentTimeMillis());
    try (final PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
      writer.println("112233");
    }
    tempFile.deleteOnExit();

    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);
    when(namedNodeMap.getNamedItem("pidFile")).thenReturn(attrPidFile);
    when(attrPidFile.getNodeValue()).thenReturn(tempFile.getAbsolutePath());

    // spy to intercept calls to Connection.connect(...)
    final Local local = spy(new Local(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }});

    doReturn(jmxServiceURL).when(local).getURL("112233");
    doReturn(jmxConnector).when(local).connect(ArgumentMatchers.<JMXServiceURL>any());
    doReturn(jmxConnection).when(jmxConnector).getMBeanServerConnection();

    // when
    local.process();

    // then
    verify(local).connect(jmxServiceURL);
    verify(context).setConnection(jmxConnection);

    final InOrder inOrder = Mockito.inOrder(jmxConnector, child1, child2);
    inOrder.verify(child1).process();
    inOrder.verify(child2).process();
    inOrder.verify(jmxConnector).close(); // ensure the connection is close after processing children
  }

  /**
   * Test if child elements are processed even if the attach origination fails.
   */
  @Test
  public void testProcessWhenAttacheFails() throws Exception {
    final File tempFile = new File(FileUtils.getTempDirectory(), LocalTest.class.getName() + ".testProcessWhenAttacheFails_pidfile_" + System.currentTimeMillis());
    try (final PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
      writer.println("112233");
    }
    tempFile.deleteOnExit();

    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);
    when(namedNodeMap.getNamedItem("pidFile")).thenReturn(attrPidFile);
    when(attrPidFile.getNodeValue()).thenReturn(tempFile.getAbsolutePath());

    // spy to intercept calls to Connection.connect(...)
    final Local local = spy(new Local(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }});

    doReturn(jmxServiceURL).when(local).getURL("112233");
    doThrow(new IOException("something went wrong")).when(local).connect(ArgumentMatchers.<JMXServiceURL>any());

    // when
    local.process();

    // then
    verify(context, never()).setConnection(jmxConnection);
    verify(child1).process();
    verify(child2).process();
  }
}
