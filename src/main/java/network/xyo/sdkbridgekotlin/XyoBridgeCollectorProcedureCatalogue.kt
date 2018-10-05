package network.xyo.sdkbridgekotlin

import network.xyo.sdkcorekotlin.network.XyoNetworkProcedureCatalogueInterface
import network.xyo.sdkcorekotlin.network.XyoProcedureCatalogue
import java.nio.ByteBuffer

/**
 * A bridges Procedure Catalogue.
 */
class XyoBridgeCollectorProcedureCatalogue : XyoNetworkProcedureCatalogueInterface {
    private var isBridging = true

    private val whatBridgeCanDoForOther : Int
        get() {
            if (isBridging) {
                return XyoProcedureCatalogue.BOUND_WITNESS or XyoProcedureCatalogue.GIVE_ORIGIN_CHAIN or XyoProcedureCatalogue.TAKE_ORIGIN_CHAIN
            }
            return XyoProcedureCatalogue.BOUND_WITNESS or XyoProcedureCatalogue.GIVE_ORIGIN_CHAIN
        }


    private val whatBridgeCanDoWithOther : Int
        get() {
            if (isBridging) {
                return XyoProcedureCatalogue.BOUND_WITNESS or XyoProcedureCatalogue.TAKE_ORIGIN_CHAIN or XyoProcedureCatalogue.GIVE_ORIGIN_CHAIN
            }
            return XyoProcedureCatalogue.BOUND_WITNESS or XyoProcedureCatalogue.TAKE_ORIGIN_CHAIN
        }

    private val whatBridgeCanDoForOtherEncoded : ByteArray
        get() = ByteBuffer.allocate(4).putInt(whatBridgeCanDoForOther).array()

    fun enableBridgeing(boolean: Boolean) {
        isBridging = boolean
    }

    override fun canDo(byteArray: ByteArray): Boolean {
        val bitFlags = ByteBuffer.wrap(byteArray).int
        return (bitFlags and whatBridgeCanDoWithOther != 0)
    }

    override fun getEncodedCanDo(): ByteArray {
        return whatBridgeCanDoForOtherEncoded
    }
}

