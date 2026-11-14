import com.zurrtum.create.client.catnip.render.PonderRenderPipelines;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
public class PipelineTargetCheck {
 public static void main(String[] args) throws Exception {
  int checked=0, failures=0;
  for (var field:PonderRenderPipelines.class.getDeclaredFields()) {
   if(field.getType()!=RenderPipeline.class || !field.getName().contains("ENTITY_BLOCK")) continue;
   var pipeline=(RenderPipeline)field.get(null);
   boolean translucent=field.getName().contains("TRANSLUCENT");
   var targets=pipeline.getColorTargetStates();
   boolean pass=targets.size()==1 && targets.getFirst()!=null && targets.getFirst().blendFunction().isPresent()==translucent;
   System.out.println(field.getName()+" target_count="+targets.size()+" expected_blend="+translucent+" pass="+pass);
   checked++;if(!pass)failures++;
  }
  System.out.println("checked="+checked+" failures="+failures);
  if(checked!=12||failures!=0)throw new AssertionError("Invalid native color target declarations");
 }
}
