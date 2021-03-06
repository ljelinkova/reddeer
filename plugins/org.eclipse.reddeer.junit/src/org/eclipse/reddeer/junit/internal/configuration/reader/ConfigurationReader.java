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
package org.eclipse.reddeer.junit.internal.configuration.reader;

import java.io.File;
import java.util.List;

import org.eclipse.reddeer.junit.requirement.configuration.RequirementConfiguration;

/**
 * Configuration reader read a file with requirement configurations
 * 
 * @author mlabuda@redhat.com
 *
 */
public interface ConfigurationReader {

	/**
	 * Loads requirements configurations from a configuration file. Reader should read all configurations
	 * stored in a file of specific format.
	 * 
	 * @param file file with requirements configurations
	 * @return list of requirements configurations
	 */
	List<RequirementConfiguration> loadConfigurations(File file);
	
}
