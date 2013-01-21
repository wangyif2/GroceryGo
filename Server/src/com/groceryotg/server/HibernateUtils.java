package com.groceryotg.server;

import com.groceryotg.database.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * User: robert
 * Date: 20/01/13
 */
public class HibernateUtils extends HttpServlet {
    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;

    private static SessionFactory configureSessionFactory() throws HibernateException {
        Configuration configuration = new Configuration();
        configuration.configure();
        serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        return sessionFactory;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Store store = new Store("Metro", "downtown kinda", 1, "metro.com");
        Unit unit = new Unit("kg");
        Subcategory subcategory = new Subcategory("sirloin", 1, "Meat");
        Item item = new Item("top sirloin steak", subcategory);
        Grocery grocery = new Grocery(store, 2, 1, unit, new Date(2010), new Date(2012), new Date(2013), 14, "top sirloin steak $1/kg $2 total");


        //using the hibernate API
        //create session factory
        SessionFactory sessionFactory = configureSessionFactory(); //read the hibernate configure file and create session factory

        //create session from session factory
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        //use session to save object
        session.save(store);
        session.save(unit);
        session.save(subcategory);
        session.save(item);
        session.save(grocery);
        session.getTransaction().commit();
    }
}
