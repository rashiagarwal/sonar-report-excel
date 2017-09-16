package org.sonarqube.utility;

import org.apache.log4j.Logger;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Connection {

  private static final Logger logger = Logger.getLogger("Connection Connectivity ");

  public Retrofit create() {
    try {
      String url = System.getProperty("url");

      return new Retrofit.Builder().baseUrl(url)
          .addConverterFactory(GsonConverterFactory.create())
          .build();
    } catch (NullPointerException | IllegalArgumentException ex) {
      logger.error(ex.getMessage());
    }
    return null;
  }
}