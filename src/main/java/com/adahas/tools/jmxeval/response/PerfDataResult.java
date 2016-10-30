package com.adahas.tools.jmxeval.response;

/**
 * Performance data result
 */
public class PerfDataResult {

  /**
   * Label
   */
  private final String label;

  /**
   * Value
   */
  private final Object value;

  /**
   * Critical value/level
   */
  private final String critical;

  /**
   * Warning value/level
   */
  private final String warning;

  /**
   * Minimum value
   */
  private final String min;

  /**
   * Maximum value
   */
  private final String max;

  /**
   * Unit of measurement
   */
  private final String unit;

  /**
   * Constructs the result
   *
   * @param label Label
   * @param value Value
   * @param unit Unit of measurement
   * @param warning Warning level/value
   * @param critical Critical level/value
   * @param min Minimum value
   * @param max Maximum value
   */
  public PerfDataResult(final String label, final Object value, final String unit, final String warning,
      final String critical, final String min, final String max) {
    this.label = label;
    this.value = value;
    this.unit = unit;
    this.warning = warning;
    this.critical = critical;
    this.min = min;
    this.max = max;
  }

  /**
   * Converts the response to output string
   *
   * Format: 'label'=value[UOM];[warn];[crit];[min];[max]
   */
  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder(label);

    if (label.contains(" ")) {
      builder.insert(0, "'");
      builder.append("'");
    }

    builder.append("=");
    final String strValue = String.valueOf(value);

    if ("true".equalsIgnoreCase(strValue)) {
      builder.append("1");
    } else if ("false".equalsIgnoreCase(strValue)) {
      builder.append("0");
    } else {
      builder.append(strValue);
    }

    if (unit != null) {
      builder.append(unit);
    }

    final String[] suffixes = new String[] {
        max, min, critical, warning
    };

    final StringBuilder suffixData = new StringBuilder();
    for (String suffix : suffixes) {
      if (suffix != null || suffixData.length() > 0) {
        if (suffix != null) {
          suffixData.insert(0, suffix);
        }
        suffixData.insert(0, ";");
      }
    }
    builder.append(suffixData);

    return builder.toString();
  }
}
