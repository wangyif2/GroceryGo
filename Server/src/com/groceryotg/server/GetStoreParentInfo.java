package com.groceryotg.server;

import com.google.gson.Gson;
import com.groceryotg.database.StoreParent;
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
@WebServlet(name = "GetStoreParentInfo", urlPatterns = "/GetStoreParentInfo")
public class GetStoreParentInfo extends HttpServlet {
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<StoreParent> storeParents = getStoreParents();

        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");
        out.print(gson.toJson(storeParents));
    }

    private List<StoreParent> getStoreParents() {
        Session session = HibernateUtils.getSessionFactory().openSession();
        session.beginTransaction();

        List<StoreParent> storeParents = (List<StoreParent>) session.createCriteria(StoreParent.class).list();
        session.getTransaction().commit();

        return storeParents;
    }
}
