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

	public static void main(String[] args) throws NoSuchAlgorithmException,
			InvalidKeyException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException,
			BadPaddingException, IOException {
		User user1 = SecuredCloudManager.getUser("a@gmail.com");
		User user2 = SecuredCloudManager.getUser("ab@gmail.com");
		User user3 = SecuredCloudManager.getUser("abc@gmail.com");
		User user4 = SecuredCloudManager.getUser("abcd@gmail.com");
		User user5 = SecuredCloudManager.getUser("abcde@columbia.edu");

		FileObject file1 = new FileObject("test12345", "Testing Text");
		file1.setPermission(FilePermission.Private);
		user1.uploadFile(file1, null);
		user1.downloadFile("test12345");

		// no other user should be able to download
		user2.downloadFile("test12345");

		FileObject file2 = new FileObject("test2",
				"Sharing objects with groups");
		file2.setPermission(FilePermission.Group);
		user2.uploadFile(file2, SecuredCloudManager.getGroup("Group1"));
		user2.downloadFile("test2");

		// user1 should be able to download
		user1.downloadFile("test2");
		// user3 should not be able to download
		user3.downloadFile("test2");

		// get the file listing for a user
		System.out.println("Files accessible to " + user1.getUsername()
				+ " are:");
		for (FileObject file : user1.getFiles()) {
			System.out.println("File name: " + file.getFilename());
		}

	}

}
