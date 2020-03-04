

Spring Boot like Actuators for Alfresco Content Services
===
Spring Boot Actuators help you monitor and manage your application when you push it to production.
This project reuses Spring Boot 2.1.6 (for compatibility with Spring 5.1.8 used by Alfresco).

* https://github.com/codecentric/spring-boot-admin
* https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html
* https://micrometer.io

![Screenshot details](/images/screenshots/health-metrics.png)
*View application health, info and details*

Configuration
----
- Default configuration is set up for localhost usage. ACS is on the port 8080 and SBA (Spring Boot Admin) on the port 9595
- In order to change the configuration adapt your alfresco-global properties
- Since security is a broader "theory" that could be setup in many different flavors, this sample comes with basicauth SBA<->Alfresco
- SBA is quite straight forward to be setup with Spring Boot, so please refer to their howto documentation [https://codecentric.github.io/spring-boot-admin/current/#set-up-admin-server](https://codecentric.github.io/spring-boot-admin/current/#set-up-admin-server)

- alfresco-global.properties default configuration (those properties are subject to change in order to standardize on spring boot)
``` 
mvc-actuators.info.environment=development
mvc-actuators.jolokia.enabled=true

spring.boot.admin.client.enabled=true
spring.boot.admin.client.url=http://localhost:9595/admin
spring.boot.admin.client.username=admin
spring.boot.admin.client.password=password
spring.boot.admin.client.instance.serviceUrl=http://localhost:8080/alfresco
spring.boot.admin.client.instance.name=Alfresco
spring.boot.admin.client.instance.metadata.user.name=admin
spring.boot.admin.client.instance.metadata.user.password=admin
```
    

- if you are still using AMPs you can run=> "mvn package -Pamp" and an amp in zip format will be built. Just rename it to .amp
- spring-boot-admin-client-*.jar is actually not a mandatory dependency, if for some reason you only want to use all the available actuators, without registering to SBA
  the entry endpoint is ${mvc-actuators.host}/s/mvc-actuators
- Default configuration is set up for localhost usage. ACS is on the port 8080 and SBA on the port 9595
  in order to enable SBA refer to [https://codecentric.github.io/spring-boot-admin/current/#set-up-admin-server](https://codecentric.github.io/spring-boot-admin/current/#set-up-admin-server) @EnableAdminServer. 
  I recommend to use it with Spring Cloud Gateway and WebFlux/Web Reactive  and a reference Alfresco Authentication implementation is available at [https://github.com/dgradecak/alfresco-jwt-auth](https://github.com/dgradecak/alfresco-jwt-auth). 
  You can enable your SBA server in that application and watch the magic happen :-)

Docker (Deploy AlfrescoMVC Actuators)
----
Define the SBA server url property on the Alfresco Repository
-
`-Dspring.boot.admin.client.url=http://192.168.100.109:9595/admin`

Build the zip/amp 
-
- clone this repo and execute: mvn clean package -Pamp
- extract the zip /lib folder to the containing folder of your docker-compose.yml (i.e. to ./modules/platform)
- reference those jars in your docker-compose file, under the alfresco container
```services:
       alfresco:
           image: alfresco/alfresco-content-repository:XXX
           volumes: ADD_JARS_HERE
```           
               
on enterprise
-
        volumes:        
            - ./modules/platform/alfresco-mvc-actuators-1.0.0-SNAPSHOT.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/alfresco-mvc-actuators-1.0.0-SNAPSHOT.jar
            - ./modules/platform/alfresco-mvc-rest-7.5.0-RELEASE.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/alfresco-mvc-rest-7.5.0-RELEASE.jar
            - ./modules/platform/jolokia-core-1.6.2.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/jolokia-core-1.6.2.jar
            - ./modules/platform/spring-boot-2.1.6.RELEASE.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/spring-boot-2.1.6.RELEASE.jar
            - ./modules/platform/spring-boot-actuator-2.1.6.RELEASE.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/spring-boot-actuator-2.1.6.RELEASE.jar
            - ./modules/platform/spring-boot-actuator-autoconfigure-2.1.6.RELEASE.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/spring-boot-actuator-autoconfigure-2.1.6.RELEASE.jar
            - ./modules/platform/spring-boot-admin-client-2.2.1.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/spring-boot-admin-client-2.2.1.jar
            - ./modules/platform/spring-boot-autoconfigure-2.1.6.RELEASE.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/spring-boot-autoconfigure-2.1.6.RELEASE.jar
            
on community
-
Add the same ones as above for enterprise and these

        volumes:        
            - ./modules/platform/HdrHistogram-2.1.11.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/HdrHistogram-2.1.11.jar
            - ./modules/platform/LatencyUtils-2.0.3.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/LatencyUtils-2.0.3.jar
            - ./modules/platform/micrometer-core-1.3.5.jar:/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/micrometer-core-1.3.5.jar            
- once started check that you can access http://localhost:8080/alfresco/s/mvc-actuators/ (with your admin account) and do not forget that in the case of started containers you need to do a down and up again in order that the new config is applied

Spring boot Admin server
-
- Deploy your SBA server
- TODO: create a docker image for SBA server

Screenshots
----
![Screenshot application list](/images/screenshots/applications.png)
*Dashboard with desktop notifications*

![Screenshot logfile](/images/screenshots/logfile.png)
*View logfile (with follow)*

![Screenshot environment](/images/screenshots/environment.png)
*View and change Alfresco environment

![Screenshot logging](/images/screenshots/loggers.png)
*Manage logger levels*

![Screenshot jmx](/images/screenshots/jmx.png)
*View and use JMX beans via jolokia*

![Screenshot jmx](/images/screenshots/scheduledtasks.png)
*View cron jobs*

![Screenshot jmx](/images/screenshots/metrics.png)
*View and setup metrics*

![Screenshot jmx](/images/screenshots/beans.png)
*View all beans*

![Screenshot wallboard](/images/screenshots/wallboard.png)
*View history of registered applications*


Supported Alfresco versions
----
v1.0.0-SNAPSHOT
-
- Tested on Alfresco Community 6.2-GA
- should work on 6.2 enterprise (be aware that some jars exist in enterprise: micrometer-core-1.3.5.jar, LatencyUtils-2.0.3.jar, HdrHistogram-2.1.11.jar/HdrHistogram-2.1.10.jar)

TODO
-
- jolokia endpoint config file for bean exposure (whitelist beans, best to be done in alf global properties)
