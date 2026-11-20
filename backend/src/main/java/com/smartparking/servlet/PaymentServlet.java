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

@WebServlet("/api/payment")
public class PaymentServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            response.setStatus(401);

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Please login again.\"}"
            );

            return;
        }

        int userId =
                (Integer) session.getAttribute("userId");

        String sql =
                "SELECT id, slot_number, vehicle_number, amount, status " +
                "FROM bookings " +
                "WHERE user_id=? " +
                "ORDER BY id DESC LIMIT 1";

        try (
                Connection con =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, userId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {

                    int bookingId =
                            rs.getInt("id");

                    String slot =
                            rs.getString("slot_number");

                    String vehicle =
                            rs.getString("vehicle_number");

                    double amount =
                            rs.getDouble("amount");

                    String status =
                            rs.getString("status");

                    response.getWriter().write(
                            "{"
                            + "\"success\":true,"
                            + "\"bookingId\":" + bookingId + ","
                            + "\"slot\":\"" + slot + "\","
                            + "\"vehicle\":\"" + vehicle + "\","
                            + "\"amount\":" + amount + ","
                            + "\"status\":\"" + status + "\""
                            + "}"
                    );

                } else {

                    response.setStatus(404);

                    response.getWriter().write(
                            "{\"success\":false,\"message\":\"No booking found.\"}"
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(500);

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Payment database error.\"}"
            );
        }
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            response.setStatus(401);

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Please login again.\"}"
            );

            return;
        }

        int userId =
                (Integer) session.getAttribute("userId");

        String paymentMethod =
                request.getParameter("paymentMethod");

        if (paymentMethod == null ||
                paymentMethod.trim().isEmpty()) {

            response.setStatus(400);

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Please select payment method.\"}"
            );

            return;
        }

        String sql =
                "UPDATE bookings " +
                "SET status='PAID' " +
                "WHERE id=(" +
                "SELECT id FROM (" +
                "SELECT id FROM bookings " +
                "WHERE user_id=? " +
                "ORDER BY id DESC LIMIT 1" +
                ") AS temp" +
                ")";

        try (
                Connection con =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, userId);

            int updated =
                    ps.executeUpdate();

            if (updated > 0) {

                response.getWriter().write(
                        "{"
                        + "\"success\":true,"
                        + "\"message\":\"Payment successful\""
                        + "}"
                );

            } else {

                response.setStatus(404);

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Booking not found.\"}"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(500);

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Payment failed.\"}"
            );
        }
    }
}