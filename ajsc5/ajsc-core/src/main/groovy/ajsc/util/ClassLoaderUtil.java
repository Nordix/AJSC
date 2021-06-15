/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package ajsc.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import java.nio.file.Paths;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ajsc.RouteMgmtService;

public class ClassLoaderUtil {

	static final Logger logger = LoggerFactory.getLogger(RouteMgmtService.class);

	// Parameters
	private static final Class[] parameters = new Class[] { URL.class };
	public void addProperties(String jarFilePath, ClassLoader cl) throws Exception {
		//logger.info("Properties file folder   " + jarFilePath);
		File folder = new File(jarFilePath);
		if (!folder.exists()) {
			logger.info("Properties file folder does not exist " + jarFilePath);
			return;
		}
		//logger.info("*** Adding properties folder to Context classloader classpath --->"
		//+ folder.toURI().toURL().toString());
		addURL(folder.toURI().toURL(), cl);
	}

	public void addJars(String jarFilePath, ClassLoader cl) throws Exception {
		if (cl == null) {
			logger.info("*****Classloader instance passed is null");
			return;
		}
	//	System.out.println("*****jarFilePath"+jarFilePath);
		
		File folder = new File(jarFilePath);
		if (!folder.exists()) {
			logger.info("jar file folder does not exist " + jarFilePath);
			return;
		}
		File[] files = folder.listFiles();
		addJars(files, cl);
	}

	public void addJars(File[] files, ClassLoader cl) throws Exception {
		String jarFileName;
		for (File file : files) {
			if (file.isDirectory()) {
				addJars(file.listFiles(), cl);
			} else {
				jarFileName = file.getName();
				if (jarFileName.endsWith(".jar")
						|| jarFileName.endsWith(".JAR")) {
					//logger.info("*** Adding jar file to Context classloader classpath --->"
					//+ file.toURI().toURL().toString());
					addURL(file.toURI().toURL(), cl);
				}
			}
		}
	}

	/**
	 * Add URL to CLASSPATH
	 */
	public void addURL(URL u, ClassLoader cl) throws IOException {

		String classpath = System.getProperty("java.class.path");
		String[] entries = classpath.split(File.pathSeparator);
		URL[] urls = new URL[entries.length];
		for(int i = 0; i < entries.length; i++) {
			urls[i] = Paths.get(entries[i]).toAbsolutePath().toUri().toURL();
		}

		for (URL url : urls) {
			if (StringUtils.equalsIgnoreCase(url.toString(), u.toString())) {
				logger.info("URL " + u + " is already in the CLASSPATH");
				return;
			}
		}
		Class sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(cl, new Object[] { u });
		} catch (Throwable t) {
			logger.error("Error, could not add URL to system classloader:", t);
		}
	}
}
