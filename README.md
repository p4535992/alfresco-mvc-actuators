

Spring Boot like Actuators for Alfresco Content Services
===


* https://github.com/codecentric/spring-boot-admin
* https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html
* https://micrometer.io

Configuration
----
- Default configuration is set up for localhost usage. ACS is on the port 8080 and SBA (Spring Boot Admin) on the port 9595
- In order to change the configuration adapt your alfresco-global properties
- Since security is a broader "theory" that could be setup in many different flavors, this sample comes with basicauth SBA<->Alfresco
- SBA is quite straight forward to be setup with Spring Boot, so please refer to their howto documentation [https://codecentric.github.io/spring-boot-admin/current/#set-up-admin-server](https://codecentric.github.io/spring-boot-admin/current/#set-up-admin-server)

- alfresco-global.properties default configuration
    * #adapt according to your proxy settings
    mvc-actuators.host=http://localhost:8080/alfresco
    * #data to be shown in the info endpoint
    mvc-actuators.info={Environment: 'Development'}
    * #disable or enable SBA registration configuration
     mvc-actuators.sba.enabled=true
    * #adapt according to your proxy settings
    mvc-actuators.sba.host=http://localhost:9595/admin
    * #the application name registered with SBA
    mvc-actuators.sba.application_name=Alfresco
    * #basic auth is used to register with the sba server
    mvc-actuators.sba.username=admin
    mvc-actuators.sba.password=password
    * #basicauth is used by sba to callback alfresco actuators
    mvc-actuators.sba.metadata={'user.name': 'admin', 'user.password':'admin'}

- if you are still using AMPs you can run=> "mvn package -Pamp" and an amp in zip format will be built. Just rename it to .amp
- spring-boot-admin-client-*.jar is actually not a mandatory dependency, if for some reason you only want to use all the available actuators, without registering to SBA
  the entry endpoing is ${mvc-actuators.host}/s/mvc-actuators
  


Supported Alfresco versions
----
v1.0.0-SNAPSHOT
-
- Default configuration is set up for localhost usage. ACS is on the port 8080 and SBA on the port 9595
  in order to enable SBA refer to [https://codecentric.github.io/spring-boot-admin/current/#set-up-admin-server](https://codecentric.github.io/spring-boot-admin/current/#set-up-admin-server) @EnableAdminServer. 
  I recommend to use it with Spring Cloud Gateway and WebFlux/Web Reactive  and a reference Alfresco Authentication implementation is available at [https://github.com/dgradecak/alfresco-jwt-auth](https://github.com/dgradecak/alfresco-jwt-auth). 
  You can enable your SBA server in that application and watch the magic happen :-)
- Tested on Alfresco Community 6.2-GA

