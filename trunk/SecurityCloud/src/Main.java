import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import coms6998.security.FileObject;
import coms6998.security.SecuredCloudManager;
import coms6998.security.User;
import coms6998.security.FileObject.FilePermission;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {
        User user = SecuredCloudManager.getUser("mohankrishna10@gmail.com");
        User user2 = SecuredCloudManager.getUser("asthamalik8@gmail.com");
        User user3 = SecuredCloudManager.getUser("jaya.allamsetty@gmail.com");

        /*FileObject file = new FileObject("test12345", "Testing Text");
        file.setPermission(FilePermission.Private);
        user.uploadFile(file, null);
        user.downloadFile("test12345");*/
        
        FileObject file2 = new FileObject("test2", "Sharing with groups");
        file2.setPermission(FilePermission.Group);
        user2.uploadFile(file2, SecuredCloudManager.getGroup("Group2"));
        
        user.downloadFile("test2");
        user3.downloadFile("test2");
        
        for (FileObject file: user.getFiles()) {
        	System.out.println("File name: "+file.getFilename());
        }
        

    }
    
}
