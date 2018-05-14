package main.java.servlets;
import main.java.tasks.DistanceService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DistanceServiceServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int distance = Integer.parseInt(req.getParameter("distance"));
        int countWays = DistanceService.countWaysToCoverDistance(distance);
        System.out.println(countWays);
        req.setAttribute("output", countWays);
        req.getRequestDispatcher("/jsp/task7.jsp").forward(req, resp);
    }
}
