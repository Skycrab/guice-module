package com.google.code.guice.jsonconfig;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import javax.validation.Validation;
import javax.validation.Validator;

/**
 * Created by yihaibo on 2019-09-29.
 * 注意使用JsonConfigModule依赖Validator和Jackson ObjectMapper
 *  Validator已提供
 *  如有特殊需求需提供Jackson ObjectMapper，否则guice默认使用无参构造函数实例化
 *
 *  --知识点
 *  如无参构造函数实现不了，可使用OptionalBinder(https://google.github.io/guice/api-docs/4.1/javadoc/com/google/inject/multibindings/OptionalBinder.html)
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
