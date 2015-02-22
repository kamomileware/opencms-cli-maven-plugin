package com.kamomileware.maven.plugin.opencms;

import com.kamomileware.maven.plugin.opencms.util.OpenCmsScriptUtils;
import com.kamomileware.maven.plugin.opencms.util.OpenCmsShellStarter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

import java.io.File;
import java.io.IOException;

/**
 * @author jagarcia
 */
public abstract class OpenCmsCmdBaseMojo extends AbstractMojo {

  public static final String PACKAGING_OPENCMS_MODULE = "opencms-module";
  /**
   * Name of the webapp aplication name for OpenCms.
   */
  @Parameter(property = "opencms.webapp.name", defaultValue = "")
  protected String openCmsWebappName;
  /**
   * Mapping for the OpenCms dispatcher servlet. Defaults to "/*".
   */
  @Parameter(property = "opencms.servlet.mapping")
  protected String openCmsServetMapping;
  /**
   * User that installs the module on the OpenCms instance.
   */
  @Parameter(property = "opencms.user.name")
  protected String openCmsUserName;
  /**
   * Credentials for the OpenCms user.
   */
  @Parameter(property = "opencms.user.pass")
  protected String openCmsUserPass;
  /**
   * Base dir for OpenCms installation. Defaults to
   * <code>${catalina.home}/webapps/ROOT"</code>.
   */
  @Parameter(property = "opencms.home", defaultValue = "${catalina.home}/webapps/ROOT", required = true)
  protected String openCmsBaseDir;
  /**
   * Base dir for servlet container installation (tomcat). Defaults to
   * <code>${catalina.base}"</code>.
   */
  @Parameter(property = "catalina.base", defaultValue = "${catalina.base}", required = true)
  protected File appServerBaseDir;
  /**
   * The maven project.
   */
  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;
  /**
   */
  @Parameter(defaultValue = "${settings}", readonly = true)
  private Settings settings;
  /**
   * Id credentials in settings
   */
  @Parameter(property = "opencms.server.id")
  private String openCmsServerAuthId;

  /**
   * Punto de entrada del mojo
   */
  public void execute() throws MojoExecutionException {
    if (!checkConditions()) {
      return;
    }
    fillOpenCmsCredentials();
    prepareExecution();
    try {
      // Generate install script
      OpenCmsScriptUtils.setLog(getLog());
      File script = getScriptToExecute();

      // Log script content
      if (getLog().isDebugEnabled()) {
        getLog().debug("Executing script: " + script);
        String content = OpenCmsScriptUtils.readFileAsStringNoException(script);
        getLog().debug(content);
      }
      // Execute the script
      OpenCmsShellStarter.setLog(getLog());
      OpenCmsShellStarter.executeOpenCmsScript(getOpenCmsWebInfDir(), appServerBaseDir, openCmsServetMapping, openCmsWebappName,
        script);
    } catch (IOException e) {
      throw new MojoExecutionException("Error generating script", e);
    }
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
