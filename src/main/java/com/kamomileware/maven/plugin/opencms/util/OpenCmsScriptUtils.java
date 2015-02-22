package com.kamomileware.maven.plugin.opencms.util;

import com.kamomileware.maven.plugin.opencms.ModuleDescriptor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * @author joseangel
 */
public class OpenCmsScriptUtils {

  private static final String MODULES_RELATIVE_PATH = "packages" + File.separator + "modules" + File.separator;

  private static Log log;

  /**
   * @param moduleName
   * @param username
   * @param password
   * @return
   * @throws IOException
   */
  public static File buildUninstallScript(String moduleName, String username, String password) throws IOException {
    StringBuilder sb = new StringBuilder();
    loginCommand(username, password, sb);
    deleteModuleCommand(moduleName, sb);
    return createTempFile(moduleName, sb.toString(), "uninstallModule_", ".ocsh");
  }

  /**
   * @param moduleName
   * @param moduleFile
   * @param username
   * @param password
   * @param addUninstall
   * @param doTouch
   * @return
   * @throws IOException
   */
  public static File buildInstallScript(String moduleName, File moduleFile, String username, String password, boolean addUninstall,
                                        boolean doTouch) throws IOException {
    StringBuilder sb = new StringBuilder();
    loginCommand(username, password, sb);
    if (addUninstall) {
      deleteModuleCommand(moduleName, sb);
    }
    importModuleCommand(moduleFile.getName(), sb);
    if (doTouch) {
      touchCommand(moduleName, sb);
    }
    return createTempFile(moduleName, sb.toString(), "installModule_", ".ocsh");
  }

  /**
   * @param username
   * @param password
   * @return
   */
  public static File buildClearCachesScript(String username, String password) throws IOException {
    StringBuilder sb = new StringBuilder();
    loginCommand(username, password, sb);
    clearCachesCommand(sb);
    exitCommand(sb);
    return createTempFile("clearCaches", sb.toString(), "clearCaches_", ".ocsh");
  }

  /**
   * @param modulesToInstall
   * @param username
   * @param password
   * @param addUninstall
   * @return
   * @throws IOException
   */
  public static File buildInstallScript(List<ModuleDescriptor> modulesToInstall, String username, String password, boolean addUninstall)
    throws IOException {
    return buildInstallScript(modulesToInstall, username, password, addUninstall, true);
  }

  /**
   * @param modulesToInstall
   * @param username
   * @param password
   * @param addUninstall
   * @param doTouch
   * @return
   * @throws IOException
   */
  public static File buildInstallScript(List<ModuleDescriptor> modulesToInstall, String username, String password, boolean addUninstall,
                                        boolean doTouch) throws IOException {
    StringBuilder sb = new StringBuilder();
    loginCommand(username, password, sb);
    for (ModuleDescriptor module : modulesToInstall) {
      if ((addUninstall && module.isUninstall() == null) || (module.isUninstall() != null && module.isUninstall())) {
        deleteModuleCommand(module.getModuleName(), sb);
      }
    }
    for (ModuleDescriptor module : modulesToInstall) {
      if (module.isInstall() == null || module.isInstall()) {
        importModuleCommand(module.getModuleFile().getName(), sb);
        if (doTouch) {
          touchCommand(module.getModuleName(), sb);
        }
      }
    }
    return createTempFile("", sb.toString(), "multipleModuleInstall_", ".ocsh");
  }

  private static void exitCommand(StringBuilder sb) {
    sb.append("exit\n");
  }

  private static void deleteModuleCommand(String module, StringBuilder sb) {
    sb.append("deleteModule \"").append(correctModuleName(module)).append("\"\n");
  }

  private static Object correctModuleName(String module) {
    return module.replace('-', '_');
  }

  private static void importModuleCommand(String module, StringBuilder sb) {
    sb.append("importModuleFromDefault ").append(module).append("\n");
  }

  private static void loginCommand(String username, String password, StringBuilder sb) {
    sb.append("login ").append(username).append(" ").append(password).append("\n");
  }

  private static void touchCommand(String moduleName, StringBuilder sb) {
    String resourcePath = "\"/system/modules/" + moduleName + "/\"";
    sb.append("setCurrentProject \"Offline\" \n");
    sb.append("lockResource ").append(resourcePath).append(" \n");
    sb.append("setDateLastModified ").append(resourcePath).append(" \"")
      .append(System.currentTimeMillis()).append("\" ").append("true").append(" \n");
    sb.append("unlockResource ").append(resourcePath).append(" \n");
    sb.append("publishProjectAndWait \n");
  }

  private static void clearCachesCommand(StringBuilder sb) {
    sb.append("clearCaches \n");
  }

  public static void main(String[] ar){
    System.out.println(System.currentTimeMillis());
  }

  /**
   * @param filename
   * @param content
   * @param prefix
   * @param suffix
   * @return
   * @throws IOException
   */
  private static File createTempFile(String filename, String content, String prefix, String suffix) throws IOException {
    File script = null;
    FileWriter writer = null;
    try {
      script = File.createTempFile(prefix + filename + "_", suffix);
      script.deleteOnExit();
      writer = new FileWriter(script);
      writer.write(content.toString());
    } finally {
      try {
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return script;
  }

  public static String readFileAsStringNoException(File filePath) {
    StringBuilder fileData = new StringBuilder(1000);
    BufferedReader reader;
    try {
      reader = new BufferedReader(new FileReader(filePath));
    } catch (FileNotFoundException e) {
      return null;
    }
    char[] buf = new char[1024];
    int numRead = 0;
    try {
      while ((numRead = reader.read(buf)) != -1) {
        fileData.append(buf, 0, numRead);
      }

    } catch (IOException e) {
      return "";
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
      }
    }
    return fileData.toString();
  }

  /**
   * Calcula la ruta del fichero destino e invoca la operacion de copia
   *
   * @param openCmsWebDir directorio WEB-INF de OpenCms
   * @param moduleFile    fichero de módulo a copiar
   * @throws MojoExecutionException error durante la copia del fichero
   */
  public static void copyFileToModulesDir(File openCmsWebDir, File moduleFile) throws MojoExecutionException {
    File moduleInstallFile = new File(openCmsWebDir, MODULES_RELATIVE_PATH + moduleFile.getName());
    if (log != null) {
      log.info("Copiando módulo [" + moduleFile + "] a directorio de paquetes [" + moduleInstallFile + "]");
    }
    try {
      copyFile(moduleFile, moduleInstallFile);
    } catch (IOException e1) {
      throw new MojoExecutionException("Error copiando el módulo a la carpeta de paquetes", e1);
    }
  }

  /**
   * Utilidad de copia de ficheros
   *
   * @param sourceFile fichero origen
   * @param destFile   fichero destino
   * @throws IOException error durante la copia del fichero origen al fichero destino
   */
  public static void copyFile(File sourceFile, File destFile) throws IOException {
    if (!destFile.exists()) {
      destFile.createNewFile();
    }
    FileChannel source = null;
    FileChannel destination = null;
    try {
      source = new FileInputStream(sourceFile).getChannel();
      destination = new FileOutputStream(destFile).getChannel();
      destination.transferFrom(source, 0, source.size());
    } finally {
      if (source != null) {
        source.close();
      }
      if (destination != null) {
        destination.close();
      }
    }
  }

  public static void setLog(Log log) {
    OpenCmsScriptUtils.log = log;
  }
}
