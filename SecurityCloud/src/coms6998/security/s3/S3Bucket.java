package coms6998.security.s3;

import java.io.File;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3Bucket {

    private final AmazonS3 s3;
    private final Bucket bucket;
    
    /**
     * Representation of an Amazon Bucket accepts the s3 client and the Amazon Bucket as parameter.
     * @param s3
     * @param bucket
     */
    public S3Bucket(AmazonS3 s3, Bucket bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }
    
    public List<S3ObjectSummary>getBucketContents() {
        ObjectListing listing = s3.listObjects(bucket.getName());
        return listing.getObjectSummaries();
    }
    
    /**
     * Name of the bucket.
     * @return
     */
    public String getName() {
        return bucket.getName();
    }
    
    /**
     * Removes the Bucket. 
     * Note: The user should make sure that you don't use this Object once you invoke this method.
     */
    public void remove() {
        s3.deleteBucket(bucket.getName());
    }
    
    /**
     * Gets the file given by the name from this bucket.
     * @param name
     * @return
     */
    public S3File getFile(String name) {
        S3Object obj = null;
        try {
            obj = s3.getObject(bucket.getName(), name);
        }
        catch(Exception e) {
            System.err.println("No File present with the given name");
            return null;
        }
        return new S3File(obj);
    }
    
    /**
     * Uploads the file to this bucket. The file is stored as the string returned by the File.getName() method 
     * @param bucketName
     * @param file
     */
    public void uploadFile(File file) {
        s3.putObject(bucket.getName(), file.getName(), file);
    }
    
    /**
     * Removes the file with the given name 
     * @param bucketName
     * @param file
     */
    public void deleteFile(String fileName) {
        s3.deleteObject(bucket.getName(), fileName);
    }
}
