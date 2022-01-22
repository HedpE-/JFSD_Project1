package jfsd.assessment.phase1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class UserManagementUtils
{
	public void writeFile(Path filePath, String content)
	{
		System.out.println("Writing to file (overwrite if exists): "+filePath.toString());

		try {
			Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		}
		catch(Exception e) {
			System.out.println("An error occurred writing to file '"+filePath.toString()+"'.\n"+e.getMessage());
		}

		System.out.println("File write operation success!");
	}

	public void deleteFile(Path filePath)
	{
		File f = filePath.toFile();
		String realPath = "";
		String inputPath = filePath.toString();
		try {
			realPath = f.getCanonicalPath();

			if (Files.exists(filePath) && (inputPath.equals(realPath))) {
				System.out.println("Deleting file: "+filePath.toString());

				boolean deleted = Files.deleteIfExists(filePath);
				if(deleted)
					System.out.println("Delete file operation success!");
			}
			else{
				System.out.println("File not found!");
			}
		}
		catch (Exception e) {
			System.out.println("An error occurred while deleting file '"+inputPath+"'.");
		}
	}

	public void createFolder(String folderPath) throws Exception {
		if(!new File(folderPath).mkdir())
			throw new IOException("Error creating folder in path '"+folderPath+"'.");
		System.out.println("Folder '"+folderPath+"' created successfully.");
	}

	public String[] listFolderFiles(String folderPath) {
		try {
			File files[] = new File(folderPath).listFiles();
			String fileNames[] = new String[files.length];

			for(int i = 0; i < files.length; i++)
				fileNames[i] = files[i].getName();

			return fileNames;
		}
		catch(Exception e) {
			System.out.println("An error occurred while listing files on folder '"+folderPath+"'.");
		}
		return null;
	}

	public String[] bubbleSort(String[] arr) {
		for(int j=0;j<arr.length-1; j++) {
			for(int i=0; i<arr.length-1;i++) {
				if(arr[i].compareTo(arr[i+1]) > 0) {
					String temp = arr[i];
					arr[i] = arr[i+1];
					arr[i+1] = temp;
				}
			}
		}

		return arr;
	}

	public int binarySearch(String arr[], String key) {
		int first = 0;
		int last = arr.length-1;
		while( first <= last ){
			int mid = (first + last)/2; 
			int comparisonResult = arr[mid].compareTo(key);
			if ( comparisonResult < 0 ){  
				first = mid + 1;
			}
			else if ( comparisonResult == 0 ){  
				return mid; 
			}
			else {  
				last = mid - 1;  
			}
		}
		return -1;
	}
}