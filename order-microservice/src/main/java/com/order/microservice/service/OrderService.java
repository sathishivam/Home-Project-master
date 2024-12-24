package com.order.microservice.service;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.*;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.s3.AmazonS3;
import com.order.microservice.Entity.Order;
import com.order.microservice.client.ProductClient;
import com.order.microservice.constant.AppConstant;
import com.order.microservice.Entity.Product;
import com.order.microservice.Repository.DynamoDBRepo;
import com.order.microservice.exception.ProductNonAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

@Service
@Slf4j
public class OrderService
{

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final DynamoDBRepo dynamoDBRepo;
    private final String sqsQueueUrl;
    private final SqsClient sqsClient;
    private final ProductClient productClient;
    private final AmazonS3 amazonS3;
    private final String bucketName;

    public OrderService(DynamoDBRepo dynamoDBRepo,
                        @Value("${sqs.queue.url}") String sqsQueueUrl,
                        SqsClient sqsClient,
                        ProductClient productClient, AmazonS3 amazonS3,
                        @Value("${s3.bucket.name}")String bucketName) {
        this.dynamoDBRepo = dynamoDBRepo;
        this.sqsQueueUrl = sqsQueueUrl;
        this.sqsClient = sqsClient;
        this.productClient = productClient;
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
    }


    @Transactional
    public String placeOrder(Order order) {
        // Call Product Service to get product details
        logger.info("calling product service to getting the product with id : {}",order.getProductId());
        ResponseEntity<Product> productResponse = productClient.getProductById(order.getProductId());

        Product product = productResponse.getBody();

        logger.info("successfully get the response product with id : {}",order.getProductId());

        if (product == null || product.getQuantity() < order.getQuantity()) {
            throw new ProductNonAvailableException("Product not available or insufficient quantity.");
        }

        Table table = dynamoDBRepo.getTable(AppConstant.ORDER);
        String id = UUID.randomUUID().toString();
        Item item = new Item().
                withPrimaryKey("id", id)
                .with("productId", product.getId())
                .with("price", order.getQuantity() * order.getPrice())
                .with("quantity", order.getQuantity())
                .with("status", "PLACED");

        PutItemOutcome outcome = table.putItem(item);

        logger.info("calling product service to getting the product with id : {}",order.getProductId());
        ResponseEntity<String> updateProductResponse = productClient.updateProduct(order.getProductId(),
                product.getQuantity() - order.getQuantity());

        // Send a message to SQS queue
        logger.info("Send message to sqs with order id : {}",id);
        sendMessageToQueue(id);
        logger.info("Message sent successfully with order id : {}",id);
        createFileandSendToS3(order,id);
        logger.info("File Stored Successfully with order id : {}",id);

        return id;
    }

    private void createFileandSendToS3(Order order, String id) {
        String fileName = id + ".txt";
        File file = new File(fileName);
        order.setId(id);
        order.setStatus("File Created");
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(order);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Upload the file to S3
        amazonS3.putObject(bucketName, fileName, file);
    }


    private void sendMessageToQueue(String order) {
        SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(sqsQueueUrl)
                .messageBody("Order placed with ID: " + order)
                .build();
        sqsClient.sendMessage(sendMsgRequest);
    }


    public Order getOrders(String uuid) throws Exception {
        Order order = new Order();
        Table table = dynamoDBRepo.getTable(AppConstant.ORDER);
        logger.info("Starting getProductsById method for id: {}", uuid);
        if (table != null)
        {
            try
            {
                logger.info("Starting getProductsById method for id: {}", uuid);

                // Define the QuerySpec with the partition key
                QuerySpec querySpec = new QuerySpec()
                        .withKeyConditionExpression("id = :v_id")
                        .withValueMap(new ValueMap().withString(":v_id", uuid));
                logger.debug("QuerySpec created with id: {}", uuid);

                // Execute the query
                ItemCollection<QueryOutcome> items = table.query(querySpec);
                logger.info("Query executed successfully for id: {}", uuid);

                // Iterate over the result set
                Iterator<Item> iter = items.iterator();
                while (iter.hasNext()) {
                    Item item = iter.next();
                    if (item != null) {
                        order.setId(item.getString("id"));
                        order.setProductId(item.getString("productId"));
                        order.setPrice(item.getLong("price"));
                        order.setQuantity(item.getInt("quantity"));
                        order.setStatus(item.getString("status"));
                    }
                    logger.debug("Added Order to list: {}", order);
                }
            }
            catch (Exception e)
            {
                logger.error("Unable to read item: {}", uuid);
                throw new Exception("Unable to read the item with ID"+uuid);
            }
        }
        logger.info("Completed getOrderById method for id: {}", uuid);
        return order;
    }

    public List<Order> getAllOrders() throws Exception
    {
        ScanSpec scanSpec = new ScanSpec();
        List<Order> orders = new ArrayList<>();
        try
        {
            Table table = dynamoDBRepo.getTable(AppConstant.ORDER);
            ItemCollection<ScanOutcome> items = table.scan(scanSpec);
            Iterator<Item> iter = items.iterator();
            while (iter.hasNext())
            {
                Item item = iter.next();
                if (item != null)
                {
                    Order order = new Order();
                    order.setId(item.getString("id"));
                    order.setProductId(item.getString("productId"));
                    order.setPrice(item.getLong("price"));
                    order.setQuantity(item.getInt("quantity"));
                    order.setStatus(item.getString("status"));
                    orders.add(order);
                }
                logger.info("Added Product to list: {}", item.toString());
            }

        }
        catch (Exception e)
        {
            logger.error("Unable to scan the table:");
            logger.error(e.getMessage());
            throw new Exception("Error has been occured while getting list of product");
        }
        return orders;
    }

    public void createTable(String tableName) throws Exception
    {
        if (dynamoDBRepo.getTable(tableName) != null)
        {
            dynamoDBRepo.createProductTable();
        }
    }

    public void deleteTable(String tableName) throws Exception
    {
        dynamoDBRepo.deleteTable(tableName);
    }

}
