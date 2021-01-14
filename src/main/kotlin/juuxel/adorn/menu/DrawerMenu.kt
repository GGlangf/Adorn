package juuxel.adorn.menu

import juuxel.adorn.Adorn
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.menu.MenuContext

class DrawerMenu(syncId: Int, playerInv: PlayerInventory, context: MenuContext) :
    SimpleMenu(AdornMenus.DRAWER, syncId, playerInv, context, 5, 3, PALETTE_ID) {
    constructor(syncId: Int, playerInv: PlayerInventory) : this(syncId, playerInv, MenuContext.EMPTY)

    companion object {
        private val PALETTE_ID = Adorn.id("drawer")
    }
}
