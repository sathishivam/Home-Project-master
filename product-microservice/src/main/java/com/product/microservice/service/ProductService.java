package com.product.microservice.service;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.*;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.product.microservice.Entity.Product;
import com.product.microservice.Repository.DynamoDBRepo;
import com.product.microservice.constant.AppConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class ProductService
{

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final DynamoDBRepo dynamoDBRepo;

    public ProductService(DynamoDBRepo dynamoDBRepo) {
        this.dynamoDBRepo = dynamoDBRepo;
    }


    @Transactional
    public void saveProduct(Product product) throws Exception
    {
        System.out.println(dynamoDBRepo.getTable(AppConstant.PRODUCT));
        Table table = dynamoDBRepo.getTable(AppConstant.PRODUCT);
        try
        {
            String id = UUID.randomUUID().toString();
            PutItemOutcome outcome = table.putItem(new Item().withPrimaryKey("id", id).
                    with("name", product.getName())
                    .with("price", product.getPrice())
                    .with("quantity", product.getQuantity()));
            logger.info("PutItem succeeded:\n{}", outcome.getPutItemResult());
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public Product getProducts(String uuid) throws Exception {
        Product product = new Product();
        Table table = dynamoDBRepo.getTable(AppConstant.PRODUCT);
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
                        product.setId(item.getString("id"));
                        product.setName(item.getString("name"));
                        product.setPrice(item.getLong("price"));
                        product.setQuantity(item.getInt("quantity"));
                    }
                    logger.debug("Added Product to list: {}", product);
                }
            }
            catch (Exception e)
            {
                logger.error("Unable to read item: {}", uuid);
                throw new Exception("Unable to read the item with ID"+uuid);
            }
        }
        logger.info("Completed getProductsById method for id: {}", uuid);
        return product;
    }

    public List<Product> getAllProducts() throws Exception
    {
        ScanSpec scanSpec = new ScanSpec();
        List<Product> products = new ArrayList<>();
        try
        {
            Table table = dynamoDBRepo.getTable(AppConstant.PRODUCT);
            ItemCollection<ScanOutcome> items = table.scan(scanSpec);
            Iterator<Item> iter = items.iterator();
            while (iter.hasNext())
            {
                Item item = iter.next();
                if (item != null)
                {
                    Product product = new Product();
                    product.setId(item.getString("id"));
                    product.setName(item.getString("name"));
                    product.setPrice(item.getLong("price"));
                    product.setQuantity(item.getInt("quantity"));
                    products.add(product);
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
        return products;
    }

    public void updateProduct(String uuid,Integer quantity)
    {

        UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", uuid)
                .withUpdateExpression("set quantity = :quantity")
                .withValueMap(new ValueMap().withNumber(":quantity", quantity))
                .withReturnValues(ReturnValue.UPDATED_NEW);

        try
        {
            Table table = dynamoDBRepo.getTable(AppConstant.PRODUCT);
            System.out.println("Updating the item...");
            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
            System.out.println("UpdateItem succeeded:\n" + outcome.getItem().toJSONPretty());

        }
        catch (Exception e)
        {
            System.err.println("Unable to update item: " + uuid);
            System.err.println(e.getMessage());
        }
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
