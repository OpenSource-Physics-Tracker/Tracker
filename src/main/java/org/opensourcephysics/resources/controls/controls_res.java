//package org.opensourcephysics.resources.controls;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.PropertyResourceBundle;
//
///**
// * Default resource loader for the OSP display package.  Resources are obtained from properties.
// *
// * Defining a Java resource class speeds up resource loading, particularly for applets because
// * a connection to the server is not required.
// *
// * @author Wolfgang Christian
//*/
//public class controls_res extends PropertyResourceBundle {
//  // relative path to strings
//  static String res = "resources.controls_res.properties";
//
//  /**
//   * Constructor tools
//   * @throws IOException
//   */
//  public controls_res() throws IOException {
//    this(controls_res.class.getResourceAsStream(res));
//  }
//
//  /**
//   * Constructor tools
//   * @param stream
//   * @throws IOException
//   */
//  public controls_res(InputStream stream) throws IOException {
//    super(stream);
//  }
//
//}