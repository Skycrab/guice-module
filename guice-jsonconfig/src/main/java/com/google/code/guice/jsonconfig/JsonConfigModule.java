package com.google.code.guice.jsonconfig;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import javax.validation.Validation;
import javax.validation.Validator;

/**
 * Created by yihaibo on 2019-09-29.
 * 注意使用JsonConfigModule需要注入Validator和Jackson ObjectMapper
 *  Validator已提供，使用者需提供Jackson ObjectMapper
 */
public class JsonConfigModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.bind(JsonConfigurator.class).in(Singleton.class);
  }

  @Provides @Singleton
  Validator provideValidator() {
    return Validation.buildDefaultValidatorFactory().getValidator();
  }
}
