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

@WebServlet("/api/bookings")
public class BookingServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        /* GET LOGGED-IN USER FROM JAVA SESSION */

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


        /* GET BOOKING DATA */

        String slotNumber =
            request.getParameter("slotNumber");

        String vehicleNumber =
            request.getParameter("vehicleNumber");

        String vehicleType =
            request.getParameter("vehicleType");

        String parkingDate =
            request.getParameter("parkingDate");

        String arrivalTime =
            request.getParameter("arrivalTime");

        String hoursText =
            request.getParameter("parkingHours");


        /* CHECK HOURS */

        if (hoursText == null ||
            hoursText.trim().isEmpty()) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Please select parking hours.\"}"
            );

            return;
        }


        int parkingHours;

        try {

            parkingHours =
                Integer.parseInt(hoursText.trim());

        } catch (NumberFormatException e) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid parking hours.\"}"
            );

            return;
        }


        if (parkingHours < 1 ||
            parkingHours > 6) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid parking hours.\"}"
            );

            return;
        }


        if (slotNumber == null ||
            slotNumber.trim().isEmpty()) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Please select a slot.\"}"
            );

            return;
        }


        if (vehicleNumber == null ||
            vehicleNumber.trim().isEmpty()) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Please enter vehicle number.\"}"
            );

            return;
        }


        if (vehicleType == null ||
            vehicleType.trim().isEmpty()) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Please select vehicle type.\"}"
            );

            return;
        }


        if (parkingDate == null ||
            parkingDate.trim().isEmpty()) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Please select parking date.\"}"
            );

            return;
        }


        if (arrivalTime == null ||
            arrivalTime.trim().isEmpty()) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Please select arrival time.\"}"
            );

            return;
        }


        double amount =
            parkingHours * 50.0;


        Connection con = null;


        try {

            con = DBConnection.getConnection();

            con.setAutoCommit(false);


            /* CHECK USER */

            String userSql =
                "SELECT id FROM users WHERE id=?";

            try (PreparedStatement ps =
                    con.prepareStatement(userSql)) {

                ps.setInt(1, userId);

                try (ResultSet rs =
                        ps.executeQuery()) {

                    if (!rs.next()) {

                        con.rollback();

                        response.setStatus(400);

                        response.getWriter().write(
                            "{\"success\":false,\"message\":\"User not found.\"}"
                        );

                        return;
                    }
                }
            }


            /* CHECK SLOT */

            String slotSql =
                "SELECT status FROM parking_slots " +
                "WHERE slot_number=? FOR UPDATE";

            try (PreparedStatement ps =
                    con.prepareStatement(slotSql)) {

                ps.setString(
                    1,
                    slotNumber.trim()
                );

                try (ResultSet rs =
                        ps.executeQuery()) {

                    if (!rs.next()) {

                        con.rollback();

                        response.setStatus(400);

                        response.getWriter().write(
                            "{\"success\":false,\"message\":\"Parking slot not found.\"}"
                        );

                        return;
                    }


                    String status =
                        rs.getString("status");


                    if (!"AVAILABLE".equalsIgnoreCase(status)) {

                        con.rollback();

                        response.setStatus(400);

                        response.getWriter().write(
                            "{\"success\":false,\"message\":\"This slot is already occupied.\"}"
                        );

                        return;
                    }
                }
            }


            /* INSERT BOOKING */

            String bookingSql =
                "INSERT INTO bookings " +
                "(user_id, slot_number, vehicle_number, " +
                "vehicle_type, parking_date, arrival_time, " +
                "parking_hours, amount, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'BOOKED')";


            try (PreparedStatement ps =
                    con.prepareStatement(bookingSql)) {

                ps.setInt(
                    1,
                    userId
                );

                ps.setString(
                    2,
                    slotNumber.trim()
                );

                ps.setString(
                    3,
                    vehicleNumber.trim()
                );

                ps.setString(
                    4,
                    vehicleType.trim()
                );

                ps.setString(
                    5,
                    parkingDate.trim()
                );

                ps.setString(
                    6,
                    arrivalTime.trim()
                );

                ps.setInt(
                    7,
                    parkingHours
                );

                ps.setDouble(
                    8,
                    amount
                );

                ps.executeUpdate();
            }


            /* CHANGE SLOT TO OCCUPIED */

            String updateSql =
                "UPDATE parking_slots " +
                "SET status='OCCUPIED' " +
                "WHERE slot_number=?";


            try (PreparedStatement ps =
                    con.prepareStatement(updateSql)) {

                ps.setString(
                    1,
                    slotNumber.trim()
                );

                ps.executeUpdate();
            }


            /* SAVE EVERYTHING */

            con.commit();


            response.setStatus(200);

            response.getWriter().write(
                "{"
                + "\"success\":true,"
                + "\"message\":\"Booking successful\","
                + "\"slot\":\""
                + slotNumber
                + "\","
                + "\"amount\":"
                + amount
                + "}"
            );


        } catch (Exception e) {

            e.printStackTrace();

            try {

                if (con != null) {
                    con.rollback();
                }

            } catch (Exception ignored) {
            }


            response.setStatus(500);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Booking database error.\"}"
            );


        } finally {

            try {

                if (con != null) {
                    con.close();
                }

            } catch (Exception ignored) {
            }
        }
    }
}