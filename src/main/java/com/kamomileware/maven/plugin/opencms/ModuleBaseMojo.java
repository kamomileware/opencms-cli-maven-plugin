package com.kamomileware.maven.plugin.opencms;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Base class for module cmd operations
 *
 * @author jagarcia
 */
public abstract class ModuleBaseMojo extends OpenCmsCmdBaseMojo {

	public static final String MANIFEST_MODULE_VERSION_PROPERTY 	= "manifest.module.version";
	public static final String MANIFEST_MODULE_NAME_PROPERTY 	 	= "manifest.module.name";
	public static final String MANIFEST_MODULE_NICENAME_PROPERTY 	= "manifest.module.nicename";
	public static final String DEFAULT_FILE_NAME_MAPPING = "@{artifactId}@-@{version}@.@{extension}@";
    public static final String DEFAULT_FILE_NAME_MAPPING_CLASSIFIER = "@{artifactId}@-@{version}@-@{classifier}@.@{extension}@";
    
	/**
     * Location of the module file to install.
     * Defaults to <code>target/&lt;artifactId&gt;-&lt;version&gt;.zip</code>.
     */
    @Parameter(property="module.file",defaultValue="${project.build.directory}/${project.build.finalName}.zip",required=true)
    protected File moduleFile;

    /**
     * 
     */
    @Parameter(property="manifest.module.name")
    protected String moduleName;
    
    /**
     * Directory to copy conversed to native resources into if needed
     */
    @Parameter(property="work.directory",defaultValue="${project.build.directory}/module/work",required=true)
    private File workDirectory;
	
	/**
	 * Comprueba que se cumplen las condiciones para instalar el módulo
	 */
	protected boolean checkConditions() throws MojoExecutionException {
		if (!PACKAGING_OPENCMS_MODULE.equals(getProject().getPackaging())) {
			this.getLog().info("El proyecto no es un módulo de OpenCms");
			return false;
		}
		// check if module file exist
		if (!moduleFile.exists()) {
			throw new MojoExecutionException("El fichero de módulo " + moduleFile + "no existe!");
		}
		moduleName = getModuleName();
		// check base dir for openCms
        checkOpenCmsWebInfDir();
        return true;
	}

    /**
	 * Calcula el nombre del módulo a instalar a partir de las propiedades del
	 * sistema
	 * 
	 * @return el nombre del módulo tal y como aparece en el manifiesto
	 * @throws MojoExecutionException
	 *             error durante la lectura de de las propiedades del módulo
	 */
	protected String getModuleName() throws MojoExecutionException {
		Properties projectProperties = getProject().getProperties();
		if (moduleName==null || moduleName.isEmpty()) {
			File propsFile = new File(new File( workDirectory, "manifest" ),"module.properties");
			if(!propsFile.exists() ) {
				throw new MojoExecutionException( "No module manifest descriptor found! ");
			}
			Properties moduleProps = new Properties();
			try {
				moduleProps.load(new FileInputStream(propsFile));
				if(!moduleProps.contains(MANIFEST_MODULE_NAME_PROPERTY)){
					throw new MojoExecutionException( "No module name property found ("+MANIFEST_MODULE_NAME_PROPERTY+")");
				}
				projectProperties.putAll(moduleProps);
				moduleName= moduleProps.get(MANIFEST_MODULE_NAME_PROPERTY).toString();
			} catch (Exception e) {
				throw new MojoExecutionException( "Error reading manifest descriptor", e);
			}
		}
		return moduleName;
	}
	
	public File getModuleFile() {
		return moduleFile;
	}

	public void setModuleFile(File moduleFile) {
		this.moduleFile = moduleFile;
	}
}
