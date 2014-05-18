package com.kamomileware.maven.plugin.opencms;

import com.kamomileware.maven.plugin.opencms.util.OpenCmsScriptUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.IOException;

/**
 * This mojo invokes clearCaches operation on a configured OpenCms instance via CmsShell.
 *
 * @author jagarcia
 */
@Mojo(name="clear-caches", requiresProject=false)
public class ClearCachesMojo extends OpenCmsCmdBaseMojo {


    @Override
    protected boolean checkConditions() throws MojoExecutionException {
        checkOpenCmsWebInfDir();
        return true;
    }

    @Override
    protected void prepareExecution() throws MojoExecutionException {;}

    @Override
    protected File getScriptToExecute() throws IOException {
        return OpenCmsScriptUtils.buildClearCachesScript(openCmsUserName, openCmsUserPass);
    }
}
