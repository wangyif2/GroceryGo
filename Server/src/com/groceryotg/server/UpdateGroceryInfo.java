package com.groceryotg.server;

import com.google.gson.Gson;
import com.groceryotg.database.Grocery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * User: robert
 * Date: 25/01/13
 */

@WebServlet(name = "UpdateGroceryInfo", urlPatterns = "/UpdateGroceryInfo")
public class UpdateGroceryInfo extends HttpServlet {
    private List<Grocery> groceries = null;
    Gson gson = new Gson();
    final static Logger logger = LoggerFactory.getLogger(UpdateGroceryInfo.class);
    public static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Date requestDate = null;
        try {
            requestDate = (req.getParameter("date").isEmpty()) ? null : format.parse(req.getParameter("date"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        logger.info("requestDate = " + ((requestDate == null) ? "null" : requestDate.toString()));

        groceries = getGroceries(requestDate);

        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");
        out.print(gson.toJson(groceries));
    }

    public List<Grocery> getGroceries(Date requestDate) {
        List<Grocery> gro = null;
        Session session = HibernateUtils.getSessionFactory().openSession();
        session.beginTransaction();

        if (requestDate == null) {
            gro = (List<Grocery>) session.createCriteria(Grocery.class).list();
        } else {
            gro = (List<Grocery>) session.createCriteria(Grocery.class).add(Restrictions.ge("endDate", requestDate)).list();
        }

        session.getTransaction().commit();

        return gro;
    }
}
