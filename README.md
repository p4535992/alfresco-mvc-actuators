

Spring Boot like Actuators for Alfresco Content Services
===


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
    * mvc-actuators.host=http://localhost:8080/alfresco #adapt according to your proxy settings    
    * mvc-actuators.info={Environment: 'Development'} #data to be shown in the info endpoint    
    * mvc-actuators.sba.enabled=true #disable or enable SBA registration configuration     
    * mvc-actuators.sba.host=http://localhost:9595/admin #adapt according to your proxy settings    
    * mvc-actuators.sba.application_name=Alfresco #the application name registered with SBA    
    * mvc-actuators.sba.username=admin #basic auth username is used to register with the sba server
    * mvc-actuators.sba.password=password #basic auth password is used to register with the sba server       
    * mvc-actuators.sba.metadata={'user.name': 'admin', 'user.password':'admin'} #basicauth is used by sba to callback alfresco actuators any other security type can be configured via spring security
    * mvc-actuators.jolokia.enabled=true #expose jolokia
    

- if you are still using AMPs you can run=> "mvn package -Pamp" and an amp in zip format will be built. Just rename it to .amp
- spring-boot-admin-client-*.jar is actually not a mandatory dependency, if for some reason you only want to use all the available actuators, without registering to SBA
  the entry endpoint is ${mvc-actuators.host}/s/mvc-actuators
- Default configuration is set up for localhost usage. ACS is on the port 8080 and SBA on the port 9595
  in order to enable SBA refer to [https://codecentric.github.io/spring-boot-admin/current/#set-up-admin-server](https://codecentric.github.io/spring-boot-admin/current/#set-up-admin-server) @EnableAdminServer. 
  I recommend to use it with Spring Cloud Gateway and WebFlux/Web Reactive  and a reference Alfresco Authentication implementation is available at [https://github.com/dgradecak/alfresco-jwt-auth](https://github.com/dgradecak/alfresco-jwt-auth). 
  You can enable your SBA server in that application and watch the magic happen :-)

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
