package com.adahas.tools.jmxeval.response;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

/**
 * Composite eval result
 */
public class Response {

  /**
   * Overall status
   */
  private transient Status status = Status.OK;
  
  /**
   * Individual check results
   */
  protected final transient List<EvalResult> evalResults = new ArrayList<EvalResult>();
  
  /**
   * Performance data
   */
  protected final transient List<PerfDataResult> perfDataValues = new ArrayList<PerfDataResult>();
  
  /**
   * Adds a single eval check result to the response
   * 
   * @param evalResult
   */
  public void addEvalResult(final EvalResult evalResult) {
    
    // set response status to the worst case of eval statuses
    if (this.status.getValue() < evalResult.getStatus().getValue()) {
      this.status = evalResult.getStatus();
    }
   
    evalResults.add(evalResult);
  }
  
  /**
   * Adds a performance data element to the response
   * 
   * @param perfDataValue
   */
  public void addPerfData(final PerfDataResult perfDataValue) {
    perfDataValues.add(perfDataValue);
  }
  
  /**
   * Converts the response to output string
   */
  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder("JMXEval ");
    
    builder.append(getEvalResponseText());
    builder.append(getPerformanceDataText());
    
    return builder.toString();
  }
  
  /**
   * Converts the eval results to output format
   * 
   * @return output string
   */
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  protected String getEvalResponseText() {
    final StringBuilder builder = new StringBuilder();
    
    if (evalResults.size() > 1) {
      builder.append(status);
      builder.append(" - ");
      builder.append(getSummaryText());
      
      final String checkCountFormat = "[%" + String.valueOf(evalResults.size()).length() + "s] "; 
      final Formatter format = new Formatter(builder); 
      
      for (int i = 0; i < evalResults.size(); i++) {
        builder.append(System.getProperty("line.separator"));
        format.format(checkCountFormat, i + 1);
        builder.append(evalResults.get(i));
      }
      
      format.close();
    } else if (evalResults.size() == 1) {
      
      builder.append(evalResults.get(0));
    }
    
    return builder.toString();
  }
  
  /**
   * Get the summary test
   * 
   * @return output text
   */
  protected String getSummaryText() {
    final StringBuilder builder = new StringBuilder();
    
    builder.append(evalResults.size() + " checks");
    
    for (Status status : Status.values()) {
      final List<String> nameList = getEvalNamesWithStatus(status);
      if (!nameList.isEmpty()) {
        builder.append(", ");
        builder.append(nameList.size());
        builder.append(" ");
        builder.append(status.toString().toLowerCase());
        
        if (!status.equals(Status.OK)) {
          builder.append(" ");
          builder.append(nameList);
        }
      }
    }
    
    return builder.toString();
  }
  
  /**
   * Converts performance data to output format
   * 
   * @return output text
   */
  protected String getPerformanceDataText() {
    final StringBuilder builder = new StringBuilder();
    
    if (!perfDataValues.isEmpty()) {
      builder.append(" |");
      for (PerfDataResult perfData : perfDataValues) {
        builder.append(" ");
        builder.append(perfData);
      }
    }
    
    return builder.toString();
  }
  
  /**
   * Gets the names of the evals having a status as specified
   * 
   * @param status Status to search for
   * @return Matching eval names
   */
  protected List<String> getEvalNamesWithStatus(final Status status) {
    final List<String> labelList = new ArrayList<String>();
    for (EvalResult evalResult : evalResults) {
      if (evalResult.getStatus().equals(status)) {
        labelList.add(evalResult.getName());
      }
    }
    return labelList;
  }
  
  /**
   * Get response status
   * 
   * @return status
   */
  public Status getStatus() {
    return status;
  }
}
