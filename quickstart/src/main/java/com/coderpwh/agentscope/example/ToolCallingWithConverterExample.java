package com.coderpwh.agentscope.example;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.DefaultToolResultConverter;
import java.time.LocalDateTime;

/**
 * @author coderpwh
 */
public class ToolCallingWithConverterExample {


    public static void main(String[] args) {

    }


//    @Tool(name = "get_user_info", description = "Retrieve user information by user ID", converter = SensitiveDataMaskingConverter.class)
    public static class SimpleTools {
        public UserInfo getUserInfo(@ToolParam(name = "userId", description = "User ID") String userId) {
            return new UserInfo(
                    userId,
                    "John Doe",
                    "john@example.com",
                    "MySecretPassword123",
                    "sk-1234567890abcdef",
                    "4567-1234-8888-6666");
        }


    }



     public static  class  SensitiveDataMaskingConverter extends DefaultToolResultConverter{

     }




    public static class UserInfo {
        @JsonPropertyDescription("User ID")
        private String userId;

        @JsonPropertyDescription("Username")
        private String username;

        @JsonPropertyDescription("Email address")
        private String email;

        @JsonPropertyDescription("Password (sensitive information)")
        private String password;

        @JsonPropertyDescription("API key (sensitive information)")
        private String apiKey;

        @JsonPropertyDescription("Credit card number (sensitive information)")
        private String creditCard;

        @JsonPropertyDescription("User register time, format: yyyy-MM-dd HH:mm:ss")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime = LocalDateTime.now();

        public UserInfo() {
        }

        public UserInfo(
                String userId,
                String username,
                String email,
                String password,
                String apiKey,
                String creditCard) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.password = password;
            this.apiKey = apiKey;
            this.creditCard = creditCard;
        }

        // Getters and setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getCreditCard() {
            return creditCard;
        }

        public void setCreditCard(String creditCard) {
            this.creditCard = creditCard;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }

    /**
     * Order Class
     */
    public static class Order {
        @JsonPropertyDescription("Order unique identifier, format: ORD + 3 digits, e.g., ORD001")
        private String id;

        @JsonPropertyDescription("User ID")
        private String userId;

        @JsonPropertyDescription("Product name, including brand and model information")
        private String product;

        @JsonPropertyDescription(
                "Order current status, possible values: 0=Pending Payment, 1=Paid, 2=Pending"
                        + " Shipment, 3=Shipped, 4=In Transit, 5=Delivered, 6=Completed, 7=Cancelled,"
                        + " 8=Refunding, 9=Refunded")
        private Integer status;

        @JsonPropertyDescription(
                "Order total price in CNY (RMB), including all product prices and shipping fees,"
                        + " excluding tax")
        private Double price;

        /**
         * Address
         * You can try adjusting the comment description of this field to help the model recognize its different meanings
         * #JsonPropertyDescription("Delivery address, including province, city, district, and detailed street address for product delivery")
         */
        @JsonPropertyDescription("Product origin, This refers to the product's country of origin.")
        private String address;

        @JsonPropertyDescription(
                "Quantity of products purchased, indicating the number of units of this product in"
                        + " the order")
        private Integer quantity;

        @JsonPropertyDescription(
                "Order remarks description, filled by users for special delivery requirements or"
                        + " product instructions, such as: handle with care, store at room temperature,"
                        + " waterproof and sunproof, etc.")
        private String description;

        @JsonPropertyDescription("Order creation time, standard time format: yyyy-MM-dd HH:mm:ss")
        private String createTime;

        public Order() {
        }

        public Order(String id, String product, Integer status, Double price) {
            this.id = id;
            this.product = product;
            this.status = status;
            this.price = price;
        }

        public Order(
                String id,
                String userId,
                String product,
                Integer status,
                Double price,
                String address,
                Integer quantity,
                String description,
                String createTime) {
            this.id = id;
            this.userId = userId;
            this.product = product;
            this.status = status;
            this.price = price;
            this.address = address;
            this.quantity = quantity;
            this.description = description;
            this.createTime = createTime;
        }

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }
    }


}
