package com.kamomileware.maven.plugin.opencms;

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

import com.kamomileware.maven.plugin.opencms.util.OpenCmsScriptUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * Goal which copies the module file to the OpenCms application configured in
 * the project and then installs it. The module file is copied to
 * <code>${opencms.home}/WEB-INF/packages/modules</code> directory. The
 * installation is done via generated script by CmsShell.
 */
@Mojo(name="redeploy", requiresProject=true)
public class RedeployModuleMojo extends ModuleBaseMojo {

  @Parameter(property = "module.touch", defaultValue = "true")
  protected boolean doTouch;

	@Override
	protected void prepareExecution() throws MojoExecutionException {
		// Copy the module to the install directory
		OpenCmsScriptUtils.copyFileToModulesDir(getOpenCmsWebInfDir(), moduleFile);
	}

	@Override
	protected File getScriptToExecute() throws IOException {
      getLog().info("Redeploying module " + moduleName + " from " + moduleFile.getAbsolutePath());
        return OpenCmsScriptUtils.buildInstallScript(moduleName, moduleFile, openCmsUserName, openCmsUserPass, true, doTouch);
	}
}
