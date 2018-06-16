import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.xml.bind.DatatypeConverter;

public class Hash {
    ArrayList<File> files = new ArrayList<File>();
    ArrayList<String> filesHash = new ArrayList<String>();
	
    String directory = new String("");
    String algorithm = "";
	
    Long duplicateSize = new Long(0);
    static Long directorySize = new Long(0);
    Checksum checksum = new CRC32();
    
    int duplicateCount = 0;
    int count = 0;
    int percent;
    
    public Hash() {
    	// default with SHA-256 hash
    	algorithm = "SHA-256";
    }
    
    public Hash(String dir, String alg) {
    	directory = dir;
    	algorithm = alg;
    }
    
    public void findAllDuplicates() {
    	Instant starts = Instant.now();
		float elapsed;
		generateFileList();
		System.out.println("Total Directory Size: "+directorySize);
	    System.out.println("Algorithm: "+algorithm);
	    System.out.println("Directory: "+directory);
	    System.out.println("Files found: "+files.size());
	    removeUniqueSizes();
	    System.out.println("Removed unique file sizes...");
	    System.out.println("==========");
	    System.out.println("Generating hashes...");
		generateHashTable();
	    System.out.println();
	    System.out.println("Hashes Generated: "+filesHash.size() );
	    System.out.println("==========");
		getDuplicates();
		System.out.println("==========");
		System.out.println("Results" );
		System.out.println("Duplicates found: "+duplicateCount);
		System.out.println("Duplicate file size : "+duplicateSize+" bytes" ); 
		float hashTime = (float)(Duration.between(starts, Instant.now()).toMillis())/1000;
		elapsed = ((float)directorySize/hashTime)/1000000 ;
		System.out.println("Duration (seconds): "+hashTime);
		System.out.println("Speed: "+(int)elapsed+ " Mb/sec");
    }
    
    public void generateFileList() {
    	getDirectoryFileList(directory,files);
    }
    
    public void generateHashTable() {
    	Instant starts = Instant.now();
		float elapsed;
		filesHash.clear();
	    for (int i = 0; i < files.size();i++) {
	    	filesHash.add(getHash(files.get(i).toString(),algorithm));
			count+=1;
			if (count > files.size() / 10){
				count=0;
				percent = (int) ((float)filesHash.size()/ files.size()*100);
				System.out.println(percent+" % "+ filesHash.size()+ " / "+files.size());
			}
	    }
		percent = (int) ((float)filesHash.size()/ files.size()*100);
		System.out.println(percent+" % "+ filesHash.size()+ " / "+files.size());
		float hashTime = (float)(Duration.between(starts, Instant.now()).toMillis())/1000;
		elapsed = ((float)directorySize/hashTime)/1000000 ;
		System.out.println("Duration (seconds): "+hashTime);
		System.out.println("Speed: "+(int)elapsed+ " Mb/sec");
    }
    
    public void removeUniqueSizes() {
    	ArrayList<Integer> sizes = new ArrayList<Integer>();
    	boolean foundMatch = false;
    	// store file sizes in a new arraylist to save time calling file size in nested loop
		for (int i = 0; i < files.size();i++) {  
			sizes.add((int) files.get(i).length());
		}
		for (int i = 0; i < files.size();i++) { 
			foundMatch = false;
			for (int j = 0; j < files.size();j++) {
				if (i!=j  ){
					if ((sizes.get(i).intValue() == sizes.get(j).intValue())) { 
						// if there is a duplicate found, skip to the next element in the list
						foundMatch = true;
						break;
					}
				}
			}
			if (foundMatch == false) { 
				// the element is unique, remove the element from both arraylists, no need to check calculate hash
				files.remove(i);
				sizes.remove(i);
				i-=1; 
			}
		}
    }
    
    
    
    public void getDuplicates() {
    	// remove files with unique file sizes first
		for (int i = 0; i < files.size();i++) {
			for (int j = i+1; j < files.size();j++) {
				if (filesHash.get(i) !=null && filesHash.get(j) !=null) {
					if (filesHash.get(i).equals(filesHash.get(j))) {
						System.out.println("Duplicate Found @: "+files.get(i)+" ### "+files.get(j) + " ### " + files.get(i).length()+" bytes" );
						duplicateSize+=files.get(i).length();
						duplicateCount+=1;
					}
				}				
			}
		}
    }
    
    public String getHashFile(String f, String alg) {
    	String result = "";
    	byte[] hash = new byte[1024];
		try {
			Instant starts = Instant.now();
			float elapsed;
			File ff = new File(f);
			System.out.println("File Name: "+f);
			System.out.println("File Size: "+ff.length());getClass();
			if (!alg.equals("CRC32")) {
				hash = MessageDigest.getInstance(algorithm).digest(Files.readAllBytes(Paths.get(f)));
				result = DatatypeConverter.printHexBinary(hash);
			} else {
				byte bytes[] = Files.readAllBytes(Paths.get(f));
				checksum.update(bytes, 0, bytes.length);
				result = String.valueOf(checksum.getValue());
			}
			float hashTime = (float)(Duration.between(starts, Instant.now()).toMillis())/1000;
			elapsed = ((float)ff.length()/hashTime)/1000000 ;
			System.out.println("Duration (seconds): "+hashTime+"  Speed: "+(int)elapsed+ " Mb/sec   Algortihm: "+alg);
		} catch (NoSuchAlgorithmException | IOException e) {
			System.out.println("file in use: "+ f );
		}
    	return result;
    }
    
	public static String getHash(String p, String algorithm) {
		String h = null;
		byte[] hash = new byte[4096];
		try {
			if (!algorithm.equals("CRC32")) {
			hash = MessageDigest.getInstance(algorithm).digest(Files.readAllBytes(Paths.get(p)));
			h = DatatypeConverter.printHexBinary(hash);
			} else {
				byte bytes[] = Files.readAllBytes(Paths.get(p));
				Checksum checksum1 = new CRC32();
				checksum1.update(bytes, 0, bytes.length);
				h = String.valueOf(checksum1.getValue());
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			System.out.println("File in use: "+ p );
		}
		return h;
	}
	public static void getDirectoryFileList(String directoryName, ArrayList<File> files) {
	    // get all the files from a directory
		File directory = new File(directoryName);
	    File[] fList = directory.listFiles();
	    for (File file : fList) {
	        if (file.isFile()) {
	            files.add(file);
	            directorySize += file.length();
	        } else if (file.isDirectory()) {
	            getDirectoryFileList(file.getAbsolutePath(), files);
	        }
	    }
	}
}
