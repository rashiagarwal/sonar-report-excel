package org.sonarqube;

import dagger.Module;
import dagger.Provides;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sonarqube.server.SonarServer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.inject.Singleton;

@Module(library = true, injects = {SonarServer.class})
public class SonarReportModule {
//
//  @Provides
//  Connection provideSonar() {
//    return new Connection();
//  }

  @Provides
  @Singleton
  public Retrofit retrofit() {
    try {
      String url = System.getProperty("url");

      return new Retrofit.Builder().baseUrl(url)
          .addConverterFactory(GsonConverterFactory.create())
          .build();
    } catch (NullPointerException | IllegalArgumentException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  @Provides
  @Singleton
  XSSFWorkbook provideXSSFWorkbook() {
    return new XSSFWorkbook();
  }
}
