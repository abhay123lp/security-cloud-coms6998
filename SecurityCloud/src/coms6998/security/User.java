package coms6998.security;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import coms6998.security.FileObject.FilePermission;
import coms6998.security.s3.S3;
import coms6998.security.s3.S3Bucket;
import coms6998.security.s3.S3File;

public class User {

    private S3 s3;
    private String username = null;
    private String password = null;
    private List<Group> groups = null;
    private String oneTimePassword = null;
    private List<FileObject> files = null;


    // to store the encryption keys for a particular file
    private static Map<FileObject, String> keyMap = new HashMap<FileObject, String>();
    private static final String BUCKET_NAME = "edu.columbia.cloud.test";

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

    public void uploadFile(FileObject file) throws NoSuchAlgorithmException,
    InvalidKeyException, NoSuchPaddingException,
    InvalidAlgorithmParameterException, IllegalBlockSizeException,
    BadPaddingException, IOException {

        // set the permission of the file
        file.setPermission(FilePermission.Private);

        // add the encryption key of the file to its hashmap
        file.generateKey();
        keyMap.put(file, file.getKey());
        FileObject.fileMap.put(file.getFilename(), file);

        // encrypt the file
        file.encryptFile();

        // upload this to S3
        String toUpload = file.getEncryptedFile();

        S3Bucket bucket = s3.getBucket("jla2164");
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

        // get the content from the S3 bucket
        S3Bucket bucket = s3.getBucket("jla2164");
        S3File s3file = bucket.getFile(filename);
        String downloadedFile = s3file.getContent();

        // get the key for the file
        String key = keyMap.get(filename);
        FileObject file = FileObject.fileMap.get(filename);
        String plainText = file.decryptFile();
        System.out.println("The file content downloaded is :"+plainText);
        return plainText;

    }

    public void shareFile(List<Group> groups) {

    }

}
