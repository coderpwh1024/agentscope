package com.coderpwh.htilchat;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HitlChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(HitlChatApplication.class, args);
        printStartupInfo();
    }

    private static void printStartupInfo() {
        System.out.println("\n=== HITL Chat Application Started ===");
        System.out.println("Open: http://localhost:8080");
        System.out.println("\nFeatures:");
        System.out.println("  - Dynamic MCP tool configuration");
        System.out.println("  - Agent interruption support");
        System.out.println("  - Dangerous tool confirmation");
        System.out.println("  - Built-in tools: get_time, list_files, random_number");
        System.out.println("\nPress Ctrl+C to stop.");
    }
}
