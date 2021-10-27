package cn.objectspace;

import com.google.common.flogger.FluentLogger;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@ComponentScan(basePackages = {"br.com", "com", "cn.objectspace"})
public class WebSSHStarter {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Value("${webssh.first.session-name}")
    private String firstSessionName;

    public static void main(String[] args) {
        logger.atInfo().log("Accessing Command-Line Arguments...");
        if (args.length == 0 || args.length > 1) {
            logger.atInfo().log("Unreported arguments or more than one disallowed argument.\n"
                    + "\t\t --bd \t-IP of the database\n"
                    /*+ "\t\t --u \t-Default user\n"
                    + "\t\t --p \t-Default user password\n"*/);
        } else {
            if (!args[0].contains("--bd")) {
                logger.atInfo().log("--bd (IP of the database.) parameter must be entered...");
            } else {
                logger.atInfo().log("Argument read %s ", args[0]);
                SpringApplication.run(WebSSHStarter.class, args);
            }
        }
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "webssh.first")
    public DataSourceProperties firstDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("webssh.first.configuration")
    public HikariDataSource firstDataSource() {
        return firstDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean firstFactoryBean() {
        LocalContainerEntityManagerFactoryBean factory
                = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(firstDataSource());
        EclipseLinkJpaVendorAdapter eclipseLinkJpaVendorAdapter = new EclipseLinkJpaVendorAdapter();
        eclipseLinkJpaVendorAdapter.setShowSql(true);
        eclipseLinkJpaVendorAdapter.setGenerateDdl(false);
        factory.setJpaVendorAdapter(eclipseLinkJpaVendorAdapter);
        factory.setPersistenceUnitName("intranet");
        factory.setJpaProperties(defaultJpaProperties(firstSessionName));
        return factory;
    }

    @Bean(value = "transactionManager")
    public PlatformTransactionManager firstTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(firstFactoryBean().getObject());
        return transactionManager;
    }

    private String detectWeavingMode() {
        return InstrumentationLoadTimeWeaver.isInstrumentationAvailable() ? "true" : "static";
    }

    private Properties defaultJpaProperties(String sessionName) {
        Properties properties = new Properties();
        properties.setProperty(PersistenceUnitProperties.WEAVING, detectWeavingMode());
        properties.setProperty(PersistenceUnitProperties.SESSION_NAME, sessionName);
        properties.setProperty(PersistenceUnitProperties.LOGGING_EXCEPTIONS, "true");
        properties.setProperty(PersistenceUnitProperties.LOGGING_SESSION, "true");
        properties.setProperty(PersistenceUnitProperties.LOGGING_PARAMETERS, "true");
        properties.setProperty(PersistenceUnitProperties.NATIVE_SQL, "true");
        return properties;
    }

    public String getFirstSessionName() {
        return firstSessionName;
    }

    public void setFirstSessionName(String firstSessionName) {
        this.firstSessionName = firstSessionName;
    }
}
