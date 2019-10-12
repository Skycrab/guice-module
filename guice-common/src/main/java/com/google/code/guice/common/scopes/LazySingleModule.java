package com.google.code.guice.common.scopes;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Created by yihaibo on 2019-10-08.
 */
public class LazySingleModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.bindScope(LazySingleton.class, CommonScopes.SINGLETON);
  }
}
