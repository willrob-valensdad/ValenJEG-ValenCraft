package ttv.migami.jeg.worldgen.structure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
/** Source parameter/origin/rotation checks only; full world terrain/shaft/access are separate. */
public final class MinipurgePlacementNativeContracts {
 private static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  int cases=0;
  var old=HbmAuthoredStructure.Profile.values();
  check(old[0].getSerializedName().equals("falloutshelter_house")&&old[0].offset==-8&&old[0].min==1&&old[0].max==128,"Old shelter profile changed");
  check(old[1].offset==-5&&old[1].min==58&&old[1].max==67,"Old patrol profile changed");
  check(old[2].offset==-20&&old[2].min==11&&old[2].max==12,"Old rig profile changed");
  check(old[3].offset==-10&&old[3].min==1&&old[3].max==128,"Old submarine profile changed");
  check(old[4]==HbmAuthoredStructure.Profile.MINIPURGE_BUNKER&&old[4].getSerializedName().equals("minipurge_bunker"),"Native source profile/codec name differs");cases+=5;
  for(int air=-64;air<=320;air++)for(int depth:new int[]{24,25}){
   int base=MinipurgeSourceHeight.baseY(air,depth);
   check(base>=-63,"Original minimum63 belowzero must hold");
   check(base==-63||base+depth==air,"Source origin depth differs");
   if(air-depth>=-63)check(base+25==air+(depth==24?1:0),"Original rim must be surfaceair oroneabove");
   cases++;
  }
  for(int bad:new int[]{-1,0,23,26,128}){boolean rejected=false;try{MinipurgeSourceHeight.baseY(64,bad);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"Unsupported source depth accepted");cases++;}
  BlockPos origin=new BlockPos(101,39,-207);
  for(var rotation:Rotation.values()){
   check(StructureTemplate.transform(BlockPos.ZERO,Mirror.NONE,rotation,BlockPos.ZERO).offset(origin).equals(origin),"Original entrance corner shifted withrotation");
   int openings=0;
   for(int x=1;x<=4;x++)for(int z=1;z<=4;z++){
    BlockPos world=StructureTemplate.transform(new BlockPos(x,25,z),Mirror.NONE,rotation,BlockPos.ZERO).offset(origin);
    check(world.getY()==64,"Unchanged explicit opening localY25 must followorigin");
    check(Math.abs(world.getX()-origin.getX())<=4&&Math.abs(world.getZ()-origin.getZ())<=4,"Original6x6opening footprint altered");openings++;
   }
   check(openings==16,"Source topopening16cells");cases+=openings+1;
  }
  System.out.println("MINIPURGE_PLACEMENT_CONTRACTS_PASS "+cases+" sourceheight/guard/native4rotation/originalcorneropening cases; no world/access claim");
 }
}
