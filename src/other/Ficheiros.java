package other;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import tokenizer.NumbersTokenizer;

public class Ficheiros {
	/**
	 * 
	 * @param directoryName
	 * @return list of directories
	 */
	public static List<String> listDir(String directoryName) {
		File directory = new File(directoryName);
		List<String> ans = new ArrayList<String>();
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (!file.isFile()) {
				ans.add(file.getAbsolutePath());
			}
		}
		return ans;
	}

	/**
	 * 
	 * @param directoryName
	 * @return list of files in a directory
	 */
	public static List<String> listFiles(String directoryName) {
		File directory = new File(directoryName);
		List<String> ans = new ArrayList<String>();
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				ans.add(file.getAbsolutePath());
			}
		}
		return ans;
	}

	/**
	 * 
	 * @param directoryName
	 * @return Map of all filesPathsByFileName under a directory
	 */
	public static Map<String,String> listFilesRecursively(String directoryName) {
		File directory = new File(directoryName);
		Map<String,String> filePathByFileName = new HashMap<String,String>();
		Stack<File> stack = new Stack<>();
		stack.add(directory);
		while (!stack.isEmpty()) {
			File f = stack.pop();
			if(f.isFile()) {
				filePathByFileName.put(f.getName(), f.getAbsolutePath());
			}else if(f.isDirectory()){
				File[] files = f.listFiles();
				for(File file: files) {
					stack.add(file);
				}
			}
		}
		return filePathByFileName;
	}

	/**
	 * 
	 * @param directoryName
	 * @return list file names in a directory
	 */
	public static List<String> listFilesNames(String directoryName) {
		File directory = new File(directoryName);
		List<String> ans = new ArrayList<String>();
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

		if (afile.renameTo(new File(changeToDir + "/" + newName + ((extension!=null)?("." + extension):"")))) {
			System.out.println("File is moved successful!");
		} else {
			System.out.println("File is failed to move!");
		}
		// System.out.println(changeToDir +"/"+ newName + "." + extension);
	}
	
	public static boolean isExtension(String fileName, String extension) {
		String[] s = fileName.split("\\.");
		return extension.equals(s[s.length-1]);
	}
	
	public static String getExtension(String fileName) {
		String[] s = fileName.split("\\.");
		return s[s.length-1];
	}

	public static void deleteFile(String filePath) {
		File file = new File(filePath);

		if (file.delete()) {
			System.out.println(file.getName() + " is deleted!");
		} else {
			System.out.println("Delete operation is failed.");
		}
	}

	public static void main(String[] args) {
		Map<String,String> ficheiros = Ficheiros.listFilesRecursively("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall");
		String path = "C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall";
		NumbersTokenizer numbersTokenizer = new NumbersTokenizer(null);
		for (String fileName : ficheiros.keySet()) {
			String[] episodeNumber = numbersTokenizer.tokenize(fileName);
			String newName = "OverTheGardenWall" + ((int) Double.parseDouble(episodeNumber[0])) + "." + getExtension(fileName);
			Ficheiros.changeDir(ficheiros.get(fileName), path, newName, null);
		}
	}
}
