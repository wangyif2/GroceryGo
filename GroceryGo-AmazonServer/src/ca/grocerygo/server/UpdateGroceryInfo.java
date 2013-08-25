package ca.grocerygo.server;

import ca.grocerygo.database.Grocery;
import com.google.gson.Gson;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
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
        Integer categoryId = null;
        try {
            requestDate = (req.getParameterMap().containsKey("date"))
                    ? ((req.getParameter("date").isEmpty()) ? null : format.parse(req.getParameter("date")))
                    : null;

            categoryId = (req.getParameterMap().containsKey("categoryId"))
                    ? ((req.getParameter("categoryId").isEmpty()) ? null : Integer.parseInt(req.getParameter("categoryId")))
                    : null;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        logger.info("requestDate = " + ((requestDate == null) ? "null" : requestDate.toString()));
        logger.info("categoryId = " + ((categoryId == null) ? "null" : categoryId));

        groceries = getGroceries(requestDate, categoryId);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(groceries));
    }

    public List<Grocery> getGroceries(Date requestDate, Integer categoryId) {
        List<Grocery> gro = null;
        Session session = HibernateUtils.getSessionFactory().openSession();
        session.beginTransaction();
        Date lastestDate = (Date) session.createCriteria(Grocery.class).setProjection(Projections.max("updateDate")).uniqueResult();

        Criteria criteria = session.createCriteria(Grocery.class, "grocery");

        if (requestDate == null) {
            criteria.add(Restrictions.eq("updateDate", lastestDate));
        } else {
            criteria.add(Restrictions.ge("endDate", requestDate)).add(Restrictions.eq("updateDate", lastestDate));
        }

        if (categoryId != null)
            criteria.createAlias("item", "myItem")
                    .createAlias("myItem.subcategory", "mySubcategory")
                    .createAlias("mySubcategory.categoryId", "myCategory")
                    .add(Restrictions.eq("myCategory.categoryId", categoryId));

        gro = (List<Grocery>) criteria.list();

        session.getTransaction().commit();

        return gro;
    }
}
