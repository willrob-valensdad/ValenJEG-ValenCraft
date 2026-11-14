import java.lang.reflect.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.irisshaders.iris.shaderpack.materialmap.*;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.layer.RenderingWrapper;
public class GlowWrapperTest {
 public static void main(String[] args) throws Exception {
  Class<?> type=Class.forName("org.alex_melan.spacereloaded.client.render.GlowGeometry$IrisGlow");
  var constructor=type.getDeclaredConstructor();constructor.setAccessible(true);
  var wrapper=(RenderingWrapper)constructor.newInstance();
  var settings=WorldRenderingSettings.INSTANCE;
  var state=CapturedRenderingState.INSTANCE;
  if(settings.getEntityIds()!=null)throw new AssertionError("Expected native initial null map");state.setCurrentEntity(37);wrapper.setup();wrapper.clear();
  if(state.getCurrentRenderedEntity()!=37)throw new AssertionError("no-shader state changed");
  var ids=new Object2IntOpenHashMap<NamespacedId>();ids.put(new NamespacedId("spacereloaded","rocket"),10104);ids.put(new NamespacedId("minecraft","lightning_bolt"),10102);
  settings.setEntityIds(ids);
  wrapper.setup();if(state.getCurrentRenderedEntity()!=10104)throw new AssertionError("wrong producer at draw");
  wrapper.clear();if(state.getCurrentRenderedEntity()!=37)throw new AssertionError("previous producer not restored");
  ids.put(new NamespacedId("spacereloaded","rocket"),12345);
  wrapper.setup();if(state.getCurrentRenderedEntity()!=12345)throw new AssertionError("stale shader mapping");
  wrapper.clear();if(state.getCurrentRenderedEntity()!=37)throw new AssertionError("restore failed after mapping change");
  System.out.println("PASS actual Iris wrapper: null-map no-op, draw mapping, previous-ID restore, fresh mapping after change");
 }
}
