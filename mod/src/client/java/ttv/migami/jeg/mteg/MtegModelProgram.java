package ttv.migami.jeg.mteg;
import java.util.*;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.init.ModDataComponents;
import ttv.migami.jeg.item.attachment.AttachmentType;
import ttv.migami.jeg.item.attachment.GunAttachments;
/** Generated, source-faithful MTEG original part draw programs. Only API/transport substitutions;
 * authored branches, matrices, cooldown easing and empty-chamber behavior are retained verbatim. */
public final class MtegModelProgram {
    public record Part(String model,Matrix4f transform) {}
    public static List<Part> plan(String id,ItemStack stack,float cooldown,boolean local) {
        List<Part> draws=new ArrayList<>();Pose pose=new Pose();
        switch(id) {
case "akm" -> {
draws.add(new Part("mteg:special/akm/main",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/akm/ejector",pose.copy()));
pose.popPose();
}
}
case "akm_custom" -> {
draws.add(new Part("mteg:special/akm_custom/main",pose.copy()));
if(attachment(stack, "STOCK", "light_stock"))
draws.add(new Part("mteg:special/akm_custom/stock_light",pose.copy()));
if(attachment(stack, "STOCK", "tactical_stock"))
draws.add(new Part("mteg:special/akm_custom/stock_tactical",pose.copy()));
if(attachment(stack, "STOCK", "weighted_stock"))
draws.add(new Part("mteg:special/akm_custom/stock_heavy",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/akm_custom/ejector",pose.copy()));
pose.popPose();
}
}
case "as_val" -> {
draws.add(new Part("mteg:special/as_val/main",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/as_val/ejector",pose.copy()));
pose.popPose();
}
}
case "awp" -> {
draws.add(new Part("mteg:special/awp/main",pose.copy()));
if(local) {
pose.pushPose();
cooldown = (float) ease(cooldown);
if (cooldown != 0) {
pose.translate(-0.172, -0.0755, cooldown/10);
pose.mulPose(Axis.ZN.rotationDegrees(-45F));
}
draws.add(new Part("mteg:special/awp/bolt",pose.copy()));
pose.popPose();
pose.pushPose();
pose.translate(0, 0, cooldown/10);
draws.add(new Part("mteg:special/awp/ejector",pose.copy()));
pose.popPose();
}
}
case "baker_rifle" -> {
draws.add(new Part("mteg:special/gun/baker_rifle",pose.copy()));
}
case "benelli_m4" -> {
draws.add(new Part("mteg:special/benelli_m4/main",pose.copy()));
if(attachment(stack, "STOCK", "light_stock"))
draws.add(new Part("mteg:special/benelli_m4/stock_light",pose.copy()));
if(attachment(stack, "STOCK", "tactical_stock"))
draws.add(new Part("mteg:special/benelli_m4/stock_tactical",pose.copy()));
if(attachment(stack, "STOCK", "weighted_stock"))
draws.add(new Part("mteg:special/benelli_m4/stock_heavy",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/benelli_m4/pump",pose.copy()));
pose.popPose();
}
}
case "blue_heat" -> {
draws.add(new Part("mteg:special/blue_heat/main",pose.copy()));
if(attachment(stack, "STOCK", "light_stock"))
draws.add(new Part("mteg:special/blue_heat/stock_light",pose.copy()));
if(attachment(stack, "STOCK", "tactical_stock"))
draws.add(new Part("mteg:special/blue_heat/stock_tactical",pose.copy()));
if(attachment(stack, "STOCK", "weighted_stock"))
draws.add(new Part("mteg:special/blue_heat/stock_heavy",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/blue_heat/ejector",pose.copy()));
pose.popPose();
}
}
case "double_barrel" -> {
draws.add(new Part("mteg:special/double_barrel/main",pose.copy()));
if(local) {
pose.pushPose();
if(hasAmmo(stack))
pose.translate(0, 0, 0.1f * (-4.5 * Math.pow(cooldown-0.5, 2) + 1.125));
else if(!hasAmmo(stack)) {
if(cooldown > 0.5)
pose.mulPose(Axis.XN.rotationDegrees(45F));
else
pose.translate(0, -0.35f, -0.175);
pose.mulPose(Axis.XN.rotationDegrees(45F));
}
draws.add(new Part("mteg:special/double_barrel/barrels",pose.copy()));
pose.popPose();
}
}
case "famas" -> {
draws.add(new Part("mteg:special/famas/main",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/6.5);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/famas/ejector",pose.copy()));
pose.popPose();
}
}
case "flamer" -> {
draws.add(new Part("mteg:special/gun/flamer",pose.copy()));
}
case "flare_gun" -> {
draws.add(new Part("mteg:special/gun/flare_gun",pose.copy()));
}
case "g36c" -> {
draws.add(new Part("mteg:special/g36c/main",pose.copy()));
if(attachment(stack, "STOCK", "light_stock"))
draws.add(new Part("mteg:special/g36c/stock_light",pose.copy()));
if(attachment(stack, "STOCK", "tactical_stock"))
draws.add(new Part("mteg:special/g36c/stock_tactical",pose.copy()));
if(attachment(stack, "STOCK", "weighted_stock"))
draws.add(new Part("mteg:special/g36c/stock_heavy",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/6.5);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/g36c/ejector",pose.copy()));
pose.popPose();
}
}
case "glock_17" -> {
draws.add(new Part("mteg:special/glock_17/main",pose.copy()));
if(local) {
pose.pushPose();
if(hasAmmo(stack))
pose.translate(0, 0, 0.1f * (-4.5 * Math.pow(cooldown-0.5, 2) + 1.125));
else if(!hasAmmo(stack)) {
if(cooldown > 0.5)
pose.translate(0, 0, 0.185f * (-4.5 * Math.pow(cooldown-0.5, 2) + 0.5));
else
pose.translate(0, 0, 0.185f * (-4.5 * Math.pow(0.5-0.5, 2) + 0.5));
}
draws.add(new Part("mteg:special/glock_17/slide",pose.copy()));
pose.popPose();
}
}
case "hellfire" -> {
draws.add(new Part("mteg:special/hellfire/main",pose.copy()));
if(attachment(stack, "STOCK", "light_stock"))
draws.add(new Part("mteg:special/hellfire/stock_light",pose.copy()));
if(attachment(stack, "STOCK", "tactical_stock"))
draws.add(new Part("mteg:special/hellfire/stock_tactical",pose.copy()));
if(attachment(stack, "STOCK", "weighted_stock"))
draws.add(new Part("mteg:special/hellfire/stock_heavy",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/hellfire/ejector",pose.copy()));
pose.popPose();
}
}
case "hog_bonker" -> {
draws.add(new Part("mteg:special/hog_bonker/main",pose.copy()));
if(local) {
pose.pushPose();
if(hasAmmo(stack))
pose.translate(0, 0, 0.1f * (-4.5 * Math.pow(cooldown-0.5, 2) + 1.125));
else if(!hasAmmo(stack)) {
if(cooldown > 0.5)
pose.mulPose(Axis.XN.rotationDegrees(45F));
else
pose.translate(0, -0.35f, -0.175);
pose.mulPose(Axis.XN.rotationDegrees(45F));
}
draws.add(new Part("mteg:special/hog_bonker/barrels",pose.copy()));
pose.popPose();
}
}
case "lanchester" -> {
draws.add(new Part("mteg:special/lanchester/main",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
pose.translate(0, 0, -cooldown/10);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/lanchester/ejector",pose.copy()));
pose.popPose();
}
}
case "m14" -> {
draws.add(new Part("mteg:special/m14/main",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/m14/ejector",pose.copy()));
pose.popPose();
}
}
case "m14_ebr" -> {
draws.add(new Part("mteg:special/m14_ebr/main",pose.copy()));
if(attachment(stack, "STOCK", "light_stock"))
draws.add(new Part("mteg:special/m14_ebr/stock_light",pose.copy()));
if(attachment(stack, "STOCK", "tactical_stock"))
draws.add(new Part("mteg:special/m14_ebr/stock_tactical",pose.copy()));
if(attachment(stack, "STOCK", "weighted_stock"))
draws.add(new Part("mteg:special/m14_ebr/stock_heavy",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/m14_ebr/ejector",pose.copy()));
pose.popPose();
}
}
case "m16a1" -> {
draws.add(new Part("mteg:special/gun/m16a1",pose.copy()));
}
case "m1911" -> {
draws.add(new Part("mteg:special/m1911/main",pose.copy()));
if(local) {
pose.pushPose();
if(hasAmmo(stack))
pose.translate(0, 0, 0.1f * (-4.5 * Math.pow(cooldown-0.5, 2) + 1.125));
else if(!hasAmmo(stack)) {
if(cooldown > 0.5)
pose.translate(0, 0, 0.185f * (-4.5 * Math.pow(cooldown-0.5, 2) + 0.5));
else
pose.translate(0, 0, 0.185f * (-4.5 * Math.pow(0.5-0.5, 2) + 0.5));
}
draws.add(new Part("mteg:special/m1911/slide",pose.copy()));
pose.popPose();
}
}
case "m1_garand" -> {
draws.add(new Part("mteg:special/m1_garand/main",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/m1_garand/ejector",pose.copy()));
pose.popPose();
}
}
case "m2_flamethrower" -> {
draws.add(new Part("mteg:special/gun/m2_flamethrower",pose.copy()));
}
case "micro_uzi" -> {
draws.add(new Part("mteg:special/gun/micro_uzi",pose.copy()));
}
case "mossberg_590" -> {
draws.add(new Part("mteg:special/mossberg_590/main",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/mossberg_590/pump",pose.copy()));
pose.popPose();
}
}
case "mp5" -> {
draws.add(new Part("mteg:special/gun/mp5",pose.copy()));
}
case "ppsh_41" -> {
draws.add(new Part("mteg:special/ppsh_41/main",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
pose.translate(0, 0, -cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/ppsh_41/ejector",pose.copy()));
pose.popPose();
}
}
case "remington_870" -> {
draws.add(new Part("mteg:special/remington_870/main",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/remington_870/pump",pose.copy()));
pose.popPose();
}
}
case "scar_h" -> {
draws.add(new Part("mteg:special/scar_h/main",pose.copy()));
if(attachment(stack, "STOCK", "light_stock"))
draws.add(new Part("mteg:special/scar_h/stock_light",pose.copy()));
if(attachment(stack, "STOCK", "tactical_stock"))
draws.add(new Part("mteg:special/scar_h/stock_tactical",pose.copy()));
if(attachment(stack, "STOCK", "weighted_stock"))
draws.add(new Part("mteg:special/scar_h/stock_heavy",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/scar_h/ejector",pose.copy()));
pose.popPose();
}
}
case "scar_l" -> {
draws.add(new Part("mteg:special/scar_l/main",pose.copy()));
if(attachment(stack, "STOCK", "light_stock"))
draws.add(new Part("mteg:special/scar_l/stock_light",pose.copy()));
if(attachment(stack, "STOCK", "tactical_stock"))
draws.add(new Part("mteg:special/scar_l/stock_tactical",pose.copy()));
if(attachment(stack, "STOCK", "weighted_stock"))
draws.add(new Part("mteg:special/scar_l/stock_heavy",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/scar_l/ejector",pose.copy()));
pose.popPose();
}
}
case "thompson" -> {
draws.add(new Part("mteg:special/thompson/main",pose.copy()));
if(attachment(stack, "UNDER_BARREL", "light_grip"))
draws.add(new Part("mteg:special/thompson/grip_light",pose.copy()));
if(attachment(stack, "UNDER_BARREL", "vertical_grip") ||
attachment(stack, "UNDER_BARREL", "angled_grip"))
draws.add(new Part("mteg:special/thompson/grip_specialised",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/12);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/thompson/ejector",pose.copy()));
pose.popPose();
}
}
case "uzi" -> {
draws.add(new Part("mteg:special/gun/uzi",pose.copy()));
}
case "vss_vintorez" -> {
draws.add(new Part("mteg:special/vss_vintorez/main",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/vss_vintorez/ejector",pose.copy()));
pose.popPose();
}
}
case "walther_ppk" -> {
draws.add(new Part("mteg:special/walther_ppk/main",pose.copy()));
if(local) {
pose.pushPose();
if(hasAmmo(stack))
pose.translate(0, 0, 0.1f * (-4.5 * Math.pow(cooldown-0.5, 2) + 1.125));
else if(!hasAmmo(stack)) {
if(cooldown > 0.5)
pose.translate(0, 0, 0.185f * (-4.5 * Math.pow(cooldown-0.5, 2) + 0.5));
else
pose.translate(0, 0, 0.185f * (-4.5 * Math.pow(0.5-0.5, 2) + 0.5));
}
draws.add(new Part("mteg:special/walther_ppk/slide",pose.copy()));
pose.popPose();
}
}
case "welrod" -> {
draws.add(new Part("mteg:special/welrod/main",pose.copy()));
if(local) {
pose.pushPose();
if (cooldown != 0 && cooldown < 0.86) {
pose.translate(-0.225, -0.0975, 0);
pose.mulPose(Axis.ZN.rotationDegrees(-45F));
if (cooldown < 0.74 && cooldown > 0.42)
pose.translate(0, 0, (-cooldown+0.74)/4);
if (cooldown < 0.42 && cooldown > 0.07)
pose.translate(0, 0, (cooldown-0.07)/4);
}
draws.add(new Part("mteg:special/welrod/bolt",pose.copy()));
pose.popPose();
}
}
case "wrapped_rifle" -> {
draws.add(new Part("mteg:special/wrapped_rifle/main",pose.copy()));
if(local) {
pose.pushPose();
pose.translate(0, -5.8 * 0.0625, 0);
cooldown = (float) ease(cooldown);
pose.translate(0, 0, cooldown/8);
pose.translate(0, 5.8 * 0.0625, 0);
draws.add(new Part("mteg:special/wrapped_rifle/ejector",pose.copy()));
pose.popPose();
}
}
default -> throw new IllegalArgumentException("No original MTEG model program: "+id);
        }
        return List.copyOf(draws);
    }
    private static boolean hasAmmo(ItemStack stack) { return stack.getOrDefault(ModDataComponents.GUN_AMMO.get(),0)>0; }
    private static boolean attachment(ItemStack stack,String type,String id) {
        AttachmentType slot=type.equals("STOCK") ? AttachmentType.STOCK : AttachmentType.UNDER_BARREL;
        return GunAttachments.id(stack,slot).map(value->value.getNamespace().equals("jeg")&&value.getPath().equals(id)).orElse(false);
    }
    private static double ease(double x) { return 1-Math.pow(1-2*x,4); }
    private static final class Pose {
        private Matrix4f matrix=new Matrix4f();private final Deque<Matrix4f> parents=new ArrayDeque<>();
        void pushPose(){parents.push(new Matrix4f(matrix));}void popPose(){matrix=parents.pop();}
        void translate(double x,double y,double z){matrix.translate((float)x,(float)y,(float)z);}
        void mulPose(Quaternionfc quaternion){matrix.rotate(quaternion);}
        Matrix4f copy(){return new Matrix4f(matrix);}
    }
}
