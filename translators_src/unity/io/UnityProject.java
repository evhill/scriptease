package io;

import io.constants.UnityField;
import io.constants.UnityType;
import io.unityresource.UnityResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import scriptease.gui.WindowFactory;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.SimpleResource;
import scriptease.util.FileOp;
import scriptease.util.ListOp;

/**
 * Represents a Unity Project file. Implements the GameModule interface to
 * behave as the facade of the project.
 * 
 * Unity Projects, in terms of ScriptEase, are mainly composed of Scene files. A
 * Scene file is similar to a level. Each Scene file contains various objects in
 * it. There are also Prefabs that are used primarily in game run time
 * instantiation. We must consider them as scripts can be attached to Prefabs.
 * 
 * @author remiller
 * @author kschenk
 * @author jyuen
 */
public final class UnityProject extends GameModule {
	/**
	 * All files generated by ScriptEase should have this prefix.
	 */
	public static final String SCRIPTEASE_FILE_PREFIX = "se_";

	/**
	 * The prefix for all tags. This is followed immediately by the type number.
	 */
	public static final String UNITY_TAG = "tag:unity3d.com,2011:";

	public static final String RESOURCE_FOLDER_NAME = "Resources";
	public static final String ASSETS_FOLDER_NAME = "Assets";

	// Note: this used to be static, but we can't make it static since we want
	// to be able to work on multiple projects at the same time.
	private Map<String, File> guidsToMetaFiles;

	private File projectLocation;
	// The nice thing about Unity is that it uses multiple files instead of one,
	// gigantic, binary gigafile (cough NWN cough). So we have can have a
	// directory where all of our ScriptEase generated stuff is stored.
	private File scripteaseGeneratedDirectory;
	private File scripteaseCScriptDirectory;

	private Collection<File> includeFiles;
	private Collection<UnityFile> scenes;
	private Collection<UnityFile> prefabs;
	private Collection<Resource> resources;
	private Collection<UnityScript> scripts;

	/**
	 * Creates a new UnityProjects with no scenes or scripts added.
	 */
	public UnityProject() {
		this.includeFiles = new ArrayList<File>();
		this.scenes = new ArrayList<UnityFile>();
		this.prefabs = new ArrayList<UnityFile>();
		this.resources = new ArrayList<Resource>();
		this.scripts = new ArrayList<UnityScript>();
		this.guidsToMetaFiles = new HashMap<String, File>();
	}

	/**
	 * Returns the active unity project based on the active model in ScriptEase.
	 * Be careful when using this, as the Unity Project must have fully loaded
	 * first.
	 * 
	 * @return
	 */
	public static UnityProject getActiveProject() {
		final SEModel model;

		model = SEModelManager.getInstance().getActiveModel();

		if (model instanceof StoryModel) {
			final GameModule module = ((StoryModel) model).getModule();

			if (module instanceof UnityProject)
				return (UnityProject) module;
		}

		throw new NullPointerException("Attempted to get active Unity Project "
				+ "when there is no Unity Project active. Active model is "
				+ model);
	}

	/**
	 * Create a random 32 char random UUID that does not already exist.
	 * 
	 * @return
	 */
	public String generateGUIDForFile(File file) {
		final Collection<String> existingGUIDs = this.guidsToMetaFiles.keySet();
		String id;
		do {
			id = UUID.randomUUID().toString().replace("-", "");
		} while (existingGUIDs.contains(id));

		this.guidsToMetaFiles.put(id, file);

		return id;
	}

	@Override
	public Collection<Resource> getAutomaticHandlers() {
		final Collection<Resource> automaticHandlers = new ArrayList<Resource>();

		for (UnityFile scene : this.scenes) {
			automaticHandlers.add(scene.getScriptEaseObject());
		}

		return automaticHandlers;
	}

	@Override
	public void addIncludeFiles(Collection<File> includeList) {
		for (File include : includeList) {
			this.includeFiles.add(include);
		}
	}

	@Override
	public void addScripts(Collection<ScriptInfo> scriptList) {
		for (ScriptInfo scriptInfo : scriptList) {
			final Resource subject = scriptInfo.getSubject();

			for (UnityFile scene : this.scenes) {
				if (scene.getTemplateID().equals(subject.getTemplateID())
						|| scene.getObjectByTemplateID(subject.getTemplateID()) != null) {
					this.scripts.add(new UnityScript(scriptInfo, scene));
				}
			}

			for (UnityFile prefab : this.prefabs) {
				if (subject.getTemplateID().equals(prefab.getTemplateID())) {
					this.scripts.add(new UnityScript(scriptInfo, prefab));
				}
			}
		}
	}

	@Override
	public void close() throws IOException {
		for (UnityFile scene : this.scenes) {
			scene.close();
		}
		for (UnityFile prefab : this.prefabs) {
			prefab.close();
		}
	}

	@Override
	public Resource getInstanceForObjectIdentifier(String id) {
		for (UnityFile scene : this.scenes) {
			if (scene.getTemplateID().equals(id))
				return scene;

			for (UnityResource object : scene.getResources())
				if (object.getTemplateID().equals(id))
					return object;
		}

		for (UnityFile prefab : this.prefabs) {
			if (prefab.getTemplateID().equals(id))
				return prefab;

			for (UnityResource object : prefab.getResources())
				if (object.getTemplateID().equals(id))
					return object;
		}
		return null;
	}

	@Override
	public List<Resource> getResourcesOfType(String typeName) {
		// Add any other resources we need to load (e.g. textures) here
		final List<Resource> resources;
		resources = new ArrayList<Resource>();
		if (typeName.equals(UnityType.SCENE.getName()))
			resources.addAll(this.scenes);
		else if (typeName.equals(UnityType.PREFAB.getName()))
			resources.addAll(this.prefabs);
		else {
			for (Resource resource : this.resources) {
				if (resource.getTypes().contains(typeName))
					resources.add(resource);
			}
		}

		return resources;
	}

	@Override
	public String getName() {
		return this.projectLocation.getName();
	}

	@Override
	public void configureTester(ProcessBuilder builder)
			throws FileNotFoundException, UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"The unity translator can't externally test.");
	}

	@Override
	public void load(boolean readOnly) throws IOException {
		final FileFilter sceneFileFilter;
		final FileFilter metaFileFilter;

		final Collection<File> sceneFiles;
		final Collection<File> metaFiles;

		sceneFileFilter = FileOp
				.createExtensionFilter(UnityFile.SCENE_FILE_EXTENSION);

		metaFileFilter = FileOp.createExtensionFilter(UnityFile.META_EXTENSION);

		// sniff out .unity and .prefab files and read them all into memory
		sceneFiles = FileOp.findFiles(this.projectLocation, sceneFileFilter);
		metaFiles = FileOp.findFiles(this.projectLocation, metaFileFilter);

		for (File metaFile : metaFiles) {
			final BufferedReader reader;

			reader = new BufferedReader(new FileReader(metaFile));

			String line;
			while ((line = reader.readLine()) != null) {
				final String guid = UnityField.GUID.getName();
				// Format: [guid: 3d8e5b1dcb8f4f6c86fb7422b2e687df]
				if (line.startsWith(guid)) {
					final String guidValue = line.substring(guid.length() + 2);

					this.guidsToMetaFiles.put(guidValue, metaFile);
				}
			}
			reader.close();
		}

		for (File sceneFile : sceneFiles) {
			final UnityFile scene;

			final Collection<String> type;

			type = ListOp.createList(UnityType.SCENE.getName());

			scene = UnityFile.buildUnityFile(sceneFile, this.guidsToMetaFiles,
					type);

			if (scene != null)
				this.scenes.add(scene);
		}

		if (this.scenes.size() <= 0)
			WindowFactory
					.getInstance()
					.showInformationDialog(
							"No Scene Files",
							"<html>No Scene files were loaded. Either none exist in "
									+ "the directory, or they were not saved as "
									+ "a text file.<br><br>"
									+ "To save a scene file as text:"
									+ "<ol><li>Close the project in ScriptEase II.</li>"
									+ "<li>Load the scene in a pro version of Unity.</li>"
									+ "<li>Under the <b>Edit Menu</b>, open the <b>Project Settings</b> submenu.</li>"
									+ "<li>Choose <b>Editor</b>. The settings will open up in the <b>Inspector</b>.</li>"
									+ "<li>Change the <b>Version Control</b> mode to <b>Meta Files</b>.</li>"
									+ "<li>Change the <b>Asset Serialization</b> mode to <b>Force Text</b>.</li>"
									+ "<li>Reload the project in ScriptEase.</li>"
									+ "<li>Celebrate with laser tag.</li></ol></html>");

		this.resources.addAll(this.loadResources());
	}

	private Collection<Resource> loadResources() throws IOException {
		final Collection<Resource> resources = new ArrayList<Resource>();

		final String[] audioExtensions;
		final String[] imageExtensions;

		final FileFilter resourceFolderFilter;
		final FileFilter audioFilter;
		final FileFilter imageFilter;
		final FileFilter guiSkinFilter;
		final FileFilter prefabFilter;

		final Collection<File> audios = new ArrayList<File>();
		final Collection<File> images = new ArrayList<File>();
		final Collection<File> guiSkins = new ArrayList<File>();
		final Collection<File> prefabFiles = new ArrayList<File>();
		final Collection<File> resourceFolders;

		audioExtensions = new String[] {
				// As Seen On:
				// http://docs.unity3d.com/Documentation/Manual/AudioFiles.html
				".mp3", ".aif", ".wav", ".ogg", ".xm", ".mod", ".it", ".s3m" };

		imageExtensions = new String[] { "psd", "tiff", "jpg", "tga", "png",
				"gif", "bmp", "iff", "pict" };

		resourceFolderFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(RESOURCE_FOLDER_NAME)
						&& file.isDirectory();
			}
		};

		audioFilter = FileOp.createExtensionFilter(audioExtensions);
		imageFilter = FileOp.createExtensionFilter(imageExtensions);
		guiSkinFilter = FileOp.createExtensionFilter("guiskin");
		prefabFilter = FileOp
				.createExtensionFilter(UnityFile.PREFAB_FILE_EXTENSION);

		resourceFolders = FileOp.findFiles(this.projectLocation,
				resourceFolderFilter);

		for (File resourceFolder : resourceFolders) {
			audios.addAll(FileOp.findFiles(resourceFolder, audioFilter));
			images.addAll(FileOp.findFiles(resourceFolder, imageFilter));
			guiSkins.addAll(FileOp.findFiles(resourceFolder, guiSkinFilter));
			prefabFiles.addAll(FileOp.findFiles(resourceFolder, prefabFilter));
		}

		resources.addAll(this.buildSimpleUnityResources(audios,
				UnityType.SE_AUDIO));

		resources.addAll(this.buildSimpleUnityResources(images,
				UnityType.SE_IMAGE));

		resources.addAll(this.buildSimpleUnityResources(guiSkins,
				UnityType.SE_GUISKIN));

		for (File prefabFile : prefabFiles) {
			UnityFile prefab;

			final Collection<String> type;

			type = ListOp.createList(UnityType.PREFAB.getName());

			prefab = UnityFile.buildUnityFile(prefabFile,
					this.guidsToMetaFiles, type);

			if (prefab != null)
				this.prefabs.add(prefab);
		}

		return resources;
	}

	/**
	 * Builds a unity resource from an asset file inside of the resources
	 * folder, such as an image.
	 * 
	 * @param name
	 * @return
	 */
	private final Collection<Resource> buildSimpleUnityResources(
			final Collection<File> files, final UnityType type) {
		final Collection<Resource> resources = new ArrayList<Resource>();

		for (File file : files) {
			final SimpleResource resource;

			String name = FileOp.getFileNameUpTo(file,
					UnityProject.RESOURCE_FOLDER_NAME);

			name = FileOp.removeExtension(name);

			resource = SimpleResource.buildSimpleResource(type.getName(), name);

			resources.add(resource);
		}
		return resources;
	}

	@Override
	public void save(boolean compile) throws IOException {
		// Delete all files in the ScriptEase folder.
		for (File file : this.scripteaseGeneratedDirectory.listFiles()) {
			file.delete();
		}

		for (File file : this.scripteaseCScriptDirectory.listFiles()) {
			file.delete();
		}

		// Write out the scene files.
		for (UnityFile scene : this.scenes) {
			scene.write();
		}

		// Write out the prefab files.
		for (UnityFile prefab : this.prefabs) {
			prefab.write();
		}

		// Write the script files to the ScriptEase folder.
		for (UnityScript script : this.scripts) {
			script.write(this.scripteaseGeneratedDirectory);
			// We then remove each script from the model immediately after
			// writing it, for next time.
			script.removeFromScene();
		}

		for (File includeFile : this.includeFiles) {
			final String includeName = includeFile.getName();
			final File copyDir;

			if (includeName.endsWith(".cs")) {
				copyDir = this.scripteaseCScriptDirectory;
			} else {
				copyDir = this.scripteaseGeneratedDirectory;
			}

			FileOp.copyFile(includeFile, new File(copyDir + "/" + includeName));
		}

		// Reset the story to the state it was at before the save.
		this.scripts.clear();
		this.includeFiles.clear();
		UnityScript.resetScriptCounter();
	}

	@Override
	public File getLocation() {
		return new File(this.projectLocation.getAbsolutePath());
	}

	@Override
	public void setLocation(File location) {
		if (this.projectLocation != null) {
			throw new IllegalStateException(
					"Cannot change Unity project location after it is set.");
		}

		if (location.isDirectory())
			this.projectLocation = location;
		else
			// Not sure why this wouldn't be a directory, but let's be safe.
			this.projectLocation = location.getParentFile();

		final String locationPath = this.projectLocation.getAbsolutePath();
		final String SCRIPTEASE_FOLDER = "/ScriptEase Scripts";
		final String PLUGINS_FOLDER = "/Plugins";
		final String SCRIPTEASE_C_FOLDER = PLUGINS_FOLDER
				+ "/Scriptease C Scripts";
		final String ASSETS_FOLDER = "/Assets";
		final String seGeneratedFolderName;
		final String seCScriptFolderName;

		if (locationPath.endsWith(ASSETS_FOLDER)) {
			seGeneratedFolderName = locationPath + SCRIPTEASE_FOLDER;
			seCScriptFolderName = locationPath + SCRIPTEASE_C_FOLDER;
		} else {
			seGeneratedFolderName = locationPath + ASSETS_FOLDER
					+ SCRIPTEASE_FOLDER;
			seCScriptFolderName = locationPath + ASSETS_FOLDER
					+ SCRIPTEASE_C_FOLDER;
		}

		this.scripteaseGeneratedDirectory = new File(seGeneratedFolderName);
		this.scripteaseCScriptDirectory = new File(seCScriptFolderName);
		// Seriously Java? mkdir()? What kind of method name is that!?
		this.scripteaseGeneratedDirectory.mkdir();
		this.scripteaseCScriptDirectory.mkdirs();
	}

	@Override
	public String getImageType() {
		return UnityType.SE_IMAGE.getName();
	}

	@Override
	public String getAudioType() {
		return UnityType.SE_AUDIO.getName();
	}

	@Override
	public String getDialogueLineType() {
		return UnityType.SE_DIALOGUELINE.getName();
	}

	@Override
	public String getDialogueType() {
		return UnityType.SE_DIALOGUE.getName();
	}

	public String getQuestionType() {
		// TODO This doesnt get checked in SEII yet. When it does, add here.
		return null;
	};

}
