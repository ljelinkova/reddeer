/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.reddeer.gef.impl.editpart;

import org.eclipse.reddeer.gef.matcher.IsEditPartWithLabel;

/**
 * EditPart implementation which is looking for a given label inside the edit part.
 * 
 * @author Andrej Podhradsky (andrej.podhradsky@gmail.com)
 *
 */
public class LabeledEditPart extends AbstractEditPart {

	/**
	 * Finds an edit part with a given label.
	 * 
	 * @param label
	 *            Label
	 */
	public LabeledEditPart(String label) {
		this(label, 0);
	}

	/**
	 * Finds an edit part with a given label at the specified index.
	 * 
	 * @param label
	 *            Label
	 * @param index
	 *            Index
	 */
	public LabeledEditPart(String label, int index) {
		super(new IsEditPartWithLabel(label), index);
	}

}
