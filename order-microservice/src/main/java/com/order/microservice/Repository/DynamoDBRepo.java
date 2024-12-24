package com.order.microservice.Repository;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.order.microservice.config.DynamoDbConfig;
import com.order.microservice.constant.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class DynamoDBRepo
{
    @Autowired
    DynamoDbConfig dynamoDbConfig;

    public void  createProductTable() throws Exception
    {
        try {
            System.out.println("Creating the table, wait...");
            Table table =dynamoDbConfig.getDynamoDB().createTable (AppConstant.ORDER,
                    List.of(
                            new KeySchemaElement("id", KeyType.HASH) // the partition key
                            // the sort key
//                            new KeySchemaElement("name", KeyType.RANGE)
                    ),
                    Arrays.asList (
                            new AttributeDefinition("id", ScalarAttributeType.S)
//                            new AttributeDefinition("name", ScalarAttributeType.S)
                    ),
                    new ProvisionedThroughput(10L, 10L)
            );
            table.waitForActive();
            System.out.println("Table created successfully.  Status: " +
                    table.getDescription().getTableStatus());

        } catch (Exception e) {
            System.err.println("Cannot create the table: ");
            throw new Exception("Error has been occured");
        }
    }

    public Table getTable(String tableName) {
        System.out.println(dynamoDbConfig.getDynamoDB().getTable("product"));
        return dynamoDbConfig.getDynamoDB().getTable(tableName);
    }

    public void deleteTable(String tableName ) throws Exception
    {
        Table table =dynamoDbConfig.getDynamoDB().getTable(tableName);
        try {
            System.out.println("Attempting to delete table; please wait...");
            table.delete();
            table.waitForDelete();
            System.out.print("Success.");

        }
        catch (Exception e) {
            System.err.println("Unable to delete table: ");
            System.err.println(e.getMessage());
            throw new Exception("Error has been occured");
        }
    }
}
