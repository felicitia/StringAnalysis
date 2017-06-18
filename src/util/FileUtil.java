package util;

import java.io.File;
import java.io.FilenameFilter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class FileUtil {

	/**
	 * 
	 * @param filepath: whole path of the xml file, including .xml
	 * @return
	 */
	public static Document xml2Doc(String filepath){
		try{
			File file = new File(filepath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			return doc;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * find all the files in the "dirpath" that have "extension"
	 * extention with ".", e.g. ".xml"
	 * @param dirpath
	 * @param extension
	 * @return
	 */
	public static File[] getFilesWithExtension(String dirpath, final String extension){
		File[] files = null;
		try{
		File dir = new File(dirpath);
		files = dir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name){
				return name.endsWith(extension);
			}
		});}catch(Exception e){
			e.printStackTrace();
		}
		return files;
	}
	
	/**
	 * get all the subdirectory paths in dirpath
	 * @param dirpath
	 * @return subdirectory name, not the full path
	 */
	public static String[] getSubdirectories(String dirpath){
		String[] directories = null;
		try{
			File dir = new File(dirpath);
			directories = dir.list(new FilenameFilter() {
				  @Override
				  public boolean accept(File current, String name) {
				    return new File(current, name).isDirectory();
				  }
				});
		}catch(Exception e){
			e.printStackTrace();
		}
		return directories;
	}
}
