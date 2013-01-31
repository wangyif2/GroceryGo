package com.groceryotg.server;

import com.groceryotg.database.*;
import org.hibernate.Session;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * User: robert
 * Date: 25/01/13
 */

@WebServlet(name = "SetSampleData", urlPatterns = "/SetSampleData")
public class SetSampleData extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("in HibernateUtil init method.....******************");
        Session session = HibernateUtils.getSessionFactory().openSession();
        session.beginTransaction();

        double totalPrice = 2.42;
        double unitPrice = 1.42;

        Store store = new Store("Metro", "downtown kinda", 1, "metro.com");
        Unit unit = new Unit("kg");
        Category category = new Category("Meat");
        Subcategory subcategory = new Subcategory("top sirloin", "sirloin", category);
        Item item = new Item("top sirloin steak", subcategory);
        Grocery grocery = new Grocery(item, store, totalPrice, unitPrice, unit, new Date(), new Date(),
                new Date(), 12, "top sirloin steak at metro with 1.42/kg and 2.42");

        session.save(store);
        session.save(unit);
        session.save(category);
        session.save(subcategory);
        session.save(item);
        session.save(grocery);

        session.getTransaction().commit();
    }
}
