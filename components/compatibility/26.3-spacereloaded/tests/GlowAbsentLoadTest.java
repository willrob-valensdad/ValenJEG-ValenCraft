public class GlowAbsentLoadTest {
 public static void main(String[] args) throws Exception {
  try {Class.forName("net.irisshaders.iris.Iris");throw new AssertionError("Iris present");}catch(ClassNotFoundException expected){}
  Class<?> c=Class.forName("org.alex_melan.spacereloaded.client.render.GlowGeometry",false,GlowAbsentLoadTest.class.getClassLoader());
  c.getDeclaredMethods();c.getDeclaredFields();
  System.out.println("PASS no-Iris verification/metadata resolution of outer glow helper; graphics initialization not asserted");
 }
}
