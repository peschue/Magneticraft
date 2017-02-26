package com.cout970.magneticraft.tileentity.heat

import com.cout970.magneticraft.api.energy.IElectricNode
import com.cout970.magneticraft.api.heat.IHeatNode
import com.cout970.magneticraft.api.internal.energy.ElectricNode
import com.cout970.magneticraft.api.internal.heat.HeatContainer
import com.cout970.magneticraft.config.Config
import com.cout970.magneticraft.gui.common.DATA_ID_MACHINE_WORKING
import com.cout970.magneticraft.misc.ElectricConstants
import com.cout970.magneticraft.misc.network.IBD
import com.cout970.magneticraft.misc.tileentity.TraitHeat
import com.cout970.magneticraft.misc.tileentity.ITileTrait
import com.cout970.magneticraft.misc.world.isServer
import com.cout970.magneticraft.tileentity.electric.TileElectricBase
import com.cout970.magneticraft.util.COPPER_HEAT_CAPACITY
import com.cout970.magneticraft.util.COPPER_MELTING_POINT
import com.cout970.magneticraft.util.DEFAULT_CONDUCTIVITY
import com.cout970.magneticraft.util.ENERGY_TO_HEAT
import net.minecraftforge.fml.relauncher.Side

/**
 * Created by cout970 on 04/07/2016.
 */

class TileElectricHeater : TileElectricBase() {

    val traitHeat: TraitHeat = TraitHeat(this, heatNodes)
    override val traits: List<ITileTrait> = listOf(traitHeat)

    var mainNode = ElectricNode({ world }, { pos }, capacity = 1.25)
    override val electricNodes: List<IElectricNode>
        get() = listOf(mainNode)

    val heatNodes: List<IHeatNode>
        get() = listOf(heat)

    val heat = HeatContainer(dissipation = 0.0,
            specificHeat = COPPER_HEAT_CAPACITY * 3,
            maxHeat = COPPER_HEAT_CAPACITY * 3 * COPPER_MELTING_POINT,
            conductivity = DEFAULT_CONDUCTIVITY,
            worldGetter = { this.world },
            posGetter = { this.getPos() })

    override fun update() {
        if (worldObj.isServer) {
            if (mainNode.voltage >= ElectricConstants.TIER_1_MACHINES_MIN_VOLTAGE && heat.heat < heat.maxHeat) {
                val power = -Config.electricHeaterMaxConsumption * interpolate(mainNode.voltage, 60.0, 70.0)
                val applied = mainNode.applyPower(power, false)
                val energy = applied.toFloat()
                heat.applyHeat((energy * ENERGY_TO_HEAT).toDouble(), false)
            }
        }
        super.update()
    }

    override fun receiveSyncData(data: IBD, side: Side) {
        super.receiveSyncData(data, side)
        if (side == Side.SERVER) {
            data.getDouble(DATA_ID_MACHINE_WORKING, { heat.heat = it })
        }
    }
}