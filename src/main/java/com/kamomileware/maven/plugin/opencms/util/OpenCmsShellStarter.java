package com.kamomileware.maven.plugin.opencms.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;
import org.codehaus.classworlds.NoSuchRealmException;

/**
 * 
 * @author jagarcia
 *
 */
public class OpenCmsShellStarter {
	private static final String DIR_LIB = "lib";
	private static final String REALM_CHILD_OC_NAME = "servletLib_and_OpenCms";
	private static final String REALM_PLUGIN_NAME = "plugin.opencms.maven";
	private static final String METHOD_START = "start";
	
	private static final Class<?>[] start_parameters = new Class[] { FileInputStream.class };
	private static final ClassWorld world = new ClassWorld();

	private static Log log;
	
	/**
	 * 
	 * @param openCmsWebDir
	 * @param appServerBaseDir
	 * @param openCmsServeltMapping
	 * @param openCmsWebappName
	 * @param log
	 * @param installScript
	 * @throws MojoExecutionException
	 */
	public static void executeOpenCmsScript(File openCmsWebDir, File appServerBaseDir, String openCmsServeltMapping,
			String openCmsWebappName, Log log, File installScript) throws MojoExecutionException {
		// recoge la definición de la clase shell de OpenCms
		Class<?> cmsShellClazz = getOpenCmsShellClass(openCmsWebDir, appServerBaseDir);

        FileInputStream fileInputStream=null;
        try {
            Constructor<?> cmsShellContructor = cmsShellClazz.getConstructors()[0];
            Object shell = cmsShellContructor.newInstance(openCmsWebDir.toString(), openCmsServeltMapping, openCmsWebappName, "opencms/> ", null);
            Method startShell = cmsShellClazz.getDeclaredMethod(METHOD_START, start_parameters);
            fileInputStream = new FileInputStream(installScript);
            startShell.invoke(shell, fileInputStream);
		} catch (Exception e) {
			// errores en la reflexión -- problema de versiones
			throw new MojoExecutionException("Error durante la invocación del script OpenCms", e);
		} finally {
            if(fileInputStream!=null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.warn(e);
                }
            }
        }
    }

	/**
	 * 
	 * @param openCmsWebDir
	 *            directorio base de OpenCms
	 * @param appServerBaseDir
	 *            directorio padre de la biblioteca de del servidor de
	 *            aplicaciones
	 * @return la definición de la clase buscada
	 * @throws MojoExecutionException
	 */
	static protected Class<?> getOpenCmsShellClass(File openCmsWebDir, File appServerBaseDir) throws MojoExecutionException {
		// create the classes realms for OpenCms
		getOrCreateClassRealm(REALM_PLUGIN_NAME);
		ClassRealm ocRealm = getOrCreateClassRealm(REALM_CHILD_OC_NAME, REALM_PLUGIN_NAME);
		try {
			// make the child realm the ContextClassLoader
			Thread.currentThread().setContextClassLoader(ocRealm.getClassLoader());
			// create shell class definition from the realm
			return ocRealm.loadClass("org.opencms.main.CmsShell");
		} catch (ClassNotFoundException e) {
			File appServerLibDir = new File(appServerBaseDir, DIR_LIB);
			if (!appServerLibDir.exists())
				throw new MojoExecutionException("Server library dir does not exist! Check appServerBaseDir property: " + appServerBaseDir);
	
			File opencmsLibDir = new File(openCmsWebDir, DIR_LIB);
			if (!opencmsLibDir.exists())
				throw new MojoExecutionException("OpenCms library dir does not exist! Check openCmsBaseDir property " + openCmsWebDir);
	
			// add all the jars we just downloaded to the new child realm
			if(log!=null){
				log.info("Cargando bibliotecas de servidor desde " + appServerLibDir);
			}
			loadJarInClassRealm(ocRealm, appServerLibDir);
	
			if(log!=null){
				log.info("Cargando bibliotecas de OpenCms desde " + opencmsLibDir);
			}
			loadJarInClassRealm(ocRealm, opencmsLibDir);
	
			try {
				return ocRealm.loadClass("org.opencms.main.CmsShell");
			} catch (ClassNotFoundException e1) {
				throw new MojoExecutionException("No se encuentra la clase \"org.opencms.main.CmsShell\"", e1);
			}
		}
	}

	private static void loadJarInClassRealm(ClassRealm ocRealm, File appServerLibDir) {
		for (File jar : appServerLibDir.listFiles()) {
			try {
				ocRealm.addConstituent(jar.toURI().toURL());
			} catch (MalformedURLException e) { // nunca ocurre
			}
		}
	}

	private static ClassRealm getOrCreateClassRealm(String classLoaderName) throws MojoExecutionException {
		return getOrCreateClassRealm(classLoaderName, null);
	}

	private static ClassRealm getOrCreateClassRealm(String classRealmName, String parentClassRealmName) throws MojoExecutionException {
		try {
			// lookup
			if (existsRealm(classRealmName)) {
				return world.getRealm(classRealmName);
			}
			// creation
			if (existsRealm(parentClassRealmName)) {
				return world.getRealm(parentClassRealmName).createChildRealm(classRealmName);
			} else {
				return world.newRealm(classRealmName, Thread.currentThread().getContextClassLoader());
			}
		} catch (NoSuchRealmException e) {
			throw new MojoExecutionException("Error inicializando los Classloaders ", e);
		} catch (DuplicateRealmException e1) {
			throw new MojoExecutionException("Error inicializando los Classloaders ", e1);
		}
	}

	@SuppressWarnings("unchecked")
	private static ClassRealm findRealm(String realmName) {
		for (ClassRealm realm : ((Iterable<ClassRealm>) (world.getRealms()))) {
			if (realm.getId().equals(realmName)) {
				return realm;
			}
		}
		return null;
	}

	private static boolean existsRealm(String realmName) {
		return realmName != null && findRealm(realmName) != null;
	}

	public static void setLog(Log log) {
		OpenCmsShellStarter.log = log;
	}
}
