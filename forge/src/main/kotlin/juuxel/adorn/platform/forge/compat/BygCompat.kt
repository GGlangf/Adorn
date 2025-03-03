package juuxel.adorn.platform.forge.compat

import juuxel.adorn.api.block.BlockVariant
import juuxel.adorn.compat.BlockVariantSet

object BygCompat : BlockVariantSet {
    override val woodVariants = listOf(
        "aspen",
        "baobab",
        "blue_enchanted",
        "bulbis",
        "cherry",
        "cika",
        "cypress",
        "ebony",
        "embur",
        "ether",
        "fir",
        "green_enchanted",
        "holly",
        "imparius",
        "jacaranda",
        "lament",
        "mahogany",
        "mangrove",
        "maple",
        "nightshade",
        "palm",
        "pine",
        "rainbow_eucalyptus",
        "redwood",
        "skyris",
        "sythian",
        "willow",
        "witch_hazel",
        "zelkova"
    ).map { BlockVariant.Wood("byg/$it") }

    override val stoneVariants = listOf(
        "dacite",
        "dacite_brick",
        "dacite_cobblestone",
        "mossy_stone",
        "rocky_stone",
        "scoria_stone",
        "scoria_cobblestone",
        "scoria_stonebrick",
        "soapstone",
        "polished_soapstone",
        "soapstone_brick",
        "soapstone_tile",
        "red_rock",
        "red_rock_brick",
        "mossy_red_rock_brick",
        "cracked_red_rock_brick",
        "chiseled_red_rock_brick"
    ).map { BlockVariant.Stone("byg/$it") }
}
