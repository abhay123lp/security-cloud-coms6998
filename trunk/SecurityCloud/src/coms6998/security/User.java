package coms6998.security;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import coms6998.security.FileObject.FilePermission;
import coms6998.security.s3.S3;
import coms6998.security.s3.S3Bucket;
import coms6998.security.s3.S3File;

public class User {

    private S3 s3;
    private String username;
    private String password;
    private Set<Group> groups;
    private String oneTimePassword;
    private Set<FileObject> files;


    // to store the encryption keys for a particular file
    private Map<String, String> keyMap = new HashMap<String, String>();

    // HashMap to create/retrieve unique instances of the user
    private static final Map<Object, User> instances = new HashMap<Object, User>();

    public static User getInstance(String username, String password) {

        String key = username + password;
        User instance = instances.get(key);

        if (instance == null) {

            synchronized (instances) {

                // check again after synchronization
                instance = instances.get(key);
                if (instance == null) {
                    instance = new User(username, password);

                    // add it to the map
                    instances.put(key, instance);
                }

            } // end of synchronized

        }// end of if loop
        return instance;

    }

    private User(String username, String password) {
        this.username = username;
        this.password = password;
        this.s3 = S3.getInstance();
        this.groups = new HashSet<Group>();
        this.files = new HashSet<FileObject>();
    }

    public String getOTP() {
        return oneTimePassword;
    }

    public void addToGroup(Group group) {
        groups.add(group);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public S3 getS3() {
        return s3;
    }

    public void uploadFile(FileObject file, Group group) throws NoSuchAlgorithmException,
        InvalidKeyException, NoSuchPaddingException,
        InvalidAlgorithmParameterException, IllegalBlockSizeException,
        BadPaddingException, IOException {

    	// add file to the group
    	group.addFileToGroup(file);
        String key = null;

        // check the permission of the file
        if (file.getPermission().equals(FilePermission.Group)) {
            // use the group key
            key = group.generateKey();
            //System.out.println("Group key is "+key);

        } 

        if (file.getPermission().equals(FilePermission.Private)) {
            // use the file key
            key = file.getKey();
        }

        // add them to the hashmaps
        keyMap.put(file.getFilename(), key);
        FileObject.fileMap.put(file.getFilename(), file);

        // upload this to S3
        String toUpload = file.encryptFile(key);

        S3Bucket bucket = s3.getBucket(SecuredCloudManager.BUCKET_NAME);
        if (bucket != null) {
            bucket.uploadFile(file.getFilename(), toUpload);
        } 
        else {
            System.out.println("Bucket not found !!");
        }

    }

    public String downloadFile(String filename) throws InvalidKeyException,
        NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException, IllegalBlockSizeException,
        BadPaddingException {

        // check if the user has permissions to download the file
        String key = keyMap.get(filename);
        if (key == null) {
            System.err.println("User has no access to the file.");
            return "";
        }

        // get the content from the S3 bucket
        S3Bucket bucket = s3.getBucket(SecuredCloudManager.BUCKET_NAME);
        S3File s3file = bucket.getFile(filename);
        String downloadedFile = s3file.getContent();

        // get the key for the file
        //System.out.println("Key is "+key);
        FileObject file = FileObject.fileMap.get(filename);
        String plainText = file.decryptFile(key);
        System.out.println("The file content downloaded is :"+plainText);
        return plainText;

    }

    public void shareFile(List<Group> groups) {

    }
    
    public Set<FileObject> getFiles() {
    	
    	Set<FileObject> files = new HashSet<FileObject>();
    	for (Group group: groups) {
    		files.addAll(group.getFiles());
    	}
    	
    	return files;
    	
    }

    public void setOTP(String otp) {
        this.oneTimePassword = otp;
    }
}
