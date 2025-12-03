package com.observatory.observatorysystem.client;

public class SessionContext {
    private static String currentUsername;
    private static String currentRole;
    private static String currentFullName;
    private static Long currentUserId;

    public static void login(String username, String role, String fullName, Long userId) {
        currentUsername = username;
        currentRole = role;
        currentFullName = fullName;
        currentUserId = userId;
        System.out.println("SessionContext: User logged in - " + username + ", ID: " + userId);
    }

    public static void logout() {
        currentUsername = null;
        currentRole = null;
        currentFullName = null;
        currentUserId = null;
    }

    public static String getCurrentUsername() { return currentUsername; }
    public static String getCurrentRole() { return currentRole; }
    public static String getCurrentFullName() { return currentFullName; }
    public static Long getCurrentUserId() {
        if (currentUserId == null) {
            System.out.println("WARNING: currentUserId is null!");
        }
        return currentUserId;
    }
    public static boolean isAdmin() { return "ADMIN".equals(currentRole); }
    public static boolean isLoggedIn() { return currentUsername != null; }
}