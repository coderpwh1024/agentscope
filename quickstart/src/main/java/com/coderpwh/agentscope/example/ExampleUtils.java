package com.coderpwh.agentscope.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExampleUtils {

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


    public static String getDashScopeApiKey() throws IOException {
        return getApiKey(
                "DASHSCOPE_API_KEY", "DashScope", "https://dashscope.console.aliyun.com/apiKey");
    }

    public static String getOpenAIApiKey() throws IOException {
        return getApiKey("OPENAI_API_KEY", "OpenAI", "https://platform.openai.com/api-keys");
    }

    public static String getApiKey(String envVarName, String serviceName, String helpUrl)
            throws IOException {

        // 1. Try environment variable
        String apiKey = System.getenv(envVarName);

        if (apiKey != null && !apiKey.isEmpty()) {
            System.out.println("✓ Using API key from environment variable " + envVarName + "\n");
            return apiKey;
        }

        // 2. Interactive input
        System.out.println(envVarName + " environment variable not found.\n");
        System.out.println("Please enter your " + serviceName + " API Key:");
        System.out.println("(Get one at: " + helpUrl + ")");
        System.out.print("\nAPI Key: ");

        apiKey = reader.readLine().trim();

        if (apiKey.isEmpty()) {
            System.err.println("Error: API Key cannot be empty");
            System.exit(1);
        }

        System.out.println("\n✓ API Key configured");
        System.out.println("Tip: Set environment variable to skip this step:");
        System.out.println("  export " + envVarName + "=" + maskApiKey(apiKey) + "\n");

        return apiKey;
    }

    public static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }


}
