package com.adahas.tools.jmxeval.model.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.EvalException;
import com.adahas.tools.jmxeval.model.Element;

/**
 * Connection element which encloses all evals performed using a single
 * JMX connection
 */
public class Connection extends Element {

  /**
   * Connection URL
   */
  private transient final String url;
  
  /**
   * Username to connect using (optional)
   */
  private transient final String username;
  
  /**
   * Password to use (required only if username is set)
   */
  private transient final String password;
  
  /**
   * Whether SSL is enabled
   */
  private transient final String ssl;
  
  /**
   * Constructs the element
   * 
   * @param node XML node
   * @param parentElement Parent element
   */
  public Connection(final Node node, final Element parentElement) {
    super(parentElement);

    url = getNodeAttribute(node, "url");
    username = getNodeAttribute(node, "username");
    password = getNodeAttribute(node, "password");
    ssl = getNodeAttribute(node, "ssl", Boolean.FALSE.toString());
  }
  
  /**
   * @see Element#process(Context)
   */
  @Override
  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.EmptyCatchBlock"})
  public void process(final Context context) throws EvalException {
    JMXConnector jmxConnector = null;
    try {
      final JMXServiceURL jmxServiceURL = new JMXServiceURL(replaceWithVars(context, url));
      final Map<String, Object> jmxEnv = new HashMap<String, Object>();
      
      if (username != null && password != null) {
        jmxEnv.put(JMXConnector.CREDENTIALS, new String[] {replaceWithVars(context,username), replaceWithVars(context,password)});
      }
      
      if (Boolean.parseBoolean(replaceWithVars(context, ssl))) {
        SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
        jmxEnv.put("com.sun.jndi.rmi.factory.socket", csf);
      }
      
      jmxConnector = JMXConnectorFactory.connect(jmxServiceURL, jmxEnv);

      // establish jmx connection
      context.setConnection(jmxConnector.getMBeanServerConnection());
      
    } catch (IOException e) {
      // do not set the connection if exception occurs, but let the process continue
      // so that the plugin output is consistent, and shows all the eval checks on it
    }
    
    try {
      // process elements
      super.process(context);
      
    } finally {
      if (jmxConnector != null) {
        try {
          jmxConnector.close();
        } catch (IOException e) {
          // ignore exceptions on connection close()
        }
      }
    }
  }
}
