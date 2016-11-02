package com.adahas.tools.jmxeval.model;

import com.adahas.tools.jmxeval.Context;

public class ElementWithConstructorArgElement extends Element {

  private final Element parent;

  public ElementWithConstructorArgElement(final Context context, final Element parent) {
    super(context);
    this.parent = parent;
  }

  public Element getParent() {
    return parent;
  }
}

