package com.kamomileware.maven.plugin.opencms;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

import com.kamomileware.maven.plugin.opencms.util.OpenCmsScriptUtils;
import com.kamomileware.maven.plugin.opencms.util.OpenCmsShellStarter;

/**
 * 
 * @author jagarcia
 * 
 */
public abstract class OpenCmsCmdBaseMojo extends AbstractMojo {

	public static final String PACKAGING_OPENCMS_MODULE = "opencms-module";

	/**
	 * The maven project.
	 */
	@Component 
	private MavenProject project;

	/**
	 */
	@Component
	private Settings settings;

	/**
	 * Name of the webapp aplication name for OpenCms.
	 */
	@Parameter(property="opencms.webapp.name", defaultValue="")
	protected String openCmsWebappName;

	/**
	 * Mapping for the OpenCms dispatcher servlet. Defaults to "/*".
	 */
	@Parameter(property="opencms.servlet.mapping")
	protected String openCmsServetMapping;

	/**
	 * Id credentials in settings
	 */
	@Parameter(property="opencms.server.id")
	private String openCmsServerAuthId;

	/**
	 * User that installs the module on the OpenCms instance.
	 */
	@Parameter(property="opencms.user.name")
	protected String openCmsUserName;

	/**
	 * Credentials for the OpenCms user.
	 */
	@Parameter(property="opencms.user.pass")
	protected String openCmsUserPass;

	/**
	 * Base dir for OpenCms installation. Defaults to
	 * <code>${catalina.home}/webapps/ROOT"</code>.
	 */
	@Parameter(property="opencms.home", defaultValue="${catalina.home}/webapps/ROOT", required=true)
	protected String openCmsBaseDir;

	/**
	 * Base dir for servlet container installation (tomcat). Defaults to
	 * <code>${catalina.base}"</code>.
	 */
	@Parameter(property="catalina.base", defaultValue="${catalina.base}", required=true)
	protected File appServerBaseDir;

	/**
	 * Punto de entrada del mojo
	 */
	public void execute() throws MojoExecutionException {
		OpenCmsScriptUtils.setLog(getLog());
		OpenCmsShellStarter.setLog(getLog());
		if (!checkConditions()) {
			return;
		}
		fillOpenCmsCredentials();
		prepareExecution();
		File script = null;
		try {
			// Generate install script
			script = getScriptToExecute();

			// Log script content
			if (getLog().isDebugEnabled()) {
				getLog().debug("Executing script: " + script);
				getLog().debug(OpenCmsScriptUtils.readFileAsStringNoException(script));
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Error generando el script de instalación", e);
		}
		// Execute the script
		OpenCmsShellStarter.executeOpenCmsScript(getOpenCmsWebInfDir(), appServerBaseDir, openCmsServetMapping, openCmsWebappName,
				getLog(), script);
	}

	abstract protected boolean checkConditions() throws MojoExecutionException;

	abstract protected void prepareExecution() throws MojoExecutionException;

	abstract protected File getScriptToExecute() throws IOException;

	/**
	 * Rellena las credenciales de usuario desde la configuración usando el
	 * servidor indicado por la propiedad openCmsServerAuthId
	 */
	protected void fillOpenCmsCredentials() {
		if (openCmsServerAuthId != null && !openCmsServerAuthId.isEmpty()) {
			Server serverAuth = getCredentialsFromServer(openCmsServerAuthId);
			if (serverAuth != null) {
				openCmsUserName = serverAuth.getUsername();
				openCmsUserPass = serverAuth.getPassword();
			}
		}
	}

	/**
	 * 
	 * @param serverId
	 * @return
	 */
	protected Server getCredentialsFromServer(String serverId) {
		Server serverSettings = getSettings() == null ? null : getSettings().getServer(serverId);
		if (serverSettings != null) {
			getLog().info("Using authentication information for server: '" + serverId + "'.");
		} else {
			getLog().warn("Server authentication entry not found for: '" + serverId + "'.");
		}
		return serverSettings;
	}

    /**
     *
     * @throws MojoExecutionException
     */
    protected void checkOpenCmsWebInfDir() throws MojoExecutionException {
        if (!getOpenCmsWebInfDir().exists()) {
            throw new MojoExecutionException("Directorio WEB-INF de OpenCms no existe " + getOpenCmsWebInfDir());
        }
    }

	// Setters & getters
	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public String getOpenCmsWebappName() {
		return openCmsWebappName;
	}

	public void setOpenCmsWebappName(String openCmsWebappName) {
		this.openCmsWebappName = openCmsWebappName;
	}

	protected File getOpenCmsWebInfDir() {
		return new File(openCmsBaseDir, "WEB-INF");
	}

}