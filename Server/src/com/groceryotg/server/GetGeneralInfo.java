package com.groceryotg.server;

import com.google.gson.Gson;
import com.groceryotg.database.Category;
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
 * Date: 24/01/13
 */

@WebServlet(name = "GetGeneralInfo", urlPatterns = "/GetGeneralInfo")
public class GetGeneralInfo extends HttpServlet {
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Category> categories = getCategories();

        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");
        out.print(gson.toJson(categories));
    }

    private List<Category> getCategories() {
        Session session = HibernateUtils.getSessionFactory().openSession();
        session.beginTransaction();

        List<Category> categories = (List<Category>) session.createCriteria(Category.class).list();
        session.getTransaction().commit();

        return categories;
    }
}
