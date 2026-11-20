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
import java.sql.ResultSet;

@WebServlet("/api/cancel-booking")
public class CancelBookingServlet extends HttpServlet {

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String slotNumber =
                request.getParameter("slotNumber");

        if (slotNumber == null ||
            slotNumber.trim().isEmpty()) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Slot number is required\"}"
            );

            return;
        }


        Connection con = null;

        try {

            con = DBConnection.getConnection();

            con.setAutoCommit(false);


            /*
             * Find booking for this slot
             */

            String findSql =
                    "SELECT id FROM bookings " +
                    "WHERE slot_number=? " +
                    "AND status='BOOKED' " +
                    "ORDER BY id DESC LIMIT 1";


            int bookingId = -1;


            try (PreparedStatement ps =
                    con.prepareStatement(findSql)) {

                ps.setString(1, slotNumber);

                ResultSet rs =
                        ps.executeQuery();


                if (rs.next()) {

                    bookingId =
                            rs.getInt("id");

                }

            }


            /*
             * No booking found
             */

            if (bookingId == -1) {

                /*
                 * Still make slot available
                 */

                String updateSlot =
                        "UPDATE parking_slots " +
                        "SET status='AVAILABLE' " +
                        "WHERE slot_number=?";


                try (PreparedStatement ps =
                        con.prepareStatement(updateSlot)) {

                    ps.setString(1, slotNumber);

                    ps.executeUpdate();

                }


                con.commit();


                response.getWriter().write(
                    "{\"success\":true,\"message\":\"Slot is now available\"}"
                );

                return;
            }


            /*
             * Delete booking
             */

            String deleteSql =
                    "DELETE FROM bookings " +
                    "WHERE id=?";


            try (PreparedStatement ps =
                    con.prepareStatement(deleteSql)) {

                ps.setInt(1, bookingId);

                ps.executeUpdate();

            }


            /*
             * Make parking slot available
             */

            String updateSql =
                    "UPDATE parking_slots " +
                    "SET status='AVAILABLE' " +
                    "WHERE slot_number=?";


            try (PreparedStatement ps =
                    con.prepareStatement(updateSql)) {

                ps.setString(1, slotNumber);

                ps.executeUpdate();

            }


            /*
             * Commit
             */

            con.commit();


            response.getWriter().write(
                "{\"success\":true,\"message\":\"Booking cancelled successfully\"}"
            );

        }

        catch (Exception e) {

            try {

                if (con != null) {
                    con.rollback();
                }

            }

            catch (Exception ignored) {
            }


            e.printStackTrace();


            response.setStatus(500);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Cancellation failed\"}"
            );

        }

        finally {

            try {

                if (con != null) {
                    con.close();
                }

            }

            catch (Exception ignored) {
            }

        }

    }

}