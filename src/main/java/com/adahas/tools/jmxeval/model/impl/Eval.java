package com.adahas.tools.jmxeval.model.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.EvalException;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.response.EvalResult;
import com.adahas.tools.jmxeval.response.Status;

/**
 * Element to enclose all operations related to a single JMX eval
 */
public class Eval extends Element {

  /**
   * Name of the eval
   */
  private transient final String name;
  
  /**
   * Host name pattern to match before evaluating
   */
  private transient final String host;
  
  /**
   * Constructs the element
   * 
   * @param node XML node
   * @param parentElement Parent element
   */
  public Eval(final Node node, final Element parentElement) {
    super(parentElement);

    this.name = getNodeAttribute(node, "name");
    this.host = getNodeAttribute(node, "host", ".*");
  }

  /**
   * Get the name of the eval
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Get the host for the eval
   * 
   * @return the host
   */
  public String getHost() {
    return host;
  }
  
  /**
   * @see Element#process(Context)
   */
  @Override
  public void process(final Context context) throws EvalException {
    try {
      final String hostname = InetAddress.getLocalHost().getHostName();
      
      if (Pattern.matches(host, hostname)) {
        // process child elements
        super.process(context);
      }
      
    } catch (EvalException e) {
      context.getResponse().addEvalResult(new EvalResult(name, e.getStatus(), e.getMessage()));
      
    } catch (RuntimeException e) {
      context.getResponse().addEvalResult(new EvalResult(
          name, Status.UNKNOWN, e.getMessage() + " [" + e.getClass().getName() + "]"));
    } catch (UnknownHostException e) {
      context.getResponse().addEvalResult(new EvalResult(
          name, Status.UNKNOWN, e.getMessage() + " [" + e.getClass().getName() + "]"));
    }
  }
}
