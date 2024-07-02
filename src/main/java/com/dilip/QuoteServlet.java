package com.dilip;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class QuoteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String searchTerm = request.getParameter("searchTerm");
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            System.out.println("Loading JDBC driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("JDBC driver loaded successfully.");

            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/quotes", "root", "Dhileepan@050297");
            System.out.println("Connected to the database successfully.");

            conn.setAutoCommit(false);

            if (searchTerm != null && !searchTerm.isEmpty()) {
                pstmt = conn.prepareStatement("SELECT * FROM quotes WHERE authors LIKE ?");
                pstmt.setString(1, "%" + searchTerm + "%");
            } else {
                pstmt = conn.prepareStatement("SELECT * FROM quotes ORDER BY RAND() LIMIT 1");
            }
            rs = pstmt.executeQuery();

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            if (rs.next()) {
                String quotes = rs.getString("quotes");
                String authors = rs.getString("authors");
                response.getWriter().write("{\"quotes\":\"" + quotes + "\",\"authors\":\"" + authors + "\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Author not found\"}");
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"SQLException: " + e.getMessage() + "\"}");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"ClassNotFoundException: " + e.getMessage() + "\"}");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
