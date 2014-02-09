package com.camomileware.maven.plugin.opencms;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.camomileware.maven.plugin.opencms.util.OpenCmsScriptUtils;

/**
 * Goal which copies the module file to the OpenCms application configured in
 * the project and then installs it. The module file is copied to
 * <code>${opencms.home}/WEB-INF/packages/modules</code> directory. The
 * installation is done via generated script by CmsShell.
 */
@Mojo(name="install-module", requiresProject=true)
public class InstallModuleMojo extends ModuleBaseMojo {

	/**
	 * Selects to update the module or a fresh install, for installing previous
	 * module version Defaults to <code>"true"</code>
	 */
	@Parameter(property="fresh.install", defaultValue="true") 
	private boolean freshInstall;

	@Override
	protected void prepareExecution() throws MojoExecutionException {
		// Copy the module to the install directory
		OpenCmsScriptUtils.copyFileToModulesDir(getOpenCmsWebInfDir(), moduleFile);
	}

	@Override
	protected File getScriptToExecute() throws IOException {
		return OpenCmsScriptUtils.buildInstallScript(moduleName, moduleFile, openCmsUserName, openCmsUserPass, freshInstall);
	}
}
