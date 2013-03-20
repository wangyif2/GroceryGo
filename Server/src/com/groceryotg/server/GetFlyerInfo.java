package com.groceryotg.server;

import com.google.gson.Gson;
import com.groceryotg.database.Flyer;
import org.hibernate.Session;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * User: robert
 * Date: 15/02/13
 */
@WebServlet(name = "GetFlyerInfo", urlPatterns = "/GetFlyerInfo")
public class GetFlyerInfo extends HttpServlet {
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Flyer> flyers = getFlyers();

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(flyers));
    }

    private List<Flyer> getFlyers() {
        Session session = HibernateUtils.getSessionFactory().openSession();
        session.beginTransaction();

        List<Flyer> flyers = (List<Flyer>) session.createCriteria(Flyer.class).list();
        session.getTransaction().commit();

        return flyers;
    }
}
