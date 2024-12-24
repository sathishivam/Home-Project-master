
# Product and Order,Notification Service

## Overview

This project is a microservices-based e-commerce application built with Spring Boot. It includes two main services:

1. **Product Service**: Manages product details including product quantity.
2. **Order Service**: Handles order placement and updates product quantities upon successful order placement.
3. **Notification-Service**: Handles the messages from queue and send notification

## Features

- **Product Management**: CRUD operations for products.
- **Order Management**: Place orders and update product quantities.
- **OpenAPI Documentation**: Auto-generated API documentation using Springdoc OpenAPI.
- **AWS Integration**: Simulated integration with AWS services using LocalStack.
- **Transaction Management**: Ensures consistency between orders and product inventory.

## Prerequisites

- Java 11 or higher
- Maven
- Docker (for LocalStack if AWS services are simulated locally)

## Setup and Installation

1. **Clone the repository**:

   git clone main package then extract https://github.com/gouthamrepo/Home-Project.git
   cd product-microservice
   cd order-microservice
   cd notification-microservice

2. run docker desktop or docker service

3. cd product-microservice

4. Run the docker file using this command - docker-compose up

5. **Setup for DynamoDB,SQS,S3**:
    
    - Run these command to create required database,sqs queue,s3 bucket:
    - The commands for create dynamodb tables and accessing in AWS CLI
    - aws dynamodb create-table \                                  
      --table-name Product \
      --attribute-definitions \
      AttributeName=id,AttributeType=S \
      --key-schema \
      AttributeName=id,KeyType=HASH \
      --billing-mode PAY_PER_REQUEST \
      --endpoint-url http://localhost:4566
   
    - aws dynamodb create-table \                                  
      --table-name Order \  
      --attribute-definitions \
      AttributeName=id,AttributeType=S \
      --key-schema \
      AttributeName=id,KeyType=HASH \
      --billing-mode PAY_PER_REQUEST \
      --endpoint-url http://localhost:4566
   
    - aws --endpoint-url=http://localhost:4566 dynamodb scan --table-name Product
   
    - aws --endpoint-url=http://localhost:4566 dynamodb list-tables
   
    - The below commands for create sqs and accessing in AWS CLI
    - aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name order-queue
   
    - aws --endpoint-url=http://localhost:4566 sqs list-queues
   
    - aws --endpoint-url=http://localhost:4566 sqs get-queue-attributes --queue-url http://sqs.us-west-2.localhost.localstack.cloud:4566/000000000000/order-queue --attribute-names ApproximateNumberOfMessages

    - aws --endpoint-url=http://localhost:4566 sqs receive-message --queue-url http://sqs.us-west-2.localhost.localstack.cloud:4566/000000000000/order-queue --max-number-of-messages 10 --wait-time-seconds 10 --attribute-names All --message-attribute-names All

    - aws --endpoint-url=http://localhost:4566 sqs delete-message --queue-url http://sqs.us-west-2.localhost.localstack.cloud:4566/000000000000/order-queue --receipt-handle <ReceiptHandle>
   
      - The below commands for create s3 and accessing in AWS CLI
      - aws --endpoint-url=http://localhost:4566 s3 mb s3://my-bucket
      - aws --endpoint-url=http://localhost:4566 s3 ls
      - aws --endpoint-url=http://localhost:4566 s3 ls s3://my-bucket

6.**Build the project**:

   - Using Maven:
     mvn clean install


7.**Run the application**:

   - Using Maven:
     mvn spring-boot:run


8.**Access the application**:

   - **Product Service**: `http://localhost:8500`
   - **Order Service**: `http://localhost:8600`
   - **Notification Service**: `http://localhost:8700`


## API Documentation

This project uses Springdoc OpenAPI to generate API documentation automatically. You can access the documentation at:

- **Swagger UI**: `http://localhost:8500/swagger-ui.html` (for Product Service)
- **Swagger UI**: `http://localhost:8600/swagger-ui.html` (for Order Service)

### Example Endpoints

- **Product Service**:
  - `PUT /products/updateProduct?id=12345&quantity=10`: Update the quantity of a product.
  
- **Order Service**:
  - `POST /order/newOrder`: Place a new order.

## AWS Integration

This project integrates with AWS services, simulated locally using LocalStack. To configure AWS settings:

\`\`\`properties
spring.cloud.aws.region.static=us-west-2
spring.cloud.aws.credentials.accessKey=test
spring.cloud.aws.credentials.secretKey=test
spring.cloud.aws.dynamodb.endpoint=http://localhost:4566/
spring.cloud.aws.sqs.endpoint=http://localhost:4566
spring.cloud.aws.s3.endpoint=http://localhost:4566
\`\`\`

## Testing

Unit and integration tests are included in the project. Run the tests using:

- **Maven**:
  mvn test



