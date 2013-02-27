package com.groceryotg.server;

import com.google.gson.Gson;
import com.groceryotg.database.Store;
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
@WebServlet(name = "GetStoreInfo", urlPatterns = "/GetStoreInfo")
public class GetStoreInfo extends HttpServlet {
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Store> stores = getStores();

        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");
        out.print(gson.toJson(stores));
    }

    private List<Store> getStores() {
        Session session = HibernateUtils.getSessionFactory().openSession();
        session.beginTransaction();

        List<Store> stores = (List<Store>) session.createCriteria(Store.class).list();
        session.getTransaction().commit();

        return stores;
    }
}
