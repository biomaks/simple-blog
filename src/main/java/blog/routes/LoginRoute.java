package blog.routes;

import blog.BlogController;
import blog.dao.SessionDAO;
import blog.dao.UserDAO;
import blog.logic.ConfigParser;
import blog.models.User;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import spark.ModelAndView;
import spark.Request;

import javax.servlet.http.Cookie;
import java.io.IOException;

import static spark.Spark.get;
import static spark.Spark.post;


public class LoginRoute extends BaseRoute {
    private Configuration cfg;
    private UserDAO userDAO;
    private SessionDAO sessionDAO;
    private ConfigParser configParser;

    Logger logger = LogManager.getLogger(LoginRoute.class.getName());

    public LoginRoute(final Configuration cfg, final Session session) {
        this.cfg = cfg;
        this.userDAO = new UserDAO(session);
        this.sessionDAO = new SessionDAO(session);
        this.configParser = new ConfigParser();
    }

    public void initPage() throws IOException {
        get("/login", (request, response) -> {
            logger.info(request.requestMethod() + " " + request.headers("Referer"));
            SimpleHash root = new SimpleHash();
            String cookie = BlogController.getSessionCookie(request);
            String username = sessionDAO.findUserNameBySessionString(cookie);
            if (username != null) {
                root.put("authorized", "true");
            } else {
                root.put("authorized", "false");
            }
            return new ModelAndView(root, "login.ftl");
        }, new FreemarkerTemplateEngine(cfg));

        post("/login", (request, response) -> {
            String username = request.queryParams("username");
            String password = request.queryParams("password");
            logger.info(request.requestMethod() + " " + request.headers("Referer"));

            logger.info("Login: User submitted: " + username + "  " + "***********");
            User user = userDAO.validateLogin(username, password);
            if (user != null) {
                String sessionID = sessionDAO.startSession(user.getUsername());

                if (sessionID == null) {
                    response.redirect("/internal_error");
                } else {
                    logger.info("User " + username + " authorized");
                    // set the cookie for the user's browser
                    Cookie cookie = new Cookie("session", sessionID);
                    //read session max age
                    int sessionTime = configParser.getSessionMaxAge();
                    cookie.setMaxAge(sessionTime);
                    response.raw().addCookie(cookie);
                    response.redirect("/");
                    return null;
                }
            } else {
                SimpleHash root = new SimpleHash();
                root.put("authorized", "false");
                root.put("username", StringEscapeUtils.escapeHtml4(username));
                root.put("password", "");
                root.put("login_error", "error");
                return new ModelAndView(root, "login.ftl");
            }
            return user.getId();
        });

        post("/logout", (request, response) -> {
            logger.info(request.requestMethod().toUpperCase() + " " + request.headers("User-Agent"));
            String sessionId = BlogController.getSessionCookie(request);
            if (sessionId == null) {
                response.redirect("/login");
            } else {
                sessionDAO.endSession(sessionId);
                Cookie c = getSessionCookieActual(request);
                c.setMaxAge(0);
                response.raw().addCookie(c);
                response.redirect("/login");
            }
            return null;
        });

        get("/logout", (request, response) -> {
            logger.info(request.requestMethod().toUpperCase() + " " + request.headers("User-Agent"));
            String sessionId = BlogController.getSessionCookie(request);
            if (sessionId == null) {
                response.redirect("/login");
            } else {
                sessionDAO.endSession(sessionId);
                Cookie c = getSessionCookieActual(request);
                c.setMaxAge(0);
                response.raw().addCookie(c);
                response.redirect("/login");
            }
            return null;
        });


        get("/signup", (request, response) -> {
            logger.info(request.requestMethod().toUpperCase() + " " + request.headers("Host") + " " + request.headers("User-Agent"));

            SimpleHash root = new SimpleHash();
            root.put("blogName", blogName);
            return new ModelAndView(root, "signup.ftl");
        }, new FreemarkerTemplateEngine(cfg));

        post("/signup", (request, response) -> {
            logger.info(request.requestMethod().toUpperCase() + " " + request.headers("Host") + " " + request.headers("User-Agent"));
            SimpleHash root = new SimpleHash();
            String username = request.queryParams("username");
            String password1 = request.queryParams("password_1");
            String password2 = request.queryParams("password_2");
            String email = request.queryParams("email");

            if (!password1.equals(password2)) {
                root.put("error", "passwords are not equal");
                logger.warn("passwords are not equal");
                return new ModelAndView(root, "signup.ftl");
            } else if (username.trim().equals("") || username.length() < 3) {
                root.put("error", "username must contain at least 3 chars");
                logger.warn("username must contain at least 3 chars");
                return new ModelAndView(root, "signup.ftl");
            } else if (!userDAO.addUser(username, password1, email)) {
                root.put("error", "username already in use");
                logger.warn("username already in use");
                return new ModelAndView(root, "signup.ftl");
            } else {
                // good user, let's start a session
                String sessionID = sessionDAO.startSession(username);
                logger.info("Session ID is" + sessionID);
                response.raw().addCookie(new Cookie("session", sessionID));
                response.redirect("/");
                return null;
            }
        }, new FreemarkerTemplateEngine(cfg));
    }

    // helper function to get session cookie as string
    private Cookie getSessionCookieActual(final Request request) {
        if (request.raw().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("session")) {
                return cookie;
            }
        }
        return null;
    }


}
