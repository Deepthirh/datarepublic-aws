### Question 2:

AWS Work Sheet:

### Prerequisites :
JDK 1.8 , Maven 3.0

#### This is a maven project
To run this test, please make sure maven is present on an appropriate system and `mvn` is added to system's path

Command to run the test:

```mvn clean test```

# Use the following system properties along with the command

```
-Daccess_key_id=<your-access-key>
-Dsecret_key_id=<your-secret-key>
-Dregion=<your-region>
-Daws.ec2.ami=ami-4b13c829
-Daws.ec2.instance.type=t2.micro
-Dnumber.of.instances=1
```

> For Example:

`mvn clean test -Daccess_key_id=<your-access-key>
                -Dsecret_key_id=<your-secret-key>
                -Dregion=<your-region>
                -Daws.ec2.ami=ami-4b13c829
                -Daws.ec2.instance.type=t2.micro
                -Dnumber.of.instances=1`
