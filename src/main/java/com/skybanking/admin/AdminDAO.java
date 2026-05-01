package com.skybanking.admin;

import com.skybanking.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AdminDAO {

    /**
     * Fetch admin profile (name + email)
     */
    public static Map<String, Object> getAdminProfile() throws Exception {
        Map<String, Object> profile = new HashMap<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT full_name, email FROM admin WHERE id = 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    profile.put("fullName", rs.getString("full_name"));
                    profile.put("email", rs.getString("email"));
                }
            }
        }
        return profile;
    }

    /**
     * Update admin profile (name + email)
     */
    public static void updateProfile(String fullName, String email) throws Exception {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE admin SET full_name = ?, email = ? WHERE id = 1")) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    /**
     * Change password (verify current password first)
     */
    public static boolean changePassword(String currentPassword, String newPassword) throws Exception {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT password FROM admin WHERE id = 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (!storedPassword.equals(currentPassword)) {
                        return false; // current password incorrect
                    }
                }
            }
        }

        // Update to new password
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE admin SET password = ? WHERE id = 1")) {
            ps.setString(1, newPassword);
            ps.executeUpdate();
        }
        return true;
    }

    /**
     * Fetch all system settings as key-value pairs
     */
    public static Map<String, String> getSystemSettings() throws Exception {
        Map<String, String> settings = new HashMap<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT setting_key, setting_value FROM system_settings")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
                }
            }
        }
        return settings;
    }

    /**
     * Update a single system setting
     */
    public static void updateSystemSetting(String key, String value) throws Exception {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE system_settings SET setting_value = ? WHERE setting_key = ?")) {
            ps.setString(1, value);
            ps.setString(2, key);
            ps.executeUpdate();
        }
    }

    /**
     * Update multiple system settings in one go
     */
    public static void updateSystemSettings(Map<String, String> newSettings) throws Exception {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE system_settings SET setting_value = ? WHERE setting_key = ?")) {

            for (Map.Entry<String, String> entry : newSettings.entrySet()) {
                ps.setString(1, entry.getValue());
                ps.setString(2, entry.getKey());
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }
}
