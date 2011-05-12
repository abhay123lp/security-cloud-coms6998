package coms6998.security.s3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.amazonaws.services.s3.model.S3Object;

/**
 * Wrapper around the S3Object present in S3.
 * @author neo
 *
 */
public class S3File {

    private final S3Object s3Object;
    
    public S3File(S3Object s3Object) {
        this.s3Object = s3Object;
    }
    
    /**
     * Name of the file stored.
     * @return
     */
    public String getName() {
        return s3Object.getKey();
    }

    /**
     * Renames the file.
     * @param name
     */
    public void rename(String name) {
        s3Object.setKey(name);
    }
    
    /**
     * Returns the content of this file as a String
     * @return
     */
    public String getContent() {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(s3Object.getObjectContent()));
        String ret = "";
        String data = null;
        try {
            while ((data = br.readLine()) != null) {
                ret += (data + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
