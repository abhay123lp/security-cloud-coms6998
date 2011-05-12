package coms6998.security.s3;

import java.io.File;

public class Test {

    public static void main(String[] args) {
        
        S3 s3 = S3.getInstance();
        
//        for(Bucket b: s3.getBuckets()) {
//            System.out.println(b.getName());
//        }
//        System.out.println(s3.getBucket("mk3245"));
//        
//        File f = new File("TempFile");
//        s3.uploadFile("mk3245", f);
//        s3.getBucketContents("mk3245");
//        s3.listBuckets();
//        for(S3ObjectSummary s: s3.getBucketContents("mk3245")) {
//            if(s.getKey().contains("/")) 
//                continue;
//            System.out.println(s.getKey() + "\tsize:" + s.getSize());
//        }
//        System.out.println(s3.getFileContent("mk3245", "TempFile2614"));
        
        System.out.println("Got Instance");
        S3Bucket bucket = s3.getBucket("mk3245");
        System.out.println("Got Bucket");
//        S3File file = bucket.getFile("TempFile");
//        file.rename("TempFile");
//        bucket.deleteFile("TempFile2614");
//        System.out.println("delete Bucket");
//        System.out.println(bucket.getFile("TempFile2614") == null);
        
//        System.out.println(bucket.getFile("TempFile") == null);
//        bucket.uploadFile(new File("TempFile"));
//        System.out.println("uploaded");
//        System.out.println(bucket.getFile("TempFile") == null);
        
        bucket.deleteFile("TempFile");
    }
}
