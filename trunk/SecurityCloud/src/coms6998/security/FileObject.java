/**
 * 
 */
package coms6998.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class FileObject {

	private String fileName;
	private String plaintext;
	private String cipherText;
	private List<Group> groups;
	private FilePermission permission;
	private String key; // will use a hash of this for encryption
	
	public static HashMap<Object, FileObject> fileMap = new HashMap<Object, FileObject>();
	public static FileObject getInstance(String filename, String content) {
		FileObject instance = fileMap.get(filename);
		if (instance == null) {
			synchronized (fileMap) {
				instance = fileMap.get(filename);
				if (instance == null) {
					instance = new FileObject(filename, content);
					fileMap.put(filename, instance);
				}
			}
		}
		return instance;
	}

	public FileObject(String filename, String content) {
		this.fileName = filename;
		this.plaintext = content;
	}

	// share with a group
	public void shareWithGroup(Group group) {
		groups.add(group);
	}
	
	// file permissions
	public enum FilePermission {
		Public, 
		Private,
		Group
	}
	
	public String getEncryptedFile() {
		return this.cipherText;
	}
	
	public void setPermission(FilePermission permission) {
		this.permission = permission;
	}
	
	// use the name of the file to create a 256-bit encryption key
	public void generateKey() throws NoSuchAlgorithmException {
		
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hash = md.digest(plaintext.getBytes());
		//System.out.println("Size of the key for the file "+this.fileName+ " is "+hash.length);
		this.key = new String(hash);
		//System.out.println("Key for the file is "+this.key);

	}
	
	public void encryptFile() throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
		// initialize the cipher for encryption in AES/CBC mode
		Cipher cipher;
		cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);
		
		byte[] encryptedText = new byte[cipher.getOutputSize(plaintext.getBytes().length)];
		encryptedText = cipher.doFinal(plaintext.getBytes());
		
		cipherText = new String(encryptedText);
		
	}
	
	public String decryptFile() throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
		// initialize the cipher for encryption in AES/CBC mode
		Cipher cipher;
		cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);
		
		byte[] decryptText = new byte[cipher.getOutputSize(cipherText.getBytes().length)];
		decryptText = cipher.doFinal(cipherText.getBytes());
		
		return new String(decryptText);

	}
 	
	// check if the file is public
	public boolean isPublic() {
		if (this.permission == FilePermission.Public) return true;
		else return false;
	}

	// check if the file is private
	public boolean isPrivate() {
		if (this.permission == FilePermission.Private) return true;
		else return false;
	}
	
	// check if the file is shared with a group
	public boolean isSharedWithGroup(Group group) {
		for (Group grp: groups) {
			if  (grp.getName().equals(group.getName())) {
				return true;
			}
		}
		return false;
	}
	
	// get the permission of the file
	public FilePermission getPermission() {
		return this.permission;
	}

	// generate the signature of the file
	public static byte[] getSignature(byte[] plainText, PrivateKey key)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		// generate the hash of the plaintext
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hash = md.digest(plainText);

		// encrypt the hash using the private key
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherText = new byte[cipher.getOutputSize(hash.length)];
		cipherText = cipher.doFinal(hash);

		return cipherText;

	}

	// verify the signature
	public static boolean verifySignature(byte[] plainText, Key key,
			byte[] origHash) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {

		// generate the hash of the decrypted plain text first
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hash = md.digest(plainText);
		String hash1 = new String(hash);

		// decrypt the signature using RSA
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decryptHash = new byte[cipher.getOutputSize(origHash.length)];
		decryptHash = cipher.doFinal(origHash);
		String hash2 = new String(decryptHash);

		// now compare the generated hash and original hash
		if (hash1.equals(hash2)) {
			return true;
		} else {
			return false;
		}

	}

	// get the name of the file
	public String getFilename() {
		return this.fileName;
	}

	// get the key used for encryption
	public String getKey() {
		return this.key;
	}

}
