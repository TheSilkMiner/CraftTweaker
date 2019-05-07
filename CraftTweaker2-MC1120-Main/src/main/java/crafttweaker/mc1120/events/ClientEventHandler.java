package crafttweaker.mc1120.events;

import crafttweaker.api.formatting.IFormattedText;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.tooltip.*;
import crafttweaker.mc1120.formatting.IMCFormattedString;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.relauncher.*;
import org.lwjgl.input.Keyboard;

public class ClientEventHandler {
    
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemTooltip(ItemTooltipEvent ev) {
        if(!ev.getItemStack().isEmpty()) {
            IItemStack itemStack = CraftTweakerMC.getIItemStack(ev.getItemStack());
            if(IngredientTooltips.shouldClearToolTip(itemStack)) {
                ev.getToolTip().clear();
            }
            for(IFormattedText tooltip : IngredientTooltips.getTooltips(itemStack)) {
                ev.getToolTip().add(((IMCFormattedString) tooltip).getTooltipString());
            }
            for(ITooltipFunction tooltip : IngredientTooltips.getAdvancedTooltips(itemStack)) {
                ev.getToolTip().add(tooltip.process(itemStack));
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                for(IFormattedText tooltip : IngredientTooltips.getShiftTooltips(itemStack)) {
                    ev.getToolTip().add(((IMCFormattedString) tooltip).getTooltipString());
                }
                for(ITooltipFunction tooltip : IngredientTooltips.getAdvancedShiftTooltips(itemStack)) {
                    ev.getToolTip().add(tooltip.process(itemStack));
                }
            }
        }
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onGuiOpenEvent(GuiOpenEvent ev) {
    
    }
}
