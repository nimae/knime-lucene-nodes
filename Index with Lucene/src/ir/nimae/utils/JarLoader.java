package ir.nimae.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

public class JarLoader {
	
	public JarLoader(String classPath, String libPath) throws Exception {
		if (libPath != null && !libPath.isEmpty())
			initLibPath(libPath);
		String[] paths = classPath.split("[\\n\\r;]+");
		URL[] urls = new URL[paths.length];
		for (int i = 0; i < paths.length; i++)
			urls[i] = new File(paths[i]).toURI().toURL();
		loaders = new URLClassLoader(urls, this.getClass().getClassLoader());
	}
	
	private void initLibPath(String path) throws Exception {
		System.setProperty("java.library.path", path);
		Field field = ClassLoader.class.getDeclaredField("sys_paths");
		field.setAccessible(true);
		field.set(null, null);
	}
	
	private final ClassLoader loaders;
	
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return Class.forName(name, true, loaders);
	}
	
	public static void main(String[] args) throws Exception {
		new JarLoader("D:\\workspace\\Perexec\\bin", "D:\\workspace\\Perexec\\jni-win\\perlembed")
				.loadClass("perlembed.Perexec");
	}
	
}
