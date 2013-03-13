package com.groceryotg.server;

import com.groceryotg.database.*;
import org.hibernate.Session;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;

/**
 * User: robert
 * Date: 25/01/13
 */

@WebServlet(name = "SetSampleData", urlPatterns = "/SetSampleData")
public class SetSampleData extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Session session = HibernateUtils.getSessionFactory().openSession();
        session.beginTransaction();

        double totalPrice = 2.42;
        double unitPrice = 1.42;

        Flyer flyer = new Flyer("asdf", 1);
        Unit unit = new Unit("kg");
        Category category = new Category("Meat");
        Subcategory subcategory = new Subcategory("top sirloin", "sirloin", category);
        Item item = new Item("top sirloin steak", subcategory);
        Grocery grocery = null;
        try {
            grocery = new Grocery(item, flyer, totalPrice, unitPrice, unit, UpdateGroceryInfo.format.parse("2013-01-27"), UpdateGroceryInfo.format.parse("2013-02-02"),
                    UpdateGroceryInfo.format.parse("2013-01-31"), 12, "top sirloin steak at metro with 1.42/kg and 2.42");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        session.save(flyer);
        session.save(unit);
        session.save(category);
        session.save(subcategory);
        session.save(item);
        session.save(grocery);

        session.getTransaction().commit();
    }
}
