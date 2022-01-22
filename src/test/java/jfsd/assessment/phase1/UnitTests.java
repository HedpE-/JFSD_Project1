package jfsd.assessment.phase1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;
import org.junit.function.ThrowingRunnable;

public class UnitTests {

	@Rule
	public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();	
	@Rule
	public final TextFromStandardInputStream systemInRule = TextFromStandardInputStream.emptyStandardInputStream();

	@Test
	public void setSelectedFolder_FolderExists_CreateTestUserFiles_Test() throws Exception {
		
		// Local Test Data
		final String folder = UserManagementTool.defaultFolder;
		final String escapedFolderPath = folder.replace("\\", "\\\\");
		
		final String regex = "Initializing user folder on path '"+escapedFolderPath+"'\\.\\.\\.\\s\\s?Existing User folder found\\.\\s\\s?Creating user files\\.\\.\\.\\s\\s?"
				+ "(Writing to file \\(overwrite if exists\\): "+escapedFolderPath+"\\\\\\w+\\.\\w+\\.usr\\s\\s?File write operation success!\\s\\s?){6}"
				+ "Finished User folder initialization\\.";
		
		// Local Test Data
		
		final UserManagementTool tool = new UserManagementTool();
		tool.setSelectedFolder(folder, true);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(regex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void setSelectedFolder_ParentExists_DontCreateTestUserFiles_Test() throws Exception {
		
		// Local Test Data

		final String folder = UserManagementTool.defaultFolder;
		final String escapedFolderPath = folder.replace("\\", "\\\\");
		
		final String regex = "Initializing user folder on path '"+escapedFolderPath+"'\\.\\.\\.\\s\\s?"
				+ "Folder '"+escapedFolderPath+"' created successfully\\.\\s\\s?"
				+ "Finished User folder initialization\\.";
		
		// Local Test Data
		
		deleteFolderRecursive(new File(folder));
		
		final UserManagementTool tool = new UserManagementTool();
		tool.setSelectedFolder(folder, false);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(regex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void setSelectedFolder_ParentDoesntExist_Test() throws Exception {
		
		// Local Test Data

		final String folder = Paths.get(UserManagementTool.defaultFolder, "invalid", "invalid").toString();
		final String escapedFolderPath = folder.replace("\\", "\\\\");
		
		final String regex = "Initializing user folder on path '"+escapedFolderPath+"'\\.\\.\\.";
		
		// Local Test Data
		
		final UserManagementTool tool = new UserManagementTool();
		assertThrows(IOException.class, new ThrowingRunnable() {
			public void run() throws Exception {
				tool.setSelectedFolder(folder, false);
			}
		});
		
		final String output = systemOutRule.getLog().trim();

		boolean matchResult = Pattern.compile(regex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void navigationContexts_Test() {
		
		// Local Test Data

		final String welcomeHeaderRegex = "\\*+\\s+Welcome to the User Management tool\\s+Developer R. Gonçalves, Vodafone\\s+\\*+";
		final String mainMenuRegex = "(?s)-= Main Menu =-\\s+(\\d[ .\\w]+\\s+){3}\\s+Enter your choice:";
		final String userManagementMenuRegex = "(?s)-= User Management Menu =-\\s+(\\d.[ \\w]+\\s+){4}\\s+Enter your choice:";
		final String applicationExitRegex = "Closing application...\\s\\s?Thank you!";
		
		final String navigationOptions[] = {"2", "4", "3"};
		
		// Local Test Data
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(false, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		boolean matchResult = Pattern.compile(welcomeHeaderRegex).matcher(output).find();
		assertTrue(matchResult);

		matchResult = Pattern.compile(mainMenuRegex).matcher(output).find();
		assertTrue(matchResult);

		matchResult = Pattern.compile(userManagementMenuRegex).matcher(output).find();
		assertTrue(matchResult);

		matchResult = Pattern.compile(applicationExitRegex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void listUserFiles_ExistingUserFiles_Test() {
		
		// Local Test Data

		final String usersListRegex = "(?s)Current User files:\\s*(\\t\\d> \\w+\\.\\w+\\.\\w{3}\\s)+";
		
		final String navigationOptions[] = {"1", "3"};
		
		// Local Test Data
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(true, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(usersListRegex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void listUserFiles_NoUserFiles_Test() {
		
		// Local Test Data

		final Path appFolder = Paths.get(UserManagementTool.defaultFolder);
		
		final String usersListRegex = "No User files found\\.";
		
		final String navigationOptions[] = {"1", "3"};
		
		// Local Test Data
		
		deleteFolderRecursive(appFolder.toFile());
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(false, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(usersListRegex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void addUserFile_ValidUserNameFormat_Test() {
		
		// Local Test Data

		final Path appFolder = Paths.get(UserManagementTool.defaultFolder);
		final String escapedFolderPath = UserManagementTool.defaultFolder.replace("\\", "\\\\");
		
		final String usersListRegex = "(?s)No User files found\\..+Enter the new user First and Last name \\(eg.: Will Robins\\):\\s+"
				+ "Writing to file \\(overwrite if exists\\): "+escapedFolderPath+"\\\\test.user.usr\\s+"
				+ "File write operation success!.+Current User files:\\s+1> test.user.usr\\s\\s";
		
		final String navigationOptions[] = {"1", "2", "1", "Test User", "4", "1", "3"};
		
		// Local Test Data
		
		deleteFolderRecursive(appFolder.toFile());
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(false, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(usersListRegex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void addUserFile_InvalidUserNameFormat_Test() {
		
		// Local Test Data
		
		final String usersListRegex = "(?s)Enter the new user First and Last name \\(eg.: Will Robins\\):\\s+"
				+ "Unrecognized name format\\.[\\w '.]+\\s\\s+";
		
		final String navigationOptions[] = {"2", "1", "invalid", "4", "1", "3"};
		
		// Local Test Data
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(false, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(usersListRegex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void deleteUserFile_ValidFileNameFormat_FileFound_Test() {
		
		// Local Test Data

		final String escapedFolderPath = UserManagementTool.defaultFolder.replace("\\", "\\\\");
		
		final String usersListRegex = "(?s)Enter the user file to delete \\(without extension\\):\\s+"
				+ "Deleting file: "+escapedFolderPath+"\\\\dalton.jones.usr\\s+Delete file operation success!";
		
		final String navigationOptions[] = {"2", "2", "dalton.jones", "4", "1", "3"};
		
		// Local Test Data
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(true, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		boolean matchResult = Pattern.compile(usersListRegex).matcher(output).find();
		assertTrue(matchResult);
		
		matchResult = Pattern.compile("\\d> dalton.jones.usr").matcher(output).find();
		assertFalse(matchResult);
	}

	@Test
	public void deleteUserFile_ValidFileNameFormat_FileNotFound_Test() {
		
		// Local Test Data

		final Path appFolder = Paths.get(UserManagementTool.defaultFolder);
		final String escapedFolderPath = UserManagementTool.defaultFolder.replace("\\", "\\\\");

		final String usersListRegex = "(?s)Enter the user file to delete \\(without extension\\):\\s+"
				+ "File '"+escapedFolderPath+"\\\\dalton.jones.usr' not found!";

		final String navigationOptions[] = {"2", "2", "dalton.jones", "4", "3"};
		
		// Local Test Data
		
		deleteFolderRecursive(appFolder.toFile());
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(false, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(usersListRegex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void deleteUserFile_InvalidFileNameFormat_Test() {
		
		// Local Test Data

		final String usersListRegex = "(?s)Enter the user file to delete \\(without extension\\):\\s+"
				+ "Unrecognized file name format\\.[\\w '.]+";

		final String navigationOptions[] = {"2", "2", "invalid", "4", "3"};
		
		// Local Test Data
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(false, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(usersListRegex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void searchUserFile_ValidUserNameFormat_FileFound_Test() {
		
		// Local Test Data
		
		final String usersListRegex = "(?s)Enter the user name \\(Firstname Lastname\\) or file name \\(firstname.lastname\\) to search:\\s+"
				+ "Found User file #\\d\\.";
		
		final String navigationOptions[] = {"2", "3", "John Dillinger", "4", "3"};
		
		// Local Test Data
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(true, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(usersListRegex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void searchUserFile_ValidUserNameFormat_FileNotFound_Test() {
		
		// Local Test Data

		final Path appFolder = Paths.get(UserManagementTool.defaultFolder);

		final String usersListRegex = "(?s)Enter the user name \\(Firstname Lastname\\) or file name \\(firstname.lastname\\) to search:\\s+"
				+ "Unable to find file 'john.dillinger.usr'\\.";

		final String navigationOptions[] = {"2", "3", "John Dillinger", "4", "3"};
		
		// Local Test Data
		
		deleteFolderRecursive(appFolder.toFile());
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(false, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(usersListRegex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void searchUserFile_ValidFileNameFormat_FileFound_Test() {
		
		// Local Test Data
		
		final String usersListRegex = "(?s)Enter the user name \\(Firstname Lastname\\) or file name \\(firstname.lastname\\) to search:\\s+"
				+ "Found User file #\\d\\.";
		
		final String navigationOptions[] = {"2", "3", "barney.stinson", "4", "3"};
		
		// Local Test Data
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(true, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(usersListRegex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void searchUserFile_ValidFileNameFormat_FileNotFound_Test() {
		
		// Local Test Data

		final Path appFolder = Paths.get(UserManagementTool.defaultFolder);

		final String usersListRegex = "(?s)Enter the user name \\(Firstname Lastname\\) or file name \\(firstname.lastname\\) to search:\\s+"
				+ "Unable to find file 'barney.stinson.usr'\\.";

		final String navigationOptions[] = {"2", "3", "barney.stinson", "4", "3"};
		
		// Local Test Data
		
		deleteFolderRecursive(appFolder.toFile());
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(false, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(usersListRegex).matcher(output).find();
		assertTrue(matchResult);
	}

	@Test
	public void searchUserFile_InvalidFormat_Test() {
		
		// Local Test Data

		final String usersListRegex = "(?s)Enter the user name \\(Firstname Lastname\\) or file name \\(firstname.lastname\\) to search:\\s+"
				+ "Unrecognized input format\\.[\\w '.]+:\\s(\\s[\\w '.-]+\\s){2}\\s+This input is case insensitive.\\s\\s+";

		final String navigationOptions[] = {"2", "3", "invalid", "4", "3"};
		
		// Local Test Data
		
		systemInRule.provideLines(navigationOptions);
		
		final UserManagementTool tool = new UserManagementTool();
		tool.execute(false, UserManagementTool.defaultFolder);
		
		final String output = systemOutRule.getLog().trim();

		final boolean matchResult = Pattern.compile(usersListRegex).matcher(output).find();
		assertTrue(matchResult);
	}

	private boolean deleteFolderRecursive(final File folder) {
		final File[] files = folder.listFiles();
		if(files != null) {
			for(final File file: files)
				deleteFolderRecursive(file);
		}
		return folder.delete();
	}
}
