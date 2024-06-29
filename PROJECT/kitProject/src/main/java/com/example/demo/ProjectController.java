package com.example.demo;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Controller
public class ProjectController {
    String jdbcurl="jdbc:mysql://127.0.0.1:3306/Airline";
    String userId;

    @GetMapping("/hello")
    public String hello(){
        System.out.println("Inside hello method");
        return "login";
    }

    @PostMapping(value = "/submit",produces = "text/html")
    public String login(@RequestParam String button, Model model,@RequestParam("userId") String userId, @RequestParam("password") String password, HttpSession session){
        System.out.println("Inside submit method");
        this.userId= userId;

        if(button.equals("button1")){
            Connection connection =null;
            try {
                connection = DriverManager.getConnection(jdbcurl, "root", "Sneha@12");
                String sql = "select password,usertype from users where userId=?";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1,userId);
                ResultSet rs = statement.executeQuery();
                while(rs.next()){
                    System.out.println("The dbpass is "+rs.getString("password")+" user pass is "+password);
                    if(password.equals(rs.getString("password"))){
                        session.setAttribute("userid", userId);
                        if("admin".equals(rs.getString("usertype")))
                            return "adminDashboard";
                        else
                            return "Dashboard";
                    }else{
                        System.out.println("Wrong credentials");
                        return "alert";
                    }
                }

            }catch (Exception e){
                System.out.println("Exception is "+e);
            }
        }
        return "signup";

    }

    @PostMapping("/signup")
    public String signup(@RequestParam("signupusername") String username, @RequestParam("signupuserId") String userId, @RequestParam("signupuserpassword") String password, @RequestParam("signupuseraddress") String address,Model model){
        System.out.println("Inside signup method");
        System.out.println("The attributes are "+ username+" "+userId+" "+password+" "+address);
       // model.addAttribute("message","The Signup was successful");
        Connection connection =null;
        try{
            connection = DriverManager.getConnection(jdbcurl,"root","Sneha@12");
            String sql= "insert into users values(?,?,?,?,?)";
            PreparedStatement pstatement = connection.prepareStatement(sql);
            pstatement.setString(1,username);
            pstatement.setString(2,userId);
            pstatement.setString(3,password);
            pstatement.setString(4,address);
            pstatement.setString(5,"user");

            pstatement.execute();
            System.out.println("Database updated successfully");
            connection.close();
        }catch(Exception e){
            System.out.println("The exception occured is "+e);
        }
        return "login";
    }


    @GetMapping("/viewFlights")
    public String viewFlights(@RequestParam(name = "viewType", defaultValue = "table") String viewType, Model model){
        List<Map<String,Object>> data = fetchFlight();
        model.addAttribute("flightList",data);
        model.addAttribute("viewType", viewType);
        return "viewFlights";
    }
//return "redirect:/books"; to direct to method with same path in case of reloading this.
    @ModelAttribute("flightList")
    public List<Map<String,Object>> fetchFlight(){
        List<Map<String,Object>> listofflights = new ArrayList<>();
        System.out.println(("The username is "+userId));
        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "Sneha@12")) {
            String sql = "select * from flights";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Map<String,Object> mp = new HashMap<>();
                mp.put("Flightname", rs.getString("flightname"));
                mp.put("Flightnumber", rs.getString("flightnumber"));
                mp.put("FromCity", rs.getString("fromCity"));
                mp.put("ToCity", rs.getString("toCity"));
                mp.put("Date", rs.getDate("date"));
                mp.put("Price", rs.getInt("price"));
                listofflights.add(mp);
            }
            System.out.println("The map data are "+listofflights);
        } catch (SQLException e) {
            System.out.println("The exception occurred: " + e);
        }
        return listofflights;
    }

    @PostMapping("/bookFlight")
    public String bookFlight(@RequestParam(value="selectedRowbutton1",required = false) String selectedRow, @RequestParam(value="selectedRowbutton2",required = false) String selectedRow2, Model model){
        System.out.println("The selected flight is "+selectedRow );
        System.out.println("The selected second row  is "+selectedRow2 );
        String Flightname="";
        String Flightnum = "";
        if(null!=selectedRow) {
            String flightcolumns[] = selectedRow.split(",");
            for (String keyvalue : flightcolumns) {
                //keyvalue="Flightname=Indigo";
                //keyvalue="Flightnumber=6E-857";
                String keyvalarr[] = keyvalue.trim().split("=");
                System.out.println("The keyvalarr" + keyvalarr);
                if (keyvalarr[0].substring(1).equals("Flightname")) {
                    Flightname = keyvalarr[1];
                }
                if (keyvalarr[0].equals("Flightnumber")) {
                    Flightnum = keyvalarr[1];
                }
            }
            System.out.println("The name and number are" + Flightname + " " + Flightnum);
            model.addAttribute("flightnamevjdsnvjdsbj,", Flightname);
            model.addAttribute("flightnumber", Flightnum);
        }
        return "flightBookedAlert";
    }

    @PostMapping("/addFlight")
    public String addFlight(@RequestParam("flightName") String flightName,
                            @RequestParam("flightNumber") String flightNumber,
                            @RequestParam("fromCity") String fromCity,
                            @RequestParam("toCity") String toCity,
                            @RequestParam("date") Date date,
                            @RequestParam("price") int price) {
        System.out.println("Inside addFlight method");
        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "Sneha@12")) {
            String sql = "INSERT INTO flights (flightname, flightnumber, fromCity, toCity, date, price) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, flightName);
            statement.setString(2, flightNumber);
            statement.setString(3, fromCity);
            statement.setString(4, toCity);
            statement.setDate(5, date);
            statement.setInt(6, price);
            statement.executeUpdate();
            System.out.println("Flight added successfully");
        } catch (SQLException e) {
            System.out.println("Exception occurred while adding flight: " + e);
        }
        return "redirect:/viewFlightsByAdmin?viewType=table";
    }

    @GetMapping("AddFlights")
    public String addFlightspage(){
        return "addFlight";
    }

    @GetMapping("UpdateFlights")
    public String updateFlightspage(){
        return "updateFlight";
    }

    @GetMapping("DeleteFlights")
    public String deleteFlightspage(){
        return "deleteFlight";
    }

    @GetMapping("/updateFlight")
    public String showUpdateForm(@RequestParam("flightNumber") String flightNumber, Model model) {
        System.out.println("Inside showUpdateForm method for flight number: " + flightNumber);

        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "Sneha@12")) {
            String sql = "SELECT * FROM flights WHERE flightnumber=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, flightNumber);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                model.addAttribute("flightNumber", rs.getString("flightnumber"));
                model.addAttribute("flightName", rs.getString("flightname"));
                model.addAttribute("fromCity", rs.getString("fromCity"));
                model.addAttribute("toCity", rs.getString("toCity"));
                model.addAttribute("date", rs.getDate("date").toString()); // Convert Date to String
                model.addAttribute("price", rs.getInt("price"));
            }
        } catch (SQLException e) {
            System.out.println("Exception occurred while fetching flight details for update: " + e);
        }

        return "updateFlight";
    }

    @PostMapping("/updateFlight")
    public String updateFlight(@RequestParam("flightNumber") String flightNumber,
                               @RequestParam("flightName") String flightName,
                               @RequestParam("fromCity") String fromCity,
                               @RequestParam("toCity") String toCity,
                               @RequestParam("date") Date date,
                               @RequestParam("price") int price) {
        System.out.println("Inside updateFlight method for flight number: " + flightNumber);

        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "Sneha@12")) {
            String sql = "UPDATE flights SET flightname=?, fromCity=?, toCity=?, date=?, price=? WHERE flightnumber=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, flightName);
            statement.setString(2, fromCity);
            statement.setString(3, toCity);
            statement.setDate(4, date);
            statement.setInt(5, price);
            statement.setString(6, flightNumber);
            statement.executeUpdate();
            System.out.println("Flight updated successfully");
        } catch (SQLException e) {
            System.out.println("Exception occurred while updating flight: " + e);
        }
        return "redirect:/viewFlightsByAdmin?viewType=table";
    }

    @GetMapping("/viewFlightsByAdmin")
    public String viewFlightsByAdmin(@RequestParam(name = "viewType", defaultValue = "table") String viewType, Model model){
        List<Map<String,Object>> data = fetchFlight();
        model.addAttribute("flightList",data);
        model.addAttribute("viewType", viewType);
        return "adminDashboard";
    }

    @PostMapping("/deleteFlight")
    public String deleteFlight(@RequestParam("flightNumber") String flightNumber) {
        System.out.println("Inside deleteFlight method for flight number: " + flightNumber);

        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "Sneha@12")) {
            String sql = "DELETE FROM flights WHERE flightnumber=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, flightNumber);
            statement.executeUpdate();
            System.out.println("Flight deleted successfully");
        } catch (SQLException e) {
            System.out.println("Exception occurred while deleting flight: " + e);
        }

        return "redirect:/viewFlightsByAdmin?viewType=table"; // Redirect back to the admin dashboard
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalidate session to logout user
        return "redirect:/hello"; // Redirect to login page or any other appropriate page
    }
    @GetMapping("/viewBookings")
    public String viewBookings(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userid");
        if (userId == null) {
            // Handle session timeout or unauthorized access
            return "redirect:/hello"; // Redirect to login page or appropriate page
        }

        List<Map<String, Object>> bookings = fetchBookings(userId);
        model.addAttribute("bookingList", bookings);
        return "viewBookings"; // This should correspond to your viewBookings.html template
    }

    private List<Map<String, Object>> fetchBookings(String userId) {
        List<Map<String, Object>> bookings = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcurl, "root", "Sneha@12")) {
            String sql = "SELECT b.bookingID, f.flightname, f.flightnumber, f.fromCity, f.toCity, f.date, f.price " +
                    "FROM bookings b " +
                    "INNER JOIN flights f ON b.flightnumber = f.flightnumber " +
                    "WHERE b.userId = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Map<String, Object> booking = new HashMap<>();
                booking.put("BookingID", rs.getInt("bookingID"));
                booking.put("FlightName", rs.getString("flightname"));
                booking.put("FlightNumber", rs.getString("flightnumber"));
                booking.put("FromCity", rs.getString("fromCity"));
                booking.put("ToCity", rs.getString("toCity"));
                booking.put("Date", rs.getDate("date"));
                booking.put("Price", rs.getInt("price"));
                bookings.add(booking);
            }
        } catch (SQLException e) {
            System.out.println("Exception occurred while fetching bookings: " + e);
        }
        return bookings;
    }







}
