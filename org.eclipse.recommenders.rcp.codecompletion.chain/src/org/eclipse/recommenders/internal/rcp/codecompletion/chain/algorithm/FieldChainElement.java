/**
 * Copyright (c) 2010 Gary Fritz, Andreas Kaluza, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gary Fritz - initial API and implementation.
 *    Andreas Kaluza - modified implementation to use WALA
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm;

import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.ui.JavaPlugin;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.TypeReference;

/**
 * This class is part of the internal call chain. It represents a field for
 * which the completion and the field references are stored.
 */
@SuppressWarnings("restriction")
public class FieldChainElement implements IChainElement {
  private String completion;
  private final IClassHierarchy classHierarchy;
  private IClass type;
  private final Integer chainDepth;
  private boolean thisQualifier = false;
  private Integer arrayDimension = 0;
  private final List<IChainElement> prevoiusElements;
  private boolean rootElement = false;
  private boolean isPrimitive = false;
  private final IField field;

  // private final List<LinkedList<IChainElement>> proposalChains = new
  // ArrayList<LinkedList<IChainElement>>();

  public FieldChainElement(final IField field, final Integer chainDepth) {
    this.field = field;
    prevoiusElements = new ArrayList<IChainElement>();
    this.chainDepth = chainDepth;
    try {
      completion = field.getName().toUnicodeString();
    } catch (final UTFDataFormatException e) {
      completion = field.getName().toString();
      JavaPlugin.log(e);
    }
    TypeReference fieldReference = field.getFieldTypeReference();
    classHierarchy = field.getClassHierarchy();
    if (fieldReference.isPrimitiveType()) {
      type = ChainCompletionContext.boxPrimitive(fieldReference.getName().toString());
      arrayDimension = 0;
      setPrimitive(true);
    } else {
      if (fieldReference.isArrayType() && fieldReference.getInnermostElementType().isPrimitiveType()) {
        type = ChainCompletionContext.boxPrimitive(fieldReference.getInnermostElementType().getName().toString());
      } else {
        type = classHierarchy.lookupClass(fieldReference);
      }
      arrayDimension = fieldReference.getDimensionality();
    }
  }

  @Override
  public ChainElementType getElementType() {
    return ChainElementType.FIELD;
  }

  @Override
  public String getCompletion() {
    return completion;
  }

  @Override
  public IClass getType() {
    return type;
  }

  @Override
  public Integer getChainDepth() {
    return chainDepth;
  }

  public boolean hasThisQualifier() {
    return thisQualifier;
  }

  public void setThisQualifier(boolean thisQualifier) {
    this.thisQualifier = thisQualifier;
  }

  @Override
  public Integer getArrayDimension() {
    return arrayDimension;
  }

  @Override
  public void addPrevoiusElement(IChainElement prevoius) {
    prevoiusElements.add(prevoius);

  }

  @Override
  public List<IChainElement> previousElements() {
    return prevoiusElements;
  }

  @Override
  public void setRootElement(boolean rootElement) {
    this.rootElement = rootElement;
  }

  @Override
  public boolean isRootElement() {
    return rootElement;
  }

  @Override
  public boolean isPrimitive() {
    return isPrimitive;

  }

  @Override
  public void setPrimitive(boolean isPrimitive) {
    this.isPrimitive = isPrimitive;

  }

  @Override
  public boolean isStatic() {
    return field.isStatic();
  }

  // @Override
  // public List<LinkedList<IChainElement>> constructProposalChains(int
  // currentChainLength) {
  // if (proposalChains.isEmpty()) {
  // System.out.println(getCompletion());
  // List<LinkedList<IChainElement>> descendingChains = new
  // ArrayList<LinkedList<IChainElement>>();
  // if (currentChainLength <= Constants.AlgorithmSettings.MAX_CHAIN_DEPTH) {
  // for (IChainElement element : previousElements()) {
  // if (element.getCompletion() != this.getCompletion()) {
  // descendingChains.addAll(element.constructProposalChains(currentChainLength
  // + 1));
  // }
  // }
  // }
  //
  // if (!this.isStatic()) {
  // List<LinkedList<IChainElement>> temp = new
  // ArrayList<LinkedList<IChainElement>>();
  // for (LinkedList<IChainElement> descendingElement : descendingChains) {
  // IChainElement firstElement = descendingElement.getFirst();
  // if (!(firstElement.getChainDepth() <= this.getChainDepth())
  // || currentChainLength == Constants.AlgorithmSettings.MIN_CHAIN_DEPTH &&
  // !firstElement.isRootElement()
  // || firstElement.isPrimitive() || descendingElement.contains(this)) {
  // continue;
  // }
  // LinkedList<IChainElement> linkedList = new
  // LinkedList<IChainElement>(descendingElement);
  // linkedList.addLast(this);
  // temp.add(linkedList);
  // }
  // descendingChains = temp;
  // }
  //
  // if (descendingChains.isEmpty() && this.isRootElement()) {
  // LinkedList<IChainElement> list = new LinkedList<IChainElement>();
  // list.add(this);
  // descendingChains.add(list);
  // }
  // proposalChains = descendingChains;
  // return proposalChains;
  // }
  // return proposalChains;
  // }
}
