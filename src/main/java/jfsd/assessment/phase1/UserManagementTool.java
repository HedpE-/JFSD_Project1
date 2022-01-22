package jfsd.assessment.phase1;

import java.io.IOException;
import java.nio.file.*;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class UserManagementTool {

	private enum NavigationContexts{
		MAIN,
		USER_MANAGEMENT
	}
	
	static final String defaultFolder = Paths.get(System.getProperty("user.home"), "JUM-Users").toString();
	String selectedFolder;
	
	private final UserManagementUtils userMgmtUtils = new UserManagementUtils();
	
	private NavigationContexts currentNavigationContext = NavigationContexts.MAIN;

	private final Scanner sc = new Scanner(System.in);

	//Execute with 1st argument as false to skip test user files creation
	//The 2nd argument can also be specified to select a different folder 
	//	The folder can be inexistent but the parent must exist.

	//	The application will check for write permission on the topmost folder it finds:
	//		If the specified folder exists, write permissions are not required on Parent folder
	//		If the specified folder doesn't exist, user must have write permissions on the Parent folder to create the full path
	//	If an exception is thrown due to folder related issues, the application will close during initialization and another folder must be chosen

	//  Both arguments are optional but the order cannot be changed
	public static void main(String[] args) {
		boolean createTestUserFiles = !(args != null && args.length > 0 && !Boolean.parseBoolean(args[0]));
		
		String folder = args != null && args.length > 1 && args[1].length() > 0 ? args[1] : defaultFolder;
		
		UserManagementTool tool = new UserManagementTool();
		tool.execute(createTestUserFiles, folder);
	}

	void execute(boolean createTestUserFiles, String folderPath) {
		try {
			setSelectedFolder(folderPath, createTestUserFiles);
			
			System.out.println("\n*****************************************************\n");
			System.out.println("\tWelcome to the User Management tool");
			System.out.println("\tDeveloper R. Gonçalves, Vodafone\n");
			System.out.println("*****************************************************\n");

			showNavigationMenu();
		}
		catch(Exception e) {
			System.out.println("\nFatal error occurred ["+e.getMessage()+"]");
			e.printStackTrace();
		}
		finally {
			sc.close();
		}
	}

	void setSelectedFolder(String folderPath, boolean createTestUserFiles) throws Exception {
		Path path = Paths.get(folderPath);
		System.out.println("Initializing user folder on path '" + folderPath + "'...");

		Path parentFolderPath = path.getParent();
		if(!Files.exists(parentFolderPath, LinkOption.NOFOLLOW_LINKS))
			throw new IOException("Parent folder not found.");
		
		boolean isPathDirectory = Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
		if(!Files.isWritable(parentFolderPath) && !isPathDirectory)
			throw new IOException("User has no write permissions on Parent folder '"+ parentFolderPath.toString() +"'.");

		if (!isPathDirectory) {
			if(Files.deleteIfExists(path))
				System.out.println("Deleted file in same path.");

			userMgmtUtils.createFolder(folderPath);			
		}
		else {
			System.out.println("Existing User folder found.");
			
			if(!Files.isWritable(path))
				throw new IOException("User has no write permissions on path '"+ folderPath +"'.");
		}

		if(createTestUserFiles)
			createTestUserFiles(folderPath);

		selectedFolder = folderPath;

		System.out.println("Finished User folder initialization.");
	}

	private void createTestUserFiles(String folderPath) throws IOException {
		final String[] dummyUserFileNames = new String[] { "john.dillinger.usr", "rachel.carr.usr", "dalton.jones.usr", "barney.stinson.usr", "sandra.bullock.usr", "piper.wright.usr" };
		
		System.out.println("Creating user files...");

		for(String userFile: dummyUserFileNames)
			userMgmtUtils.writeFile(Paths.get(folderPath, userFile), "");
	}

	private void showNavigationMenu() throws Exception {
		try {
			if(currentNavigationContext == NavigationContexts.MAIN)
				showMainMenu();
			else
				showUserManagementMenu();
		}
		catch(InputMismatchException e) {
			String message = e.getMessage();
			if(message != null && !message.isEmpty())
				System.out.println(message);
			else
				System.out.println("You have made an invalid choice!");
			System.out.println();
			
			showNavigationMenu();
		}
	}
	
	private void showMainMenu() throws Exception {
		printNavigationOptions(NavigationContexts.MAIN);

		int option = 0;
		boolean inputValidation = sc.hasNextInt();
		if(inputValidation)
			option =  sc.nextInt();
		sc.nextLine();
		System.out.println();
		
		switch (option) {
			case 1:
				listUserFiles();
				break;
			case 2:
				currentNavigationContext = NavigationContexts.USER_MANAGEMENT;
				break;
			case 3:
				System.out.println("Closing application...\nThank you!");
				return;
			default:
				throw new InputMismatchException();
		}
		System.out.println();
		showNavigationMenu();
	}

	private void showUserManagementMenu() throws Exception {
		final String userNameFormatRegex = "(?m)^[A-Za-z]+ [A-Za-z]+$";
		final String fileNameFormatRegex = "(?m)^[A-Za-z]+\\.[A-Za-z]+(.usr)?$";

		final String userNameFormatErrorMessage = "The user name is composed by the first and last names separated by a Space ' ' character.";
		final String fileNameFormatErrorMessage = "The file name is composed by lower case first and last names separated by a Dot '.' character.";
		
		printNavigationOptions(NavigationContexts.USER_MANAGEMENT);

		int option = 0;
		boolean inputValidation = sc.hasNextInt();
		if(inputValidation)
			option =  sc.nextInt();
		sc.nextLine();
		System.out.println();

		String userName, fileName;			
		switch (option){
			case 1:
				System.out.print("Enter the new user First and Last name (eg.: Will Robins): ");
				userName = sc.nextLine().trim();
	
				if(!Pattern.matches(userNameFormatRegex, userName))
					throw new InputMismatchException("Unrecognized name format. "+userNameFormatErrorMessage+" This input is case insensitive.");
	
				System.out.println();

				fileName = String.join(".", userName.toLowerCase().split(" ")) + ".usr";
				userMgmtUtils.writeFile(Paths.get(selectedFolder, fileName), "");
				break;
			case 2:
				System.out.print("Enter the user file to delete (without extension): ");
				fileName = sc.nextLine().trim().toLowerCase();
	
				if(!Pattern.matches(fileNameFormatRegex, fileName))
					throw new InputMismatchException("Unrecognized file name format. "+fileNameFormatErrorMessage+" This input is case insensitive.");
	
				System.out.println();
	
				if(!fileName.endsWith(".usr"))
					fileName += ".usr";

				try {
					deleteFile(fileName);
				}
				catch(Exception e) {
					System.out.println("An error occurred during file delete operation. "+e.getMessage());
				}
				deleteFile(fileName);
				break;
			case 3:
				System.out.print("Enter the user name (Firstname Lastname) or file name (firstname.lastname) to search: ");
				String input = sc.nextLine().trim().toLowerCase();
	
				if(Pattern.matches(userNameFormatRegex, input))
					fileName = String.join(".", input.split(" ")) + ".usr";
				else if(Pattern.matches(fileNameFormatRegex, input)) {
					fileName = input;
					if(!fileName.endsWith(".usr"))
						fileName += ".usr";
				}
				else
					throw new InputMismatchException("Unrecognized input format. The input must be in one of the following formats:\n\t- "+userNameFormatErrorMessage+"\n\t- "+fileNameFormatErrorMessage+"\n\nThis input is case insensitive.");
	
				System.out.println();
	
				searchFileFromDirectory(Paths.get(selectedFolder), fileName);
				break;
			case 4:
				currentNavigationContext = NavigationContexts.MAIN;
				break;
			default:
				throw new InputMismatchException();
		}
		System.out.println();
		showNavigationMenu();
	}

	private void printNavigationOptions(NavigationContexts navigationContext) {
		String[] menuOptions;
		if(navigationContext == NavigationContexts.MAIN) {
			menuOptions = new String[] {
				"List current User files",
				"Manage user files...",
				"Quit application"
			};
			
			System.out.println("-= Main Menu =-");
		}
		else {
			menuOptions = new String[] {
				"Add a file to the existing directory list",
				"Delete a user specified file from the existing directory list",
				"Search a user specified file from the main directory",
				"Go Back"
			};

			System.out.println("-= User Management Menu =-");			
		}

		System.out.println();

		for(int i=1; i<=menuOptions.length;i++){
			System.out.println(i+". "+menuOptions[i-1]);
		}
		
		System.out.print("\nEnter your choice: ");
	}

	private void listUserFiles() {
		String[] fileNames = userMgmtUtils.listFolderFiles(selectedFolder);
		
		if(fileNames != null && fileNames.length > 0) {
			fileNames = userMgmtUtils.bubbleSort(fileNames);
			
			System.out.println("Current User files:");
			
			for(int i=1;i<=fileNames.length; i++)
				System.out.println("\t"+i+"> "+fileNames[i-1]);
		}
		else
			System.out.println("No User files found.");
	}

	private void deleteFile(String fileName) throws Exception {
		Path filePath = Paths.get(selectedFolder, fileName);
		if(Files.exists(filePath, LinkOption.NOFOLLOW_LINKS))
			userMgmtUtils.deleteFile(filePath);
		else
			System.out.println("File '"+filePath.toString()+"' not found!");
	}

	private void searchFileFromDirectory(Path folderPath, String fileName) {
		String fileNames[] = userMgmtUtils.listFolderFiles(folderPath.toString());

		int i = userMgmtUtils.binarySearch(fileNames, fileName);
		if(i > -1)
			System.out.println("Found User file #"+(i+1)+".");
		else
			System.out.println("Unable to find file '"+fileName+"'.");
	}
}
