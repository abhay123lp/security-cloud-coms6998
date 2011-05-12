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
        FileObject file = new FileObject("test12345", "Testing Text");
        file.setPermission(FilePermission.Private);
        user.uploadFile(file, null);
        user.downloadFile("test12345");
    }
}
