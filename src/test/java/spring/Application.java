package spring;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Stream;

import org.h2.tools.Server;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import model.Student;

@SpringBootApplication
public class Application {

    static Properties propsH2 = new Properties();
    static Properties propsH2Api = new Properties();
    static Properties propsMySQL = new Properties();
    static Properties propsMySQLError = new Properties();
    static Properties propsPostgres = new Properties();

    static {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        Arrays.asList(
            new SimpleEntry<Properties, String>(propsH2, "spring/hibernate.h2.properties"), 
//            new SimpleEntry<Properties, String>(propsH2Api, "spring/hibernate.h2-api.properties"), 
            new SimpleEntry<Properties, String>(propsMySQL, "spring/hibernate.mysql.properties"), 
            new SimpleEntry<Properties, String>(propsMySQLError, "spring/hibernate.mysql-5.5.40.properties"), 
            new SimpleEntry<Properties, String>(propsPostgres, "spring/hibernate.postgres.properties")
        ).stream()
            .forEach(simpleEntry -> {
                try (
                    InputStream inputStreamH2 = classloader.getResourceAsStream(simpleEntry.getValue());
                    InputStream inputStreamHibernate = classloader.getResourceAsStream("spring/hibernate.cfg.properties")
                ) {
                    simpleEntry.getKey().load(inputStreamH2);
                    simpleEntry.getKey().load(inputStreamHibernate);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    public static void init() {
        Stream.of(
            propsH2, 
            propsMySQL, 
            propsMySQLError, 
            propsPostgres
        ).forEach(props -> {
            Configuration configuration = new Configuration();
            configuration.addProperties(props).configure("spring/hibernate.cfg.xml");
            configuration.addAnnotatedClass(Student.class);
            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties());
            SessionFactory factory = configuration.buildSessionFactory(builder.build());
            Session session = factory.openSession();
            Transaction transaction = session.beginTransaction();
            Student student = new Student();
            student.setAge(1);
            session.save(student);
            transaction.commit();
            session.close();
            factory.close();
        });
    }

    /**
     * For debug purpose only.
     * @param args
     */
    public static void main(String[] args) {
        
        init();
        
        SpringApplication.run(Application.class, args);
    }
}