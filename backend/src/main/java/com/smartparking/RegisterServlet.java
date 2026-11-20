package com.smartparking.servlet;

import com.smartparking.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");

        if (fullName == null || fullName.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Please fill all required fields\"}"
            );

            return;
        }

        String sql =
            "INSERT INTO users " +
            "(full_name, email, phone, password) " +
            "VALUES (?, ?, ?, ?)";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, fullName.trim());
            ps.setString(2, email.trim());
            ps.setString(3, phone == null ? "" : phone.trim());
            ps.setString(4, password);

            ps.executeUpdate();

            response.setStatus(200);

            response.getWriter().write(
                "{\"success\":true,\"message\":\"Registration successful\"}"
            );

        } catch (SQLException e) {

            e.printStackTrace();

            if (e.getErrorCode() == 1062) {

                response.setStatus(409);

                response.getWriter().write(
                    "{\"success\":false,\"message\":\"This email is already registered. Please use another email or login.\"}"
                );

            } else {

                response.setStatus(500);

                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Database error. Please check MySQL connection.\"}"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(500);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Java backend error. Please check Tomcat.\"}"
            );
        }
    }
}