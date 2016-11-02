package com.adahas.tools.jmxeval.model;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;

public class ElementWithConstructorArgNodeAndElement extends Element {

  private final Node node;
  private final Element parent;

  public ElementWithConstructorArgNodeAndElement(final Context context, final Node node, final Element parent) {
    super(context);
    this.node = node;
    this.parent = parent;
  }

  public Element getParent() {
    return parent;
  }

  public Node getNode() {
    return node;
  }
}

