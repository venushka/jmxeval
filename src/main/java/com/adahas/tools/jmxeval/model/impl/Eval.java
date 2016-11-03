package com.adahas.tools.jmxeval.model.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.response.EvalResult;
import com.adahas.tools.jmxeval.response.Status;

/**
 * Element to enclose all operations related to a single JMX eval
 */
public class Eval extends Element {

  private static final Logger log = LogManager.getLogger(Eval.class);

  /**
   * Name of the eval
   */
  private final Field name;

  /**
   * Host name pattern to match before evaluating
   */
  private final Field host;

  /**
   * Constructs the element
   *
   * @param context Execution context
   * @param node XML node
   */
  public Eval(final Context context, final Node node) {
    super(context);

    this.name = getNodeAttr(node, "name");
    this.host = getNodeAttr(node, "host", ".*");
  }

  /**
   * Get the name of the eval
   *
   * @return the name
   */
  public Field getName() {
    return name;
  }

  /**
   * Get the host for the eval
   *
   * @return the host
   */
  public Field getHost() {
    return host;
  }

  /**
   * @see Element#process()
   */
  @Override
  public void process() throws JMXEvalException {
    try {
      final String hostname = InetAddress.getLocalHost().getHostName();

      if (Pattern.matches(host.get(), hostname)) {
        // process child elements
        super.process();
      }

    } catch (JMXEvalException | RuntimeException | UnknownHostException e) {
      log.error("Error while evaluating check", e);

      // add the evaluation failure to the response
      context.getResponse().addEvalResult(new EvalResult(name.get(), Status.UNKNOWN, e.getMessage() + " [" + e.getClass().getName() + "]"));
    }
  }
}
