package com.adahas.tools.jmxeval.model.impl;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.EvalException;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.model.PerfDataSupport;
import com.adahas.tools.jmxeval.util.ExprEval;

/**
 * Element to evaluate a mathematical expression
 */
public class Expr extends Element implements PerfDataSupport {
  
  /**
   * Variable name
   */
  private transient final String var;
  
  /**
   * Expression string
   */
  private transient final String expression;
  
  /**
   * Scale for output value
   */
  private transient final String scale;

  /**
   * Constructs the element
   * 
   * @param node XML node
   * @param parentElement Parent element
   */
  public Expr(final Node node, final Element parentElement) {
    super(parentElement);
    
    this.var = getNodeAttribute(node, "var");
    this.expression = getNodeAttribute(node, "expression");
    this.scale = getNodeAttribute(node, "scale");
  }
  
  /**
   * @see Element#process(Context)
   */
  @Override
  public void process(final Context context) throws EvalException {
    
    final ExprEval exprEval = new ExprEval(replaceWithVars(context, expression));
    if (scale != null) {
      exprEval.setScale(Integer.parseInt(scale));
    }
    final Object result = exprEval.evaluate();
    
    // set result variable
    context.setVar(var, result);
    
    // process child elements
    super.process(context);
  }

  /**
   * @see PerfDataSupport#getVar()
   */
  public String getVar() {
    return var;
  }

  /**
   * @see PerfDataSupport#getCritical()
   */
  public String getCritical() {
    return null;
  }

  /**
   * @see PerfDataSupport#getWarning()
   */
  public String getWarning() {
    return null;
  }
}
