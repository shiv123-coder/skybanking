package com.skybanking;

import java.sql.Connection;
import java.sql.Statement;

public class SchemaUpdate {
    public static void main(String[] args) {
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            st.execute("ALTER TABLE loans ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            System.out.println("loans table altered successfully.");
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
