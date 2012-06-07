package com.adahas.tools.jmxeval.model.impl;

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
   * Constructs the element
   * 
   * @param node XML node
   * @param parentElement Parent element
   */
  public Eval(final Node node, final Element parentElement) {
    super(parentElement);

    this.name = getNodeAttribute(node, "name");
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
   * @see Element#process(Context)
   */
  @Override
  public void process(final Context context) throws EvalException {
    try {
      // process child elements
      super.process(context);
      
    } catch (EvalException e) {
      context.getResponse().addEvalResult(new EvalResult(name, e.getStatus(), e.getMessage()));
      
    } catch (RuntimeException e) {
      context.getResponse().addEvalResult(new EvalResult(
          name, Status.UNKNOWN, e.getMessage() + " [" + e.getClass().getName() + "]"));
    }
  }
}
