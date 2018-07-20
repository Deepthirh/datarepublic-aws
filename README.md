# Use the following system properties for the test

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
