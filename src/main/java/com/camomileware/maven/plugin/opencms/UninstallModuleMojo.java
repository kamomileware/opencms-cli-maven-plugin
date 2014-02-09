package com.camomileware.maven.plugin.opencms;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import com.camomileware.maven.plugin.opencms.util.OpenCmsScriptUtils;

/**
 * Generates and execute a script for deleting the module indicate by property
 * <code>manifest.module.name</code>. If no property as this exist searches for
 * the manifest properties descriptor in the working dir.
 * 
 * @author jagarcia
 */
@Mojo(name="uninstall-module", requiresProject=true)
public class UninstallModuleMojo extends ModuleBaseMojo {

	@Override
	protected void prepareExecution() throws MojoExecutionException {
		// Copy the module to the install directory
		OpenCmsScriptUtils.copyFileToModulesDir(getOpenCmsWebInfDir(), moduleFile);
	}

	protected File getScriptToExecute() throws IOException {
		return OpenCmsScriptUtils.buildUninstallScript(moduleName, openCmsUserName, openCmsUserPass);
	}
}
