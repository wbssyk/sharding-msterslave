package com.example.sharding.msterslave.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
/**
 * @Method
 * @Author yakun.shi
 * @Description
 * @Return
 * @Date 2019/9/19 17:33
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        //=====添加head参数start============================
//        ParameterBuilder tokenPar = new ParameterBuilder();
//        List<Parameter> pars = new ArrayList<Parameter>();
//        tokenPar.name("Authorization").description("AccessToken令牌").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
//        pars.add(tokenPar.build());
        // =========添加head参数end===================
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.datasource.controller"))
                .paths(PathSelectors.any())
                .build();
//                .globalOperationParameters(pars);
    }


    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("多数据源接口文档")
                .description("多数据源接口文档")
                .termsOfServiceUrl("http://www.google.com.hk")
                .version("1.0")
                .build();
    }

    @Bean
    UiConfiguration uiConfig() {
        return new UiConfiguration(true, false, -1, -1,
                ModelRendering.MODEL, true, DocExpansion.LIST, null, null,
                OperationsSorter.ALPHA, true, TagsSorter.ALPHA, null);
    }

}