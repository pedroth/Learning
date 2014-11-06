package beginner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Ficheiros {
	public static List<String> listDir(String directoryName) {
		File directory = new File(directoryName);
		List<String> ans = new ArrayList<String>();
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (!file.isFile()) {
				ans.add(file.getAbsolutePath());
			}
		}
		return ans;
	}

	public static List<String> listFiles(String directoryName) {
		File directory = new File(directoryName);
		List<String> ans = new ArrayList<String>();
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				ans.add(file.getAbsolutePath());
			}
		}
		return ans;
	}

	public static List<String> listFilesNames(String directoryName) {
		File directory = new File(directoryName);
		List<String> ans = new ArrayList<String>();
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				ans.add(file.getName());
			}
		}
		return ans;
	}

	public static void changeDir(String filePath, String changeToDir, String newName, String extension) {
		File afile = new File(filePath);

		if (afile.renameTo(new File(changeToDir + "/" + newName + "." + extension))) {
			System.out.println("File is moved successful!");
		} else {
			System.out.println("File is failed to move!");
		}
		// System.out.println(changeToDir +"/"+ newName + "." + extension);
	}
	
	public static void deleteFile(String filePath) {
		File file = new File(filePath);
		 
		if(file.delete()){
			System.out.println(file.getName() + " is deleted!");
		}else{
			System.out.println("Delete operation is failed.");
		}
	}
	

	public static void main(String[] args) {
		String path1 = "C:/Users/pedro/Downloads/Battlestar.Galactica.All.Seasons.COMPLETE..DVDRip.XviD-ArenaBG/S04/episodes";
		String path2 = "C:/Users/pedro/Downloads/Battlestar.Galactica.All.Seasons.COMPLETE..DVDRip.XviD-ArenaBG/S04/Battlestar.Galactica - S04.en";

		List<String> fileA = listFiles(path1);
		List<String> fileB = listFiles(path2);
		for (int j = 0; j < fileA.size(); j++) {
			String name = "bsg" + "S4E" + (j + 1);
			changeDir(fileA.get(j), path1, name, "avi");
			changeDir(fileB.get(j), path1, name, "srt");
			System.out.println(fileB.get(j));
		}
	}
}
