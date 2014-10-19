/*
 * Copyright 2010 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * 
 * Modified by Akhil Lohchab
 * Modified by Sambit Sahu
 * Modified by Kyung-Hwa Kim (kk2515@columbia.edu)
 * 
 * 
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.jcraft.jsch.*;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.AttachVolumeResult;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest;
import com.amazonaws.util.StringUtils;

public class AwsSample {

    /*
     * Important: Be sure to fill in your AWS access credentials in the
     *            AwsCredentials.properties file before you try to run this
     *            sample.
     * http://aws.amazon.com/security-credentials
     */

    static AmazonEC2      ec2;
    static KeyPair keyPair;
    public static void main(String[] args) throws Exception {


    	 BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAISLVPAFR7CLS2DBA", "XMJCiyGPxrGfhsVKjs3zXsq33qE5d0GSpWNRSmhE");

         /*********************************************
          * 
          *  #1 Create Amazon Client object
          *  
          *********************************************/
    	 System.out.println("#1 Create Amazon Client object");
         ec2 = new AmazonEC2Client(credentials);

         
       
        try {
        	
        	/*********************************************
        	 * 
             *  #2 Describe Availability Zones.
             *  
             *********************************************/
        	System.out.println("#2 Describe Availability Zones.");
            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
                    " Availability Zones.");

            /*********************************************
             * 
             *  #3 Describe Available Images
             *  
             *********************************************/
            System.out.println("#3 Describe Available Images");
            DescribeImagesResult dir = ec2.describeImages();
            List<Image> images = dir.getImages();
            System.out.println("You have " + images.size() + " Amazon images");
            
            
            /*********************************************
             *                 
             *  #4 Describe Key Pair
             *                 
             *********************************************/
            System.out.println("#9 Describe Key Pair");
            DescribeKeyPairsResult dkr = ec2.describeKeyPairs();
            System.out.println(dkr.toString());
            
            /*********************************************
             * 
             *  #5 Describe Current Instances
             *  
             *********************************************/
            System.out.println("#4 Describe Current Instances");
            DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
            List<Reservation> reservations = describeInstancesRequest.getReservations();
            Set<Instance> instances = new HashSet<Instance>();
            // add all instances to a Set.
            for (Reservation reservation : reservations) {
            	instances.addAll(reservation.getInstances());
            }
            
            System.out.println("You have " + instances.size() + " Amazon EC2 instance(s).");
            for (Instance ins : instances){
            	
            	// instance id
            	String instanceId = ins.getInstanceId();
            	
            	// instance state
            	InstanceState is = ins.getState();
            	System.out.println(instanceId+" "+is.getName());
            }
            ///////////////////////////////////////
            
            String Temp_Group = "Testgroup1"; //name of the group
            CreateSecurityGroupRequest r1 = new CreateSecurityGroupRequest(Temp_Group, "temporal group");
            ec2.createSecurityGroup(r1);
             AuthorizeSecurityGroupIngressRequest r2 = new AuthorizeSecurityGroupIngressRequest();
             r2.setGroupName(Temp_Group);
         
           /*************the property of http*****************/
           IpPermission permission = new IpPermission();
           permission.setIpProtocol("tcp");
           permission.setFromPort(80);
           permission.setToPort(80);
           List<String> ipRanges = new ArrayList<String>();
           ipRanges.add("0.0.0.0/0");
           permission.setIpRanges(ipRanges);
          
           /*************the property of SSH**********************/
           IpPermission permission1 = new IpPermission();
           permission1.setIpProtocol("tcp");
           permission1.setFromPort(22);
           permission1.setToPort(22);
           List<String> ipRanges1 = new ArrayList<String>();
           ipRanges1.add("0.0.0.0/22");
           permission1.setIpRanges(ipRanges1);
             
           /*************the property of https**********************/
           IpPermission permission2 = new IpPermission();
           permission2.setIpProtocol("tcp");
           permission2.setFromPort(443);
           permission2.setToPort(443);
           List<String> ipRanges2 = new ArrayList<String>();
           ipRanges2.add("0.0.0.0/0");
           permission2.setIpRanges(ipRanges2);
          
           /*************the property of tcp**********************/
           IpPermission permission3 = new IpPermission();
           permission3.setIpProtocol("tcp");
           permission3.setFromPort(0);
           permission3.setToPort(65535);
           List<String> ipRanges3 = new ArrayList<String>();
           ipRanges3.add("0.0.0.0/0");
           permission3.setIpRanges(ipRanges3);
  
           /**********************add rules to the group*********************/
           List<IpPermission> permissions = new ArrayList<IpPermission>();
           permissions.add(permission);
           permissions.add(permission1);
           permissions.add(permission2);
           permissions.add(permission3);
           r2.setIpPermissions(permissions);
          
           ec2.authorizeSecurityGroupIngress(r2);
           List<String> groupName = new ArrayList<String>();
           groupName.add(Temp_Group);//wait to out our instance into this group
           
           /*********************************************
           *
           *  #6.2 Create a New Key Pair
           * 
           *********************************************/
 
         
          CreateKeyPairRequest newKeyRequest = new CreateKeyPairRequest();
          newKeyRequest.setKeyName("Test_Key2");
          CreateKeyPairResult keyresult = ec2.createKeyPair(newKeyRequest);
         
          /************************print the properties of this key*****************/
          KeyPair kp = new KeyPair();
          
          kp = keyresult.getKeyPair();
          System.out.println("The key we created is = "
          + kp.getKeyName() + "\nIts fingerprint is="
          + kp.getKeyFingerprint() + "\nIts material is= \n"
          + kp.getKeyMaterial());
        
          String fileName="C:/Users/Akhil/workspace/Test_Key2.pem"; 
          File distFile = new File(fileName); 
          BufferedReader bufferedReader = new BufferedReader(new StringReader(kp.getKeyMaterial()));
          BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(distFile)); 
          char buf[] = new char[1024];        
          int len; 
          while ((len = bufferedReader.read(buf)) != -1) { 
                  bufferedWriter.write(buf, 0, len); 
          } 
          bufferedWriter.flush(); 
          bufferedReader.close(); 
          bufferedWriter.close(); 
          //String myinstance; 
           /*********************************************
             * 
             *  #6 Create an Instance
             *  
             *********************************************/
            System.out.println("#5 Create an Instance");
            String imageId = "ami-76f0061f"; //Basic 32-bit Amazon Linux AMI
            int minInstanceCount = 1; // create 1 instance
            int maxInstanceCount = 1;
            RunInstancesRequest rir = new RunInstancesRequest(imageId, minInstanceCount, maxInstanceCount);
            rir.setKeyName("Test_Key2");
            rir.withSecurityGroups("Testgroup1");
            
            RunInstancesResult result = ec2.runInstances(rir);
            
            //get instanceId from the result
            List<Instance> resultInstance = result.getReservation().getInstances();
            String createdInstanceId = null;
            String myAvailabilityZone = null;
            for (Instance ins : resultInstance){
            	createdInstanceId = ins.getInstanceId();
            	System.out.println("New instance has been created: "+ins.getInstanceId());
            	//myinstance = ins.getInstanceId();
            	
            
            }
            
            Thread.currentThread().sleep(60000);
              
            /*********************************************
             * 
             * 
             * Create a New Volume and attach it
             * 
             ***********************************************/
 
            List<Instance> resultInstance2 = result.getReservation().getInstances();
            
            
            createdInstanceId = null;
            for (Instance ins : resultInstance2){
            
             createdInstanceId = ins.getInstanceId();
             System.out.println("New instance has been created: "+ins.getInstanceId());//print the instance ID
            
            
               /*********************************************
                 * 
                 *  #6.4 Create an Instance
                 *  
                 *********************************************/
         
             CreateVolumeRequest newVol = new CreateVolumeRequest(1, "us-east-1a");
             
             
             CreateVolumeResult volresult = ec2.createVolume(newVol);
             Volume vol1 = volresult.getVolume();
             String volId = vol1.getVolumeId();
             Thread.currentThread().sleep(30000);
             
             AttachVolumeRequest attachRequest = new AttachVolumeRequest().withInstanceId(createdInstanceId).withVolumeId(volId);
             attachRequest.withDevice("/dev/sda5");
             ec2.attachVolume(attachRequest);
             
             
             
             System.out.println("EBS volume has been attached and the volume ID is: "+volId);
            }
            /*********************************************
             * 
             *  #7 Create a 'tag' for the new instance.
             *  
             *********************************************/
            System.out.println("#6 Create a 'tag' for the new instance.");
            List<String> resources = new LinkedList<String>();
            List<Tag> tags = new LinkedList<Tag>();
            Tag nameTag = new Tag("Akhil", "MyFirstInstance");
            
            resources.add(createdInstanceId);
            tags.add(nameTag);
            
            CreateTagsRequest ctr = new CreateTagsRequest(resources, tags);
            ec2.createTags(ctr);
            
            
                        
            /*********************************************
             * 
             *  #8 Stop/Start an Instance
             *  
             *********************************************/
            System.out.println("#7 Stop the Instance");
            List<String> instanceIds = new LinkedList<String>();
            instanceIds.add(createdInstanceId);
            
            //stop
            StopInstancesRequest stopIR = new StopInstancesRequest(instanceIds);
            ec2.stopInstances(stopIR);
            
            //start
            StartInstancesRequest startIR = new StartInstancesRequest(instanceIds);
            ec2.startInstances(startIR);
            
            
          
           System.out.println("#8 Getting DNS, IP.");
           
            DescribeInstancesRequest request =  new DescribeInstancesRequest();
            request.setInstanceIds(instanceIds);

            DescribeInstancesResult result1 = ec2.describeInstances(request);
            List<Reservation> reservations1 = result1.getReservations();

            List<Instance> instances1;
            for(Reservation res : reservations1){
                instances1 = res.getInstances();
                for(Instance ins1 : instances1){
                	System.out.println("The public DNS is: "+ins1.getPublicDnsName()+"\n"+ins1.getRamdiskId());
                    System.out.println("The private IP is: "+ins1.getPrivateIpAddress());
                    System.out.println("The public IP is: "+ins1.getPublicIpAddress()); 
                    
                }
           
          
           /*********************************************
            
           
             *  #10 Terminate an Instance
             *  
             *********************************************/
            System.out.println("#8 Terminate the Instance");
            TerminateInstancesRequest tir = new TerminateInstancesRequest(instanceIds);
            //ec2.terminateInstances(tir);
            
                        
            /*********************************************
             *  
             *  #11 shutdown client object
             *  
             *********************************************/
            ec2.shutdown();
            
            
            }
            } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }

        
    }
	private static CreateVolumeResult CreateVolume(CreateVolumeRequest newVol) {
		// TODO Auto-generated method stub
		return null;
	}
}
