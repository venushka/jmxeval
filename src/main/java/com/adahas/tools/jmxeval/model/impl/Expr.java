package com.adahas.tools.jmxeval.model.impl;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;
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
  private final Field var;

  /**
   * Expression string
   */
  private final Field expression;

  /**
   * Scale for output value
   */
  private final Field scale;

  /**
   * Constructs the element
   *
   * @param context Execution context
   * @param node XML node
   */
  public Expr(final Context context, final Node node) {
    super(context);

    this.var = getNodeAttr(node, "var");
    this.expression = getNodeAttr(node, "expression");
    this.scale = getNodeAttr(node, "scale");
  }

  /**
   * @see Element#process()
   */
  @Override
  public void process() throws JMXEvalException {
    final ExprEval exprEval = new ExprEval(expression.get());
    if (scale.get() != null) {
      exprEval.setScale(Integer.parseInt(scale.get()));
    }
    final Object result = exprEval.evaluate();

    // set result variable
    context.setVar(var.get(), result);

    // process child elements
    super.process();
  }

  /**
   * @see com.adahas.tools.jmxeval.model.PerfDataSupport#getVar()
   */
  @Override
  public Field getVar() {
    return var;
  }

  /**
   * @see com.adahas.tools.jmxeval.model.PerfDataSupport#getCritical()
   */
  @Override
  public Field getCritical() {
    return literalNull();
  }

  /**
   * @see com.adahas.tools.jmxeval.model.PerfDataSupport#getWarning()
   */
  @Override
  public Field getWarning() {
    return literalNull();
  }
}
