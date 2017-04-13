package diskExceptions;

import java.io.FileNotFoundException;

public class NonExistingDiskException extends FileNotFoundException {
	
	public NonExistingDiskException(){
		
	}
	
	public NonExistingDiskException(String arg0){
		super(arg0);
	}

}
