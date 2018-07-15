package eu.europa.ec.cc.zkapachebridge.test.helpers;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TestHealthServlet extends HttpServlet {
    private boolean healthy = true;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String value = req.getParameter("health");
        if (value != null) {
            healthy = ("true".equals(value));
        }
        String result = "";
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println("{\"status\":\"" + (healthy ? "UP" : "DOWN") + "\"}");
    }

}
