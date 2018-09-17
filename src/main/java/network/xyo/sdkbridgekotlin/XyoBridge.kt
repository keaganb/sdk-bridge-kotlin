package network.xyo.sdkbridgekotlin

import kotlinx.coroutines.experimental.async
import network.xyo.sdkcorekotlin.hashing.XyoHash
import network.xyo.sdkcorekotlin.network.XyoNetworkPipe
import network.xyo.sdkcorekotlin.network.XyoNetworkProcedureCatalogueInterface
import network.xyo.sdkcorekotlin.network.XyoNetworkProviderInterface
import network.xyo.sdkcorekotlin.node.*
import network.xyo.sdkcorekotlin.storage.XyoStorageProviderInterface
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * A functional Xyo Network Bridge.
 *
 * @param bridgeFromNetwork The network to collect data from and relay to archivists.
 * @param bridgeToNetwork The network to send the data to.
 * @param storageProvider The place to store all the origin blocks.
 * @pram hashingProvider The hashing provider to use.
 */
open class XyoBridge (private val bridgeFromNetwork : XyoNetworkProviderInterface,
                      private val bridgeToNetwork :  XyoNetworkProviderInterface,
                      storageProvider : XyoStorageProviderInterface,
                      hashingProvider : XyoHash.XyoHashProvider) : XyoRelayNode(storageProvider, hashingProvider) {


    override val procedureCatalogue : XyoNetworkProcedureCatalogueInterface = XyoBridgeCollectorProcedureCatalogue()

    override suspend fun findSomeoneToTalkTo() = suspendCoroutine<XyoNetworkPipe> { cont ->
        async {
            cont.resume(bridgeFromNetwork.find(procedureCatalogue))
        }

        async {
            cont.resume(bridgeToNetwork.find(procedureCatalogue))
        }
    }
}