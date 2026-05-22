package com.skybanking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class NotificationCleanup {
    public static void main(String[] args) {
        try {
            Connection con = DBConnection.getConnection();
            
            // Delete empty/corrupted notifications
            String deleteSql = "DELETE FROM notifications WHERE title IS NULL OR title = '' OR message IS NULL OR message = ''";
            try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                int rowsAffected = ps.executeUpdate();
                System.out.println("Cleaned up " + rowsAffected + " empty/corrupted notifications.");
            }
            
            con.close();
            System.out.println("Database cleanup successful.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
