package com.adahas.tools.jmxeval.model.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.model.ElementBuilder;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

/**
 * Local (attach) element which encloses all evals performed using a single
 * JMX connection
 */
public class Local extends Element {

  private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

  private static final Logger log = LogManager.getLogger(Local.class);

  /**
   * PID file location.
   */
  private final Field pidFile;

  /**
   * Constructs the element
   *
   * @param context Execution context
   * @param node XML node
   */
  public Local(final Context context, final Node node) {
    super(context);

    pidFile = getNodeAttr(node, "pidFile");
  }

  /**
   * @see Element#process()
   */
  @Override
  public void process() throws JMXEvalException {
    JMXConnector jmxConnector = null;
    try (final BufferedReader reader = new BufferedReader(new FileReader(pidFile.get()))) {
      final String line = reader.readLine();

      final JMXServiceURL jmxServiceURL = getURL(line);
      jmxConnector = connect(jmxServiceURL);

      // establish jmx connection
      context.setConnection(jmxConnector.getMBeanServerConnection());

    } catch (IOException | AttachNotSupportedException | AgentLoadException | AgentInitializationException e) {
      log.warn("Exception occured while attaching to process", e);
      // do not set the connection if exception occurs, but let the process continue
      // so that the plugin output is consistent, and shows all the eval checks on it
    } finally {
      try {
        // process elements even if the connection origination fails
        super.process();
      } finally {
        IOUtils.closeQuietly(jmxConnector);
      }
    }
  }

  /**
   * Get the JMX connection URL.
   *
   * @param pid Process ID of the process to attach
   * @return JMX connection URL
   * @throws AttachNotSupportedException When attaching to other VM fails
   * @throws IOException Fails communicating with the other VM
   * @throws AgentLoadException When loading the management agent on other VM fails
   * @throws AgentInitializationException When initialising the management agent on other VM fails
   */
  JMXServiceURL getURL(final String pid) throws AttachNotSupportedException, IOException, AgentLoadException, AgentInitializationException {
    final VirtualMachine vm = VirtualMachine.attach(pid);

    String connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
    if (connectorAddress == null) {
      // load management agent if not already loaded
      vm.loadAgent(findAgentJar(vm).getAbsolutePath());
      connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
    }
    return new JMXServiceURL(connectorAddress);
  }

  /**
   * Find the management-agent.jar in the JRE.
   *
   * @param vm Virtual machine
   * @return the management-agent.jar file
   * @throws IOException when reading system properties fails from the other VM.
   */
  File findAgentJar(final VirtualMachine vm) throws IOException {
    // if java.home is a JRE
    final File agentJarInJRE = new File(vm.getSystemProperties().getProperty("java.home") + "/lib/management-agent.jar");
    if (agentJarInJRE.exists()) {
      return agentJarInJRE;
    }

    // if java.home is a JDK
    final File agentJarInJDK = new File(vm.getSystemProperties().getProperty("java.home") + "/jre/lib/management-agent.jar");
    if (agentJarInJDK.exists()) {
      return agentJarInJDK;
    }

    throw new IOException("Could not find either [" + agentJarInJRE.getAbsolutePath() + "] or [" + agentJarInJDK + "]");
  }

  /**
   * Get a {@link JMXConnector} to connect to a given URL. Using a getter method to allow testing, due to lack of a DI framework.
   *
   * @return an {@link ElementBuilder} instance
   * @throws IOException if connecting to the JMX URL fails
   */
  JMXConnector connect(final JMXServiceURL jmxServiceURL) throws IOException {
    return JMXConnectorFactory.connect(jmxServiceURL);
  }
}
