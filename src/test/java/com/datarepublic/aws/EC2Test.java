package com.datarepublic.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.util.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(JUnit4.class)
public class EC2Test {

    private AmazonEC2 ec2;
    private RunInstancesResult instances;

    @Before
    public void setUp() {
        String accessKeyId = System.getProperty("access_key_id");
        String secretKeyId = System.getProperty("secret_key_id");
        String region = System.getProperty("region");

        Assert.assertNotNull("Access key id is null!", accessKeyId);
        Assert.assertNotNull("Secret key id is null!", secretKeyId);
        Assert.assertNotNull("Region is null!", region);

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretKeyId);

        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(region)
                .build();
    }

    @Test
    public void shouldCreateCheckStatusAndTerminateEC2Instance() throws Exception {
        shouldCreate();
        shouldCheckIfItIsRunning();
        shouldTerminate();
    }

    private void shouldCreate() {

        String ami = System.getProperty("aws.ec2.ami");
        InstanceType instanceType = InstanceType.fromValue(System.getProperty("aws.ec2.instance.type"));
        Integer count = Integer.getInteger("number.of.instances");

        System.out.printf("Creating %d aws ec2 instance(s) from ami %s of type %s\n", count, ami, instanceType);

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(ami)
                .withInstanceType(instanceType)
                .withMinCount(1)
                .withMaxCount(count);

        instances = ec2.runInstances(runInstancesRequest);

        System.out.println(instances);
    }

    private void shouldCheckIfItIsRunning() throws Exception {
        List<String> instanceIds = instances.getReservation().getInstances().stream().map(Instance::getInstanceId).collect(Collectors.toList());
        DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
        DescribeInstanceStatusResult instanceStatus;
        int tries = 10;
        boolean hasStatus;
        do {
            System.out.println("Waiting for status update...");
            TimeUnit.SECONDS.sleep(2);
            instanceStatus = ec2.describeInstanceStatus(describeInstanceStatusRequest.withInstanceIds(instanceIds));
            hasStatus = !instanceStatus.getInstanceStatuses().isEmpty();
            tries--;
        } while (!hasStatus && tries > 0);

        Assert.assertTrue("Could not get status in 20 sec", hasStatus);

        List<InstanceStatus> list = instanceStatus.getInstanceStatuses().stream()
                .filter(s -> s.getInstanceState().getCode() != 16) // Running status
                .collect(Collectors.toList());

        Assert.assertTrue("Not all instances are runnig!\n" + list.stream().map(InstanceStatus::toString).collect(Collectors.joining("\n")), list.isEmpty());
    }

    private void shouldTerminate() {
        List<String> instanceIds = instances.getReservation().getInstances().stream().map(Instance::getInstanceId).collect(Collectors.toList());

        System.out.printf("Terminating aws ec2 instance(s) %s\n", CollectionUtils.join(instanceIds, ","));
        TerminateInstancesResult terminateInstancesResult = ec2.terminateInstances(new TerminateInstancesRequest(instanceIds));

        List<InstanceStateChange> list = terminateInstancesResult.getTerminatingInstances().stream()
                .filter(s -> s.getCurrentState().getCode() != 32)
                .collect(Collectors.toList());

        Assert.assertTrue("Not all instances are shutting down!\n" + list.stream().map(InstanceStateChange::toString).collect(Collectors.joining("\n")), list.isEmpty());
    }

}
