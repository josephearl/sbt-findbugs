package uk.co.josephearl.sbt.findbugs;

class UselessFinalize {
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }
}