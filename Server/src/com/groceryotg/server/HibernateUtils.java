package com.groceryotg.server;

import com.groceryotg.database.Grocery;
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
        Grocery grocery = new Grocery("Vegie");

        //using the hibernate API
        //create session factory
        SessionFactory sessionFactory = configureSessionFactory(); //read the hibernate configure file and create session factory

        //create session from session factory
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        //use session to save object
        session.save(grocery);
        session.getTransaction().commit();
    }
}
