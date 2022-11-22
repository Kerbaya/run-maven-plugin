/*
 * Copyright 2022 Kerbaya Software
 * 
 * This file is part of run-maven-plugin. 
 * 
 * run-maven-plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * run-maven-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with run-maven-plugin.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.kerbaya.run;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;

import lombok.Getter;
import lombok.Setter;

@org.apache.maven.plugins.annotations.Mojo(
		name="run",
		requiresProject=false,
		threadSafe=true,
		aggregator=false,
		requiresOnline=true)
public class RunMojo implements org.apache.maven.plugin.Mojo
{
	public static final String ARGUMENT_PREFIX = "arg.";
	public static final String OPTION_PREFIX = "opt.";
	
	@Getter
	@Setter
	private Log log;
	
	/**
	 * <p>The artifact coordinates in the format 
	 * {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}</p>
	 * <p>Example: {@code org.myorg:myapp:1.0.0}</p> 
	 */
	@Parameter(property="artifact", required=true)
	private String artifact;
	
	/**
	 * <p>The full class name of the main class.  If omitted, {@code artifact} will be executed as a runnable JAR.</p>
	 * Example: {@code org.myorg.myapp.Main}
	 */
	@Parameter(property="className")
	private String className;
	
	/**
	 * <p>String arguments to pass to the method.  May also be specified using {@code arg.#} parameters</p>
	 * <p>Model example:</p>
	 * <code><pre>
	 * &lt;arguments&gt;
	 * 	&lt;argument&gt;arg0_value&lt;/argument&gt;
	 * 	&lt;argument&gt;arg1_value&lt;/argument&gt;
	 * &lt;/arguments&gt;
	 * </pre></code>
	 * <p>Command-line example:</p>
	 * <code><pre>-Darg.0=arg0_value -Darg.1=arg1_value</pre></code>
	 */
	@Parameter
	private List<String> arguments;
	
	/**
	 * <p>String options to pass to the forked Java runtime environment.  May also be specified using {@code opt.#} 
	 * parameters</p>
	 * <p>Model example:</p>
	 * <code><pre>
	 * &lt;options&gt;
	 * 	&lt;option&gt;-Xms1g&lt;/option&gt;
	 * 	&lt;option&gt;-DpropName=propValue&lt;/option&gt;
	 * &lt;/options&gt;
	 * </pre></code>
	 * <p>Command-line example:</p>
	 * <code><pre>-Dopt.0=-Xms1g -Dopt.1=-DpropName=propValue</pre></code>
	 */
	@Parameter
	private List<String> options;
	
	@Inject
	private RepositorySystem rs;

	@Parameter(defaultValue="${repositorySystemSession}", required=true, readonly=true)
	private RepositorySystemSession rss;
	
	@Parameter(defaultValue="${project.remoteProjectRepositories}", readonly=true)
	private List<RemoteRepository> remoteProjectRepositories;
	
	@Parameter(defaultValue="${session.userProperties}", readonly=true)
	private Properties properties;
	
	private void debug(String pattern, Object... args)
	{
		if (log != null && log.isDebugEnabled())
		{
			log.debug(args.length == 0 ? pattern : String.format(pattern, args));
		}
	}
	
	private static URL toUrl(URI uri)
	{
		try
		{
			return uri.toURL();
		}
		catch (MalformedURLException e)
		{
			throw new IllegalStateException(e);
		}
	}
	
	private List<String> calcProperties(List<String> model, String prefix)
	{
		if (model != null && !model.isEmpty())
		{
			return model;
		}
		
		if (properties == null)
		{
			return Collections.emptyList();
		}
		
		List<String> result = new ArrayList<>();
		int i = 0;
		String value;
		while((value = properties.getProperty(prefix + i)) != null)
		{
			result.add(value);
			i++;
		}
		
		return result;
	}
	
	private List<String> calcArguments()
	{
		return calcProperties(arguments, ARGUMENT_PREFIX);
	}
	
	private List<String> calcOptions()
	{
		return calcProperties(options, OPTION_PREFIX);
	}
	
	private String calcMainClassName(List<File> classPathArtifacts) throws IOException
	{
		if (className != null)
		{
			return className;
		}
		
		File file = classPathArtifacts.get(0);
		if (file == null)
		{
			return null;
		}
		
		try (JarFile jar = new JarFile(file))
		{
			return jar.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
		}
	}
	
	private DefaultArtifact createArtifact() throws MojoFailureException
	{
		//<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
		String[] parts = artifact.split(":");
		String groupId = parts[0];
		String artifactId = parts[1];
		switch (parts.length)
		{
		case 3:
			return new DefaultArtifact(groupId, artifactId, "jar", parts[2]);
		case 4:
			return new DefaultArtifact(groupId, artifactId, parts[2], parts[3]);
		case 5:
			return new DefaultArtifact(groupId, artifactId, parts[3], parts[2], parts[4]);
		}
		
		throw new MojoFailureException("invalid artifact: " + artifact);
	}
	
	private static void add(Consumer<? super File> list, DependencyNode node)
	{
		Artifact a = node.getArtifact();
		if (a != null)
		{
			File file = a.getFile();
			if (file != null)
			{
				list.accept(file);
			}
		}

		for (DependencyNode child: node.getChildren())
		{
			add(list, child);
		}
	}
	
	private void execute0() 
			throws DependencyResolutionException, IOException, MojoFailureException, InterruptedException, MojoExecutionException
	{
		DefaultArtifact artifact = createArtifact();
		
		debug("resolving: %s", artifact);
		DependencyResult dr = rs.resolveDependencies(
				rss, 
				new DependencyRequest(
						new CollectRequest(new Dependency(artifact, "compile"), remoteProjectRepositories), 
						null));
		
		List<File> classPathArtifacts = new ArrayList<>();
		
		add(classPathArtifacts::add, dr.getRoot());
		
		String className = calcMainClassName(classPathArtifacts);
		if (className == null)
		{
			throw new MojoFailureException("className not set and Main-Class not found");
		}
		
		String classPath = classPathArtifacts.stream()
				.map(File::toURI)
				.map(RunMojo::toUrl)
				.map(URL::toString)
				.collect(Collectors.joining(" "));
		
		Manifest manifest = new Manifest();
		Attributes mainAttrs = manifest.getMainAttributes();
		mainAttrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		mainAttrs.put(Attributes.Name.CLASS_PATH, classPath);
		
		final int rc;
		
		Path tmpJar = Files.createTempFile(null, ".jar");
		try
		{
			try (OutputStream os = Files.newOutputStream(tmpJar, StandardOpenOption.TRUNCATE_EXISTING);
					JarOutputStream zos = new JarOutputStream(os, manifest))
			{
			}
			
			List<String> command = new ArrayList<>();
			command.add(Paths.get(System.getProperty("java.home"))
					.resolve("bin")
					.resolve("java")
					.toAbsolutePath()
					.toString());
			command.add("-cp");
			command.add(tmpJar.toAbsolutePath().toString());
			command.addAll(calcOptions());
			command.add(className);
			command.addAll(calcArguments());
			
			debug("command: %s", command);
			
			rc = new ProcessBuilder(command)
					.inheritIO()
					.start()
					.waitFor();
		}
		finally
		{
			Files.delete(tmpJar);
		}
		
		if (rc != 0)
		{
			throw new MojoExecutionException("rc = " + rc);
		}
	}
	
	@Override
	public void execute() throws MojoFailureException, MojoExecutionException
	{
		try
		{
			execute0();
		}
		catch (DependencyResolutionException | IOException | InterruptedException e)
		{
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
}
