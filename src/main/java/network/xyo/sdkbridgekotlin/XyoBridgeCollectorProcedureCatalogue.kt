package network.xyo.sdkbridgekotlin

import network.xyo.sdkcorekotlin.network.XyoNetworkProcedureCatalogueInterface
import network.xyo.sdkcorekotlin.network.XyoProcedureCatalogue
import java.nio.ByteBuffer

class XyoBridgeCollectorProcedureCatalogue : XyoNetworkProcedureCatalogueInterface {
    override fun canDo(byteArray: ByteArray): Boolean {
        val bitFlags = ByteBuffer.wrap(byteArray).int
        return (bitFlags and whatBridgeCanDoWithOther != 0)
    }

    override fun getEncodedCanDo(): ByteArray {
        return whatBridgeCanDoForOtherEncoded
    }

    companion object {
        private const val whatBridgeCanDoForOther = XyoProcedureCatalogue.BOUND_WITNESS or XyoProcedureCatalogue.GIVE_ORIGIN_CHAIN or XyoProcedureCatalogue.TAKE_ORIGIN_CHAIN
        private const val whatBridgeCanDoWithOther = XyoProcedureCatalogue.BOUND_WITNESS or XyoProcedureCatalogue.TAKE_ORIGIN_CHAIN or XyoProcedureCatalogue.GIVE_ORIGIN_CHAIN
        private val whatBridgeCanDoForOtherEncoded =  ByteBuffer.allocate(4).putInt(whatBridgeCanDoForOther).array()
    }
}

