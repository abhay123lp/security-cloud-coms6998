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
	private List<Group> groups;
	private String key; // will use a hash of this for encryption

	public FileObject(String filename) {
		this.fileName = filename;
	}

	public void shareWithGroup(Group group) {
		groups.add(group);
	}

	public static byte[] encryptDecryptAes(byte[] plaintext, byte[] key,
			String mode) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, ShortBufferException,
			IllegalBlockSizeException, BadPaddingException {

		// build the initialization vector, using default
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);

		// get key for encryption
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

		// initialize the cipher for encryption in AES/CBC mode
		Cipher cipher;
		cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		if (mode.contains("e")) {
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);
		} else {
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);
		}

		// make sure the cipher text buffer is of appropriate length
		byte[] ciphertext = new byte[cipher.getOutputSize(plaintext.length)];

		// encrypt the message
		ciphertext = cipher.doFinal(plaintext);

		return ciphertext;

	}

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

	public String getFilename() {
		return this.fileName;
	}

	public String getKey() {
		return this.key;
	}

}
