package uk.co.josephearl.sbt.findbugs;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class PotentialLostLoggerExperimental {
  public static void initLogging() throws Exception {
    Logger logger = Logger.getLogger("edu.umd.cs");
    logger.addHandler(new FileHandler());
    logger.setUseParentHandlers(false);
  }
}
