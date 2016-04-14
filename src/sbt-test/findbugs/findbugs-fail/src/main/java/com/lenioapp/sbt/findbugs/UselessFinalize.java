package com.lenioapp.sbt.findbugs;

class UselessFinalize {
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }
}