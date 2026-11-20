package com.smartparking.servlet;

import com.smartparking.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null) {
            response.setStatus(400);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Email and password are required.\"}"
            );
            return;
        }

        String sql =
            "SELECT id, full_name, email, phone " +
            "FROM users WHERE email=? AND password=?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, email.trim());
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    int userId = rs.getInt("id");
                    String fullName = rs.getString("full_name");
                    String userEmail = rs.getString("email");
                    String phone = rs.getString("phone");

                    /* CREATE JAVA SESSION */

                    HttpSession session =
                        request.getSession(true);

                    session.setAttribute(
                        "userId",
                        userId
                    );

                    session.setAttribute(
                        "fullName",
                        fullName
                    );

                    session.setAttribute(
                        "email",
                        userEmail
                    );

                    session.setAttribute(
                        "phone",
                        phone
                    );

                    response.setStatus(200);

                    response.getWriter().write(
                        "{"
                        + "\"success\":true,"
                        + "\"id\":" + userId + ","
                        + "\"fullName\":\"" + fullName + "\","
                        + "\"email\":\"" + userEmail + "\","
                        + "\"phone\":\"" + (phone == null ? "" : phone) + "\""
                        + "}"
                    );

                } else {

                    response.setStatus(401);

                    response.getWriter().write(
                        "{\"success\":false,\"message\":\"Invalid email or password.\"}"
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(500);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Database connection error.\"}"
            );
        }
    }
}