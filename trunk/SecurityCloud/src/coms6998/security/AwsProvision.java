package coms6998.security;

import java.io.*;
import java.util.*;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.*;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class AwsProvision {

    private AmazonEC2 ec2;
    private AmazonS3Client s3;
    private String startupImageId;
    private String elasticIp;
    private String amiName;
    private KeyPair keyPair;
    private String keyPairName;
    private String groupName;
    private Double averageCPU = null;
    private String createdImageId;
    private String createdInstanceId;
    private String createdVolumeId;
    private AWSCredentials credentials = null;
    private AmazonCloudWatchClient cloudWatch;
    private CreateSecurityGroupRequest securityGroupRequest;
    private AuthorizeSecurityGroupIngressRequest ingressRules;

    // Hashmap that stores key-value pair for each instance
    // key - username is used as key
    private static final Map<Object, AwsProvision> instances = new HashMap<Object, AwsProvision>();


    public static AwsProvision getInstance(String username, String keyPairName,
            String secGroupName, String amiName) {

        // key per singleton
        String key = username+keyPairName;
        AwsProvision instance = instances.get(key);
        if (instance == null) {
            synchronized (instances) {

                // check again after having acquired the lock to make sure
                // that the instance was not created meanwhile by another thread
                instance = instances.get(key);
                if (instance == null) {
                    instance = new AwsProvision(keyPairName, secGroupName,
                            amiName);
                    // add it to the map
                    instances.put(key, instance);
                }
            }
        }
        return instance;
    }

    /**
     * Default constructor for the class
     * 
     * @param key
     * @param secGroup
     * @param amiName
     */
    private AwsProvision(String key, String secGroup, String amiName) {

        this.keyPairName = key;
        this.groupName = secGroup;
        this.amiName = amiName;

        try {
            this.credentials = new PropertiesCredentials(
                    AwsProvision.class
                    .getResourceAsStream("AwsCredentials.properties"));
        } catch (IOException e) {
            System.out.println("Could not get the security credentials !!");
            System.exit(-1);
        }

        try {

            this.ec2 = new AmazonEC2Client(credentials);

            // generate a key pair
            this.keyPair = this.genKeyPair(this.keyPairName);

            // create a new Security group
            this.securityGroupRequest = new CreateSecurityGroupRequest(
                    this.groupName,
            "Security group for Users logging from workplace");
            this.ec2.createSecurityGroup(this.securityGroupRequest);

            // IpPermission list for defining ingress rules for the group
            List<String> protocolList = new ArrayList<String>();
            protocolList.add("ssh");
            protocolList.add("http");
            protocolList.add("https");
            List<IpPermission> permissionList = this
            .createIpPermission(protocolList);

            // create the ingress rules for the security group
            this.ingressRules = new AuthorizeSecurityGroupIngressRequest(
                    groupName, permissionList);
            this.ingressRules.setIpPermissions(permissionList);
            this.ec2.authorizeSecurityGroupIngress(this.ingressRules);

            // set the availability zone
            AvailabilityZone zone = new AvailabilityZone();
            zone.setZoneName("us-east-1a");

            // create an instance
            this.startupImageId = "ami-76f0061f"; // Basic 32-bit Amazon Linux
            // AMI
            RunInstancesRequest rir = new RunInstancesRequest(
                    this.startupImageId, 1, 1);

            // Set the Instance type to t1.micro
            rir.withInstanceType("t1.micro");
            // attach the key pair
            rir.withKeyName(this.keyPair.getKeyName());
            // attach the security group
            rir.withSecurityGroups(this.groupName);
            // Run the instance
            ec2.runInstances(rir);

            Instance id = null;
            List<Instance> resultInstance = new LinkedList<Instance>();

            do {

                // wait and check if the instance is in running state
                Thread.sleep(10000);

                // get the list of instances
                DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest();
                describeInstanceRequest.withInstanceIds(this.createdInstanceId);
                DescribeInstancesResult instancesResult = this.ec2
                .describeInstances(describeInstanceRequest);
                List<Reservation> reservList = instancesResult
                .getReservations();
                for (Reservation reserv : reservList) {
                    resultInstance = reserv.getInstances();
                    for (Instance ins : resultInstance) {
                        id = ins;
                    }
                    System.out.println("State of instance is "
                            + id.getState().getName().toString());
                }

            } while (!id.getState().getName().toString().contains("running"));

            // instance is in running state
            DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest();
            describeInstanceRequest.withInstanceIds(this.createdInstanceId);
            DescribeInstancesResult instancesResult = this.ec2
            .describeInstances(describeInstanceRequest);
            List<Reservation> reservList = instancesResult.getReservations();

            // get the instance details
            for (Reservation reserv : reservList) {
                resultInstance = reserv.getInstances();
                for (Instance ins : resultInstance) {
                    this.createdInstanceId = ins.getInstanceId();
                }
            }

            // allocate elastic IP address to the instance
            AllocateAddressResult elasticResult = this.ec2.allocateAddress();
            this.elasticIp = elasticResult.getPublicIp();
            System.out.println("New elastic IP: " + this.elasticIp);

            // associate the elastic IP with the created instance
            AssociateAddressRequest aar = new AssociateAddressRequest();
            aar.setInstanceId(this.createdInstanceId);
            aar.setPublicIp(this.elasticIp);
            this.ec2.associateAddress(aar);

            // create a volume and attach it to the instance
            this.attachVolume(this.createdInstanceId);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage()
                    + " while creating the instance");
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        } catch (InterruptedException e) {
            System.out.println("Caught Exception: " + e.getMessage());
        }

    }

    /**
     * This method re-starts an instance on the VM at the specified time
     */
    public void reloadVM() {

        try {

            // load from the saved AMI
            System.out.println("Loading the VM with the AMI "+this.createdImageId);
            RunInstancesRequest rir = new RunInstancesRequest(
                    this.createdImageId, 1, 1);
            rir.withInstanceType("t1.micro");
            rir.withKeyName(this.keyPairName);
            rir.withSecurityGroups(this.getSecGroupName());
            ec2.runInstances(rir);

            List<Instance> resultInstance = new LinkedList<Instance>();

            Thread.sleep(45000);
            // get the instance details
            DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest();
            describeInstanceRequest.withInstanceIds(this.createdInstanceId);
            DescribeInstancesResult instancesResult = this.ec2
            .describeInstances(describeInstanceRequest);
            List<Reservation> reservList = instancesResult.getReservations();

            for (Reservation reserv : reservList) {
                resultInstance = reserv.getInstances();
                for (Instance ins : resultInstance) {
                    this.createdInstanceId = ins.getInstanceId();
                }
            }

            // associate the elastic IP with the created instance
            System.out.println("Attaching the elastic IP after reloading ");
            AssociateAddressRequest aar = new AssociateAddressRequest();
            aar.setInstanceId(this.createdInstanceId);
            aar.setPublicIp(this.elasticIp); // use the previously generated IP
            this.ec2.associateAddress(aar);

            // attach the existing volume it to the instance
            System.out.println("Attaching the volume after reloading ");
            AttachVolumeRequest avr = new AttachVolumeRequest();
            avr.setVolumeId(this.createdVolumeId);
            avr.setInstanceId(this.createdInstanceId);
            avr.setDevice("/dev/sdf");
            this.ec2.attachVolume(avr);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage()
                    + " while reloading the VM");
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        } catch (InterruptedException e) {
            System.out.println("Caught Exception: " + e.getMessage());
        }

        System.out.println("VM reload is complete !");

    }

    /**
     * This method creates a list of Ippermission to be associated with the
     * security group for the ingress rules
     * 
     * @param list
     * @return
     */
    public List<IpPermission> createIpPermission(List<String> list) {

        int port = 0;
        String ipProtocol = null;
        List<String> ipRanges = new LinkedList<String>();
        ipRanges.add("0.0.0.0/0");
        List<IpPermission> permissionList = new LinkedList<IpPermission>();

        for (String protocol : list) {

            if (protocol.equals("ssh")) {
                port = 22;
                ipProtocol = "tcp";
                IpPermission permission = new IpPermission();
                permission.setIpProtocol(ipProtocol);
                permission.setFromPort(port);
                permission.setToPort(port);
                permission.setIpRanges(ipRanges);
                permissionList.add(permission);
            }

            if (protocol.equals("http")) {
                port = 80;
                ipProtocol = "tcp";
                IpPermission permission = new IpPermission();
                permission.setIpProtocol(ipProtocol);
                permission.setFromPort(port);
                permission.setToPort(port);
                permission.setIpRanges(ipRanges);
                permissionList.add(permission);
            }

            if (protocol.equals("https")) {
                port = 443;
                ipProtocol = "tcp";
                IpPermission permission = new IpPermission();
                permission.setIpProtocol(ipProtocol);
                permission.setFromPort(port);
                permission.setToPort(port);
                permission.setIpRanges(ipRanges);
                permissionList.add(permission);
            }

            if (ipProtocol.isEmpty()) {
                System.out.println("Unknown protocol !!");
                return null;
            }

        }

        return permissionList;

    }

    /**
     * This method creates and attaches a volume to the instance when it is in
     * running state
     * 
     * @param instanceId
     */
    public void attachVolume(String instanceId) {

        try {

            // create the volume first
            CreateVolumeRequest cvr = new CreateVolumeRequest();
            cvr.setAvailabilityZone("us-east-1b");
            cvr.setSize(10); // size = 10 gigabytes
            CreateVolumeResult volumeResult = this.ec2.createVolume(cvr);
            this.createdVolumeId = volumeResult.getVolume().getVolumeId();

            // attach the volume
            AttachVolumeRequest avr = new AttachVolumeRequest();
            avr.setVolumeId(createdVolumeId);
            avr.setInstanceId(instanceId);
            avr.setDevice("/dev/sdf");
            this.ec2.attachVolume(avr);
            System.out.println("Volume "+this.createdVolumeId+" has been attached ");

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage()
                    + " while attaching the volume");
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

    }

    /**
     * This method detaches the specified volume from the specified instance
     * 
     * @param instanceId
     * @param volumeId
     */
    public void dettachVolume(String instanceId, String volumeId) {

        try {

            // create a snapshot of the volume before detaching it
            CreateSnapshotRequest csr = new CreateSnapshotRequest();
            csr.setVolumeId(this.createdVolumeId);

            CreateSnapshotResult result = this.ec2.createSnapshot(csr);
            this.createdVolumeId = result.getSnapshot().getSnapshotId();

            System.out.println("Detaching the volume");
            DetachVolumeRequest dvr = new DetachVolumeRequest();
            dvr.setVolumeId(volumeId);
            dvr.setInstanceId(instanceId);

            this.ec2.detachVolume(dvr);
            System.out.println("volume "+this.createdVolumeId+" has been detached");

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage()
                    + " while detaching the volume");
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

    }

    /**
     * This method creates an S3 bucket and attaches it to the specified
     * instance
     * 
     * @param instanceId
     */
    public void attachS3Bucket(String instanceId) {

        this.s3 = new AmazonS3Client(this.credentials);

        try {

            // create bucket
            String bucketName = "cloud-sample-bucket";
            this.s3.createBucket(bucketName);

            // set key
            String key = "object-name.txt";

            // set value
            File file = File.createTempFile("temp", ".txt");
            file.deleteOnExit();
            Writer writer = new OutputStreamWriter(new FileOutputStream(file));
            writer.write("This is a sample sentence.\r\nYes!");
            writer.close();

            // put object - bucket, key, value(file)
            this.s3.putObject(new PutObjectRequest(bucketName, key, file));

            // get object
            S3Object object = s3
            .getObject(new GetObjectRequest(bucketName, key));

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    object.getObjectContent()));
            String data = null;
            while ((data = reader.readLine()) != null) {
                System.out.println(data);
            }

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage()
                    + " while attaching the S3 bucket");
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * This method creates a snapshot of the current state of the AMI for later
     * use
     * 
     * @param instanceId
     * @return
     */
    public String createAmiFromInstance(String instanceId, String imageId) {

        try {

            Collection<String> imageIdList = new ArrayList<String>();
            imageIdList.add(this.createdImageId);

            CreateImageRequest cir = new CreateImageRequest();
            cir.setInstanceId(instanceId);
            cir.setName(this.getAmiName());
            CreateImageResult createImageResult = ec2.createImage(cir);
            this.createdImageId = createImageResult.getImageId();
            System.out.println("New image "+this.createdImageId+" has been created");

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage()
                    + " while creating a snapshot");
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

        return this.createdImageId;

    }

    /**
     * This method creates a cloud watch client and monitors the VM for cpu
     * utilization
     * 
     * @return true - if CPU utilization is greater than 0.1
     * @return false - if CPU utilization is less than 0.1
     */
    public boolean monitorVM() {

        try {

            System.out.println("Creating a cloud watch client");
            this.cloudWatch = new AmazonCloudWatchClient(this.credentials);

            // create request message
            GetMetricStatisticsRequest statRequest = new GetMetricStatisticsRequest();

            // set up request message
            statRequest.setNamespace("AWS/EC2"); // namespace
            statRequest.setPeriod(60); // period of data
            ArrayList<String> stats = new ArrayList<String>();

            System.out.println("Creating a stats request for Average and Sum");
            // Use one of these strings: Average, Maximum, Minimum, SampleCount,
            // Sum
            stats.add("Average");
            // stats.add("Sum");
            statRequest.setStatistics(stats);

            // Use one of these strings: CPUUtilization, NetworkIn, NetworkOut,
            // DiskReadBytes, DiskWriteBytes, DiskReadOperations
            statRequest.setMetricName("CPUUtilization");

            // set time
            GregorianCalendar calendar = new GregorianCalendar(
                    TimeZone.getTimeZone("UTC"));
            calendar.add(GregorianCalendar.SECOND,
                    calendar.get(GregorianCalendar.SECOND) - 1); // 1 second ago
            Date endTime = calendar.getTime();
            System.out.println("Calendar end time is: " + endTime.toString());
            calendar.add(GregorianCalendar.MINUTE, -10); // 10 minutes ago
            Date startTime = calendar.getTime();
            System.out.println("Calendar start time is: "
                    + startTime.toString());

            // specify start and stop times
            statRequest.setStartTime(startTime);
            statRequest.setEndTime(endTime);

            // specify an instance
            System.out.println("Collecting stats for the Instance: "
                    + this.createdInstanceId);
            ArrayList<Dimension> dimensions = new ArrayList<Dimension>();
            dimensions.add(new Dimension().withName("InstanceId").withValue(
                    this.createdInstanceId));
            statRequest.setDimensions(dimensions);

            // get statistics
            GetMetricStatisticsResult statResult = this.cloudWatch
            .getMetricStatistics(statRequest);

            // display
            System.out.println(statResult.toString());
            List<Datapoint> dataList = statResult.getDatapoints();

            Date timeStamp = null;
            for (Datapoint data : dataList) {
                this.averageCPU = data.getAverage();
                timeStamp = data.getTimestamp();
                System.out
                .println("Average CPU utlilization for last 10 minutes: "
                        + this.averageCPU + " at Time: " + timeStamp);
                System.out
                .println("Total CPU utlilization for last 10 minutes: "
                        + data.getSum());
            }

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage()
                    + " while monitoring VM");
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

        if (this.averageCPU <= 0.1) {
            return false;
        } else
            return true;

    }

    /**
     * This method shuts down the VM after detaching the attached volume and
     * after creating a snapshot of the current image
     */
    public void shutdownVM() {

        List<String> instanceIds = new ArrayList<String>();
        instanceIds.add(this.createdInstanceId);

        try {

            // take a snapshot of the VM first
            this.createdImageId = this
            .createAmiFromInstance(this.createdInstanceId, this.createdImageId);

            // wait till the AMI creation is done
            try {
                Thread.sleep(180000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // detach the volume
            this.dettachVolume(this.createdInstanceId, this.createdVolumeId);

            // terminate the instance
            TerminateInstancesRequest tir = new TerminateInstancesRequest(
                    instanceIds);
            TerminateInstancesResult result = this.ec2.terminateInstances(tir);
            System.out.println("VM instance "+this.createdInstanceId+" has been shut down");

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage()
                    + " while shutting down VM");
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

    }

    /**
     * This method generates a new key pair for the user of this instance
     * 
     * @param keyPairName
     * @return
     */
    public KeyPair genKeyPair(String keyPairName) {

        // generate a 2048-bit RSA key pair for the AWS access credentials
        CreateKeyPairRequest keyPairRequest = new CreateKeyPairRequest(
                keyPairName);

        CreateKeyPairResult keyPairResult = ec2.createKeyPair(keyPairRequest);
        KeyPair newPair = keyPairResult.getKeyPair();
        String keyMaterial = newPair.getKeyMaterial();

        try {

            // write the contents of the key to the file "Program-keyPair.pem"
            File file = new File("Program-keyPair.pem");

            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(keyMaterial);
            bw.close();
            fw.close();

            System.out.println("A new key pair with ID "
                    + keyPairRequest.getKeyName() + " generated ");

        } catch (FileNotFoundException e) {
            System.out
            .println("FileNotFoundException while writing the secret key");
            ec2.shutdown();
            System.exit(-1);
        } catch (IOException e) {
            System.out.println("Cannot write the secret key to the file");
            ec2.shutdown();
            System.exit(-1);
        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage()
                    + " while generating key pair");
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

        return newPair;

    }

    public AmazonEC2 getEC2Client() {
        return this.ec2;
    }

    public String getImageId() {
        return this.createdImageId;
    }

    public String getInstanceId() {
        return this.createdInstanceId;
    }

    public String getElasticIp() {
        return this.elasticIp;
    }

    public String getVolumeId() {
        return this.createdVolumeId;
    }

    public AmazonS3Client getS3Client() {
        return this.s3;
    }

    public String getSecGroupName() {
        return this.groupName;
    }

    public String getAmiName() {
        return this.amiName;
    }

}
