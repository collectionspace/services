package org.collectionspace.authentication.spring;

import org.springframework.web.cors.CorsConfiguration;

public class CSpaceCorsConfiguration extends CorsConfiguration {
    @Override
    public String checkOrigin(String origin) {
      System.out.println("origin=[" + origin + "]");

      return super.checkOrigin(origin);
    }
}
