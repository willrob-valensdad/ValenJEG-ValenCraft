import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Build a distinct observed fixture JAR. No observed class enters timed candidates. */
public class RailObserve {
 static final String BASE="com/zurrtum/create/content/trains/graph/";
 static final String HOOK=BASE+"RailObserveHooks";
 static MethodInsnNode call(String name,String desc){return new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,name,desc,false);}
 static byte[] observe(String name,byte[] bytes) {
  ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);
  for(MethodNode m:c.methods) {
   if(name.equals("TrackGraph")||name.equals("ValenRailIntersections")||name.equals("TrackEdgeIntersection")) {
    for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode x&&x.getOpcode()==Opcodes.INVOKESTATIC
      &&x.owner.equals("java/util/UUID")&&x.name.equals("randomUUID")&&x.desc.equals("()Ljava/util/UUID;")){x.owner=HOOK;x.name="uuid";}
   }
   InsnList code=new InsnList();
   if(name.equals("TrackEdge")&&m.name.equals("getIntersection")) {
    code.add(new VarInsnNode(Opcodes.ALOAD,4));code.add(call("geometry","(L"+BASE+"TrackNode;)V"));
   }
   if(name.equals("EdgeData")&&m.name.equals("addIntersection")) {
    code.add(new VarInsnNode(Opcodes.ALOAD,1));code.add(new VarInsnNode(Opcodes.ALOAD,2));code.add(new VarInsnNode(Opcodes.DLOAD,3));
    code.add(new VarInsnNode(Opcodes.ALOAD,5));code.add(new VarInsnNode(Opcodes.ALOAD,6));code.add(new VarInsnNode(Opcodes.DLOAD,7));
    code.add(call("commit","(L"+BASE+"TrackGraph;Ljava/util/UUID;DL"+BASE+"TrackNode;L"+BASE+"TrackNode;D)V"));
   }
   if(name.equals("TrackGraph")&&m.name.equals("deferIntersectionUpdate")) {
    code.add(new VarInsnNode(Opcodes.ALOAD,1));code.add(call("defer","(L"+BASE+"TrackEdge;)V"));
   }
   if(name.equals("TrackGraph")&&m.name.equals("markDirty"))code.add(call("dirty","()V"));
   if(name.equals("TrackGraph")&&m.name.equals("putConnection")||name.equals("TrackGraphSync")&&m.name.equals("edgeAdded")) {
    int start=name.equals("TrackGraph")?1:2;
    code.add(new LdcInsnNode(name.equals("TrackGraph")?"put":"sync"));
    code.add(new VarInsnNode(Opcodes.ALOAD,start));code.add(new VarInsnNode(Opcodes.ALOAD,start+1));
    code.add(call("publish","(Ljava/lang/String;L"+BASE+"TrackNode;L"+BASE+"TrackNode;)V"));
   }
   if(code.size()>0)m.instructions.insert(code);
  }
  ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);return w.toByteArray();
 }
 public static void main(String[] args)throws Exception {
  Set<String> types=Set.of("TrackGraph","TrackEdge","TrackEdgeIntersection","EdgeData","TrackGraphSync","ValenRailIntersections");
  try(ZipFile in=new ZipFile(args[0]);ZipOutputStream out=new ZipOutputStream(Files.newOutputStream(Path.of(args[1]),StandardOpenOption.CREATE_NEW))) {
   for(ZipEntry e:Collections.list(in.entries())) {
    byte[] data=in.getInputStream(e).readAllBytes();String n=e.getName();
    if(n.startsWith(BASE)&&n.endsWith(".class")&&types.contains(n.substring(BASE.length(),n.length()-6)))data=observe(n.substring(BASE.length(),n.length()-6),data);
    ZipEntry copy=new ZipEntry(n);copy.setTime(e.getTime());out.putNextEntry(copy);out.write(data);out.closeEntry();
   }
  }
 }
}
