package coms6998.security;

import java.io.File;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

public class S3 {

    private static final S3 INSTANCE = new S3("AKIAJQI77NKHES62FKDQ", "V2oud1zY0YtEVfvXlbs/1DvUjjYJ/1Nd8pqKPOk7");
    private AmazonS3 s3;
    
    private S3(String accessKey, String secretKey) {
        AWSCredentials aws = new BasicAWSCredentials(accessKey, secretKey);
        s3 = new AmazonS3Client(aws);
    }
    
    public static S3 getInstance() {
        return INSTANCE;
    }
    
    /**
     * List of buckets present
     * @return
     */
    public List<Bucket> getBuckets() {
        return s3.listBuckets();
    }
    
    /**
     * Returns a bucket if it exists else it returns a null pointer.
     * @param name
     * @return
     */
    public Bucket getBucket(String name) {
        if(s3.doesBucketExist(name)) {
            for(Bucket b: getBuckets()) {
                if(b.getName().equals(name)) {
                    return b;
                }
            }
        }
        return null;
    }
    
    public void uploadFile(String bucketName, File file) {
        s3.putObject(bucketName, file.getName(), file);
    }
}
