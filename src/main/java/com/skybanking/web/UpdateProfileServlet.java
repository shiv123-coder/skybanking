package com.skybanking.web;

import com.skybanking.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/updateProfile")
public class UpdateProfileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if(session == null || session.getAttribute("user_id") == null){
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("user_id");
        String action = request.getParameter("action");

        // 1️⃣ Send or Resend OTP
        if("sendOtp".equals(action) || "resendOtp".equals(action)){
            String newEmail = request.getParameter("email");
            if(newEmail == null || newEmail.isEmpty()){
                request.setAttribute("error", "Email is required to send OTP.");
                request.getRequestDispatcher("updateProfile.jsp").forward(request, response);
                return;
            }

            session.setAttribute("profileEmailToVerify", newEmail);

            int otp = 100000 + (int)(Math.random() * 900000);
            session.setAttribute("otp", otp);
            session.setAttribute("otpExpiry", System.currentTimeMillis() + 5*60*1000);
            session.removeAttribute("isOtpVerified");

            try{
                EmailUtil.sendOtp(newEmail, null, otp); // Send to new email
            } catch(Exception e){ e.printStackTrace(); }

            response.sendRedirect("verifyotp.jsp");
            return;
        }

        // 2️⃣ Verify OTP
        else if("verifyOtp".equals(action)){
            String enteredOtp = request.getParameter("otp");
            Integer sessionOtp = (Integer) session.getAttribute("otp");
            Long expiry = (Long) session.getAttribute("otpExpiry");

            if(sessionOtp == null || expiry == null || System.currentTimeMillis() > expiry){
                request.setAttribute("error", "OTP expired! Please resend OTP.");
                request.getRequestDispatcher("verifyotp.jsp").forward(request, response);
            } else if(!sessionOtp.toString().equals(enteredOtp)){
                request.setAttribute("error", "Invalid OTP. Try again.");
                request.getRequestDispatcher("verifyotp.jsp").forward(request, response);
            } else {
                session.setAttribute("isOtpVerified", true);
                session.removeAttribute("otp");
                session.removeAttribute("otpExpiry");

                response.sendRedirect("updateProfile.jsp");
            }
            return;
        }

        // 3️⃣ Update profile
        else if("updateProfile".equals(action)){
            Boolean isOtpVerified = (Boolean) session.getAttribute("isOtpVerified");
            if(isOtpVerified == null || !isOtpVerified){
                request.setAttribute("error", "OTP not verified! Please verify first.");
                request.getRequestDispatcher("updateProfile.jsp").forward(request, response);
                return;
            }

            String fullname = request.getParameter("fullname");
            String username = request.getParameter("username");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");

            try(Connection con = DBConnection.getConnection()){
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE users SET fullname=?, username=?, email=?, phone=? WHERE user_id=?"
                );
                ps.setString(1, fullname);
                ps.setString(2, username);
                ps.setString(3, email);
                ps.setString(4, phone);
                ps.setInt(5, userId);
                ps.executeUpdate();

                session.removeAttribute("isOtpVerified");
                session.removeAttribute("profileEmailToVerify"); // clear temp email
                request.setAttribute("success", "Profile updated successfully!");
            } catch(Exception e){
                e.printStackTrace();
                request.setAttribute("error", "Error updating profile.");
            }

            request.getRequestDispatcher("updateProfile.jsp").forward(request, response);
        }
    }
}
