import java.io.IOException;

public class main {
	public static void  main(String[] args) throws IOException {
		Hash h = new Hash();
		
		//SHA-256 , MD5, CRC32
		
		h = new Hash("D:\\Torrents\\","SHA-256");
		//h = new Hash("D:\\Torrents\\","MD5");
		//h.generateFileList();
		//h.removeUniqueSizes();
		//h.generateHashTable();
		//h.getDuplicates();
		h.findAllDuplicates();
	}
}