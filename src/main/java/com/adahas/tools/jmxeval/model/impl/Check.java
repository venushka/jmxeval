package com.adahas.tools.jmxeval.model.impl;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.EvalException;
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
   * Variable name
   */
  private transient final String var;
  
  /**
   * Critical value/level
   */
  private transient final String critical;
  
  /**
   * Warning value/level
   */
  private transient final String warning;
  
  /**
   * Output message template
   */
  private transient final String message;
  
  /**
   * Critical/Warning level match mode
   */
  private transient final String mode;
  
  /**
   * Constructs the element
   * 
   * @param node Related XML configuration node
   * @param parentElement Parent element
   */
  public Check(final Node node, final Element parentElement) {
    super(parentElement);

    this.var = getNodeAttribute(node, "useVar");
    this.critical = getNodeAttribute(node, "critical");
    this.warning = getNodeAttribute(node, "warning");
    this.message = getNodeAttribute(node, "message");
    this.mode = getNodeAttribute(node, "mode", "default");
  }
  
  /**
   * @see Element#process(Context)
   */
  @Override
  public void process(final Context context) throws EvalException {
    
    Status status;
    
    final Object attributeValue = context.getVar(var);
    if (attributeValue == null) {
      status = Status.UNKNOWN;
    } else {
      status = getStatus(attributeValue, critical, warning, 
          Mode.valueOf(mode.toUpperCase(Locale.ENGLISH)));
    }
    
    final String outputMessage = replaceWithVars(context, message);
    
    // set results to context
    final String evalName = ((Eval) getParentElement()).getName();
    context.getResponse().addEvalResult(new EvalResult(evalName, status, outputMessage));
    
    // process child elements
    super.process(context);
  }
  
  /**
   * Get the status given a check result value
   * 
   * @param value Value to check
   * @param criticalLevel Critical value level
   * @param warningLevel Warning value level
   * @param mode Check mode
   * @return Status of the check
   */
  protected Status getStatus(final Object value, final String criticalLevel,
      final String warningLevel, final Mode mode) {
    
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
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
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
   */
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  protected Status getStatusInDefaultMode(final Object value, final String criticalLevel, final String warningLevel) {
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
   */
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  protected Status getStatusByRangeCheck(final Object value, final String criticalLevel, final String warningLevel) {
    Status resultStatus = Status.OK;
    
    // range check for numerics
    final Double doubleValue = ((Number) value).doubleValue();
    NagiosRange critical = new NagiosRange(criticalLevel);
    NagiosRange warning  = new NagiosRange(warningLevel);
    
    if (!critical.isValueOK(doubleValue)) {
      resultStatus = Status.CRITICAL;
    } else if (!warning.isValueOK(doubleValue)) {
      resultStatus = Status.WARNING;
    }
    return resultStatus;
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
    return critical;
  }

  /**
   * @see PerfDataSupport#getWarning()
   */
  public String getWarning() {
    return warning;
  }
}
