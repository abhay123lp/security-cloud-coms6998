package coms6998.security.s3;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

public class S3 {

    private static final S3 INSTANCE = new S3("AKIAJQI77NKHES62FKDQ", "V2oud1zY0YtEVfvXlbs/1DvUjjYJ/1Nd8pqKPOk7");
    private final AmazonS3 s3;
    
    private S3(String accessKey, String secretKey) {
        AWSCredentials aws = new BasicAWSCredentials(accessKey, secretKey);
        s3 = new AmazonS3Client(aws);
    }
    
    /**
     * Returns an instance of the S3 with the in build parameters that are provided.
     * @return
     */
    public static S3 getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a new bucket with the name specified.
     * @param bucketName
     * @return
     */
    public S3Bucket createBucket(String bucketName) {
        return new S3Bucket(s3, s3.createBucket(bucketName));
    }
    
    /**
     * Deletes a bucket with the name provided.
     * @param bucketName
     */
    public void removeBucket(String bucketName) {
        s3.deleteBucket(bucketName);
    }
    
    /**
     * List of buckets present
     * Note: If possible do not use this method. Use getBucket(name) instead. 
     * @return
     */
    public List<S3Bucket> getBuckets() {
        List<S3Bucket> buckets = new ArrayList<S3Bucket>();
        for(Bucket buck: s3.listBuckets()) {
            buckets.add(new S3Bucket(s3, buck));
        }
        return getBuckets();
    }
    
    /**
     * Returns a bucket if it exists else it returns a null pointer.
     * @param name
     * @return
     */
    public S3Bucket getBucket(String name) {
        if(s3.doesBucketExist(name)) {
            for(Bucket buck: s3.listBuckets()) {
                if(buck.getName().equals(name)) {
                    System.out.println(buck.getName());
                    return new S3Bucket(s3, buck);
                }
            }
        }
        System.err.println("Bucket with name: " + name + " is not present.");
        return null;
    }
}
