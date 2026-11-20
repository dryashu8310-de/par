package com.smartparking.servlet;

import com.smartparking.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet("/api/slots")
public class SlotServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try (
            Connection connection = DBConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet result =
                    statement.executeQuery(
                            "SELECT * FROM parking_slots")
        ) {

            StringBuilder json = new StringBuilder();

            json.append("[");

            boolean first = true;

            while (result.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"id\":")
                    .append(result.getInt("id"))
                    .append(",")

                    .append("\"slotNumber\":\"")
                    .append(result.getString("slot_number"))
                    .append("\",")

                    .append("\"status\":\"")
                    .append(result.getString("status"))
                    .append("\"")
                    .append("}");

                first = false;
            }

            json.append("]");

            out.print(json);

        } catch (Exception e) {

            out.print("""
                    {
                        "success": false,
                        "message": "Unable to load parking slots"
                    }
                    """);

            e.printStackTrace();
        }
    }
}