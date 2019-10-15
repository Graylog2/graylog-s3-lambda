# Graylog S3 Input
An AWS Lambda function that reads log messages from AWS S3 and sends them to a Graylog GELF (TCP) input.

## Overview

The Graylog S3 Input allows log files written to S3 to be sent to a Graylog cluster. The central component of this input
is the `graylog-s3-lambda` function, which triggers each time a new file is written to S3. When the file is written, the 
Lambda function is automatically triggered. Then, the function reads all lines in the file and sends them to the specified 
Graylog node or cluster. Several log formats are already supported, and more will be added in the future.       

## Installation:

### Step 1: Create base Lambda function and policy.

Navigate to the Lambda service page in the AWS web console. Create a new Lambda function from and specify a function name of your choice, and choose the Java-8 runtime.
Create or specify an execution role with the following permissions. You can also further restrict the Resource permissions as desired for your specific setup.

```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "Policy",
            "Effect": "Allow",
            "Action": [
                "logs:CreateLogGroup"
                "s3:GetObject",
                "logs:CreateLogStream",
                "logs:PutLogEvents"
            ],
            "Resource": [
                "arn:aws:logs:your-region:your-account-number:*"
                "arn:aws:s3:your-region::s3-bucket-name/*"
            ]
        }
    ]
}
```

NOTE: If your Graylog cluster is running in a VPC, you may need to add the AWSLambdaVPCAccessExecutionRole managed role to allow the Lambda function to route traffic to the VPC.

Once the function is created, upload the function code graylog-s3-lambda.jar located in the Preparation task section.  Specify the following method for the Handler: org.graylog.integrations.s3.GraylogS3Function::handleRequest

### Step 2: Specify configuration.

Specify the following environment variables to configure the Lambda function for your Graylog cluster:

* `GRAYLOG_HOST`: *(required)* The hostname or IP address of the Graylog host or load balancer.
* `GRAYLOG_PORT`: *(optional - defaults to 12201)*: The Graylog service port.
* `CONTENT_TYPE`: *(optional - defaults to `text/plain`)* The type of log messages to read. Messages will be parsed according to their content type. Supported values: `application/json`, `text/plain`, and `application/x.cloudflare.log`
* `COMPRESSION_TYPE`: *(optional - defaults to `none`)* The compression type. Supported values: `none`, `gzip`
* `CONNECT_TIMEOUT` *(optional - defaults to 10000ms)* The number of milliseconds to wait for the connection to be established.
* `LOG_LEVEL` *(optional - defaults to INFO)* The level of detail to include in the CloudWatch logs generated from the Lambda function. Supported values are OFF, ERROR, WARN, INFO, DEBUG, TRACE, and ALL. Increase the logging level to help with troubleshooting. See this page for more information. 

More configuration options will be documented soon.

### Step 3: Create S3 trigger.

Create an AWS S3 Trigger for the Lambda function so that the function can execute each Cloudflare log file that is written. Specify the same S3 bucket that you did in the Preparation step and make sure to choose All object create events option is selected. You can also apply any other desired file filters here.

If your Graylog cluster is located within a VPC, you will need to configure your Lambda function to access resources in a VPC.

### Step 4: Create GELF (TCP) input

Create a GELF (TCP) input on a Graylog node. You can create the input globally and put the nodes behind a TCP load balancer if load balancing is desired. 
