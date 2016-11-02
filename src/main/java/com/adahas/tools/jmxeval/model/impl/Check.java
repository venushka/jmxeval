package com.adahas.tools.jmxeval.model.impl;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.model.PerfDataSupport;
import com.adahas.tools.jmxeval.response.EvalResult;
import com.adahas.tools.jmxeval.response.Status;
import com.adahas.tools.jmxeval.util.NagiosRange;

/**
 * Element to perform Nagios style checks on variables
 */
public class Check extends Element implements PerfDataSupport {

  /**
   * Supported modes for check
   */
  enum Mode {
    DEFAULT,
    REGEX
  }

  /**
   * Eval name
   */
  private final Field eval;

  /**
   * Variable name
   */
  private final Field var;

  /**
   * Critical value/level
   */
  private final Field critical;

  /**
   * Warning value/level
   */
  private final Field warning;

  /**
   * Output message template
   */
  private final Field message;

  /**
   * Critical/Warning level match mode
   */
  private final Field mode;

  /**
   * Constructs the element
   *
   * @param context Execution context
   * @param node Related XML configuration node
   * @param parentElement Parent element
   */
  public Check(final Context context, final Node node, final Element parentElement) {
    super(context);

    this.eval = ((Eval) parentElement).getName();
    this.var = getNodeAttr(node, "useVar");
    this.critical = getNodeAttr(node, "critical");
    this.warning = getNodeAttr(node, "warning");
    this.message = getNodeAttr(node, "message");
    this.mode = getNodeAttr(node, "mode", "default");
  }

  /**
   * @see Element#process()
   */
  @Override
  public void process() throws JMXEvalException {
    Status status;

    final Object valueToCheck = context.getVar(var.get());
    if (valueToCheck == null) {
      status = Status.UNKNOWN;
    } else {
      status = getStatus(valueToCheck, critical.get(), warning.get(), Mode.valueOf(mode.get().toUpperCase(Locale.ENGLISH)));
    }

    // set results to context
    context.getResponse().addEvalResult(new EvalResult(eval.get(), status, message.get()));

    // process child elements
    super.process();
  }

  /**
   * Get the status given a check result value
   *
   * @param value Value to check
   * @param criticalLevel Critical value level
   * @param warningLevel Warning value level
   * @param mode Check mode
   * @return Status of the check
   * @throws JMXEvalException if evaluation fails
   */
  protected Status getStatus(final Object value, final String criticalLevel, final String warningLevel, final Mode mode) throws JMXEvalException {
    Status resultStatus;

    if (mode.equals(Mode.REGEX)) {
      resultStatus = getStatusInRegExMode(value, criticalLevel, warningLevel);
    } else {
      resultStatus = getStatusInDefaultMode(value, criticalLevel, warningLevel);
    }

    return resultStatus;
  }

  /**
   * Get the status given a check result value based on regex mode
   *
   * @param value Value to check
   * @param criticalLevel Critical value level
   * @param warningLevel Warning value level
   * @return Status of the check
   */
  protected Status getStatusInRegExMode(final Object value, final String criticalLevel, final String warningLevel) {
    Status resultStatus = null;

    if (criticalLevel != null) {
      // critical level
      final Pattern pattern = Pattern.compile(criticalLevel);
      final Matcher matcher = pattern.matcher(value.toString());

      if (matcher.matches()) {
        resultStatus = Status.CRITICAL;
      }
    }

    if (warningLevel != null && resultStatus == null) {
      // warning level (if not critical status already set
      final Pattern pattern = Pattern.compile(warningLevel);
      final Matcher matcher = pattern.matcher(value.toString());

      if (matcher.matches()) {
        resultStatus = Status.WARNING;
      }
    }

    // return OK nothing matches
    if (resultStatus == null) {
      resultStatus = Status.OK;
    }

    return resultStatus;
  }

  /**
   * Get the status given a check result value based on default mode
   *
   * @param value Value to check
   * @param criticalLevel Critical value level
   * @param warningLevel Warning value level
   * @return Status of the check
   * @throws JMXEvalException if evaluation fails
   */
  protected Status getStatusInDefaultMode(final Object value, final String criticalLevel, final String warningLevel) throws JMXEvalException {
    Status resultStatus = Status.OK;

    // if
    // - either levels are null, try an exact match
    // - if neither are null and not a number,
    if (criticalLevel == null || warningLevel == null || !(value instanceof Number)) {

      // give critical level higher priority
      if (criticalLevel != null && criticalLevel.equals(value.toString())) {
        resultStatus = Status.CRITICAL;
      } else if (warningLevel != null && warningLevel.equals(value.toString())) {
        resultStatus = Status.WARNING;
      }

    } else {
      resultStatus = getStatusByRangeCheck(value, criticalLevel, warningLevel);
    }

    // return OK nothing matches
    return resultStatus;
  }

  /**
   * Get the status given a check result value within ranges (only for numerical values)
   *
   * @param value Value to check
   * @param criticalLevel Critical value level
   * @param warningLevel Warning value level
   * @return Status of the check
   * @throws JMXEvalException if evaluation fails
   */
  protected Status getStatusByRangeCheck(final Object value, final String criticalLevel, final String warningLevel) throws JMXEvalException {
    Status resultStatus = Status.OK;

    // range check for numerics
    final Double doubleValue = ((Number) value).doubleValue();
    final NagiosRange criticalRange = new NagiosRange(criticalLevel);
    final NagiosRange warningRange  = new NagiosRange(warningLevel);

    if (!criticalRange.isInRange(doubleValue)) {
      resultStatus = Status.CRITICAL;
    } else if (!warningRange.isInRange(doubleValue)) {
      resultStatus = Status.WARNING;
    }
    return resultStatus;
  }

  /**
   * @see PerfDataSupport#getVar()
   */
  @Override
  public Field getVar() {
    return var;
  }

  /**
   * @see PerfDataSupport#getCritical()
   */
  @Override
  public Field getCritical() {
    return critical;
  }

  /**
   * @see PerfDataSupport#getWarning()
   */
  @Override
  public Field getWarning() {
    return warning;
  }
}
