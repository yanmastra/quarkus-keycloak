package io.yanmastra.quarkusProjectCreator;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Application {

    private static String quarkusVersion = "3.15.3";
    private static Scanner scanner = new Scanner(System.in);
    private static final String mvnCreateProject = "mvn io.quarkus.platform:quarkus-maven-plugin:<quarkus-version>:create " +
            "-DprojectGroupId=<groupId> " +
            "-DprojectArtifactId=<artifactId> " +
            "-DwithoutTests";

    public static void main(String[] args) {
        System.out.println("########################");
        System.out.println("##  PROJECT CREATOR   ##");
        System.out.println("########################");

        boolean updateVersion = inputBoolean("\n\nThe last LTS version of quarkus: "+quarkusVersion+"\nIs there any update?");
        if (updateVersion) {
            quarkusVersion = inputString("\nPlease enter the new version of quarkus");
        }
        int option = inputNumber("\nPlease choose what type of project you need to create?\n" +
                "1. Empty Quarkus Project\n" +
                "2. Keycloak Integrated, this project will included the quarkus-authorisation dependencies\n" +
                "3. Rest API, this included CRUD helper dependencies\n" +
                "4. Rest API with Keycloak Integrated, this is combination of number (2) and (3)\n" +
                "5. Rest API with self Authentication system, this is same as number (3) but included a dependency " +
                "that contains some tools to generate secured and encrypted JWT for token authentication\nYour choice?",
                1, 5);
        System.out.println("#######################");
        createQuarkusProject("io.test", "test-a-project");
    }

    public static String inputString(String label) {
        System.out.print(label+"=");
        String input = "";
        try {
            input = scanner.nextLine();
        }catch (InputMismatchException e) {
            System.err.println("\n\nPlease input any text please\n\n");
            return inputString(label);
        } catch (Throwable e) {
            System.err.println("\n\nError: " + e.getMessage() + "\n\n");
            return inputString(label);
        }
        return input;
    }

    public static Boolean inputBoolean(String label) {
        System.out.print(label+"(Y/N)=");
        String input = "";
        try {
            input = scanner.nextLine();
            if (StringUtils.isBlank(input) || !input.equalsIgnoreCase("Y") && !input.equalsIgnoreCase("N")) {
                System.err.println("\n\nPlease input the correct option Y or N!");
                return inputBoolean(input);
            }
            if ("y".equalsIgnoreCase(input)) return true;
            if ("n".equalsIgnoreCase(input)) return false;
        }catch (InputMismatchException e) {
            System.err.println("\n\nPlease input the correct option Y or N!");
            scanner.close();
            scanner = new Scanner(System.in);
            return inputBoolean(label);
        } catch (Throwable e) {
            scanner.close();
            scanner = new Scanner(System.in);
            System.err.println("\n\nError: " + e.getMessage() + "\n\n");
        }
        return inputBoolean(label);
    }

    private static int inputNumber(String label, int min, int max) {
        System.out.print(label+"=");
        String sNumber;
        int number = 0;
        try {
            sNumber = scanner.nextLine();
            number = Integer.parseInt(sNumber);
            if (number > max || number < min)
                throw new Throwable("Please input a number between " + min + " and " + max);
        }catch (NumberFormatException e) {
            System.err.println("\n\nPlease input any number please");
            return inputNumber(label, min, max);
        } catch (Throwable e) {
            System.err.println("\n\nError: " + e.getMessage());
            return inputNumber(label, min, max);
        }
        return number;
    }

    private static boolean createQuarkusProject(String groupId, String artifactId) {
        String command = mvnCreateProject.replaceAll("<quarkus-version>", quarkusVersion);
        command = command.replaceAll("<groupId>", groupId);
        command = command.replaceAll("<artifactId>", artifactId);


        String dir = System.getProperty("user.dir");
        dir = dir.substring(0, dir.lastIndexOf("/"));

        executeCommand(command, dir);
        dir = dir + "/" + artifactId;

        return false;
    }

    private static void executeCommand(String command, String dir) {
        if (StringUtils.isBlank(command)) {
            System.err.println("\n\nPlease input a command please!");
            return;
        }
        try {
            System.out.println(dir + ":" + command);
            String[] commands = command.trim().split(" ");
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(new File(dir)).command(commands);
            Process process = builder.start();

            // Read output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                int exitCode = process.waitFor();
                System.out.println("Exited with code: " + exitCode);
            } catch (Exception e) {
                System.err.println("\n\nError: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
