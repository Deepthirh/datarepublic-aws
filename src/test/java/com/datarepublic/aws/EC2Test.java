package com.datarepublic.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.util.CollectionUtils;
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
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(System.getProperty("access_key_id"), System.getProperty("secret_key_id"));

        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(System.getProperty("region"))
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
        boolean hasStatus;
        do {
            System.out.println("Waiting for status update...");
            TimeUnit.SECONDS.sleep(2);
            instanceStatus = ec2.describeInstanceStatus(describeInstanceStatusRequest.withInstanceIds(instanceIds));
            hasStatus = !instanceStatus.getInstanceStatuses().isEmpty();
        } while (!hasStatus);

        System.out.println("\n");
        instanceStatus.getInstanceStatuses().forEach(s -> System.out.printf("Instance with Id %s is %s\n", s.getInstanceId(), s.getInstanceState().getName()));
        System.out.println("\n");
    }

    private void shouldTerminate() {
        List<String> instanceIds = instances.getReservation().getInstances().stream().map(Instance::getInstanceId).collect(Collectors.toList());

        System.out.printf("Terminating aws ec2 instance(s) %s\n", CollectionUtils.join(instanceIds, ","));
        TerminateInstancesResult terminateInstancesResult = ec2.terminateInstances(new TerminateInstancesRequest(instanceIds));

        terminateInstancesResult.getTerminatingInstances().forEach(s -> System.out.printf("Instance with Id %s is %s\n", s.getInstanceId(), s.getCurrentState().getName()));
    }

}
