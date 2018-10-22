package network.xyo.sdkbridgekotlin

import kotlinx.coroutines.experimental.*
import network.xyo.sdkcorekotlin.hashing.XyoHash
import network.xyo.sdkcorekotlin.network.XyoNetworkPipe
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

    private var index = 0
    private var isBridging = true
    protected val whoToTalkTo : XyoBridgeTalkTo
            get() = nextChoice()


    override val procedureCatalogue = XyoBridgeCollectorProcedureCatalogue()

    /**
     * The problem is that the whoToTalkTo is a getter and it can get to the bottom
     */
    override suspend fun findSomeoneToTalkTo() = suspendCoroutine<XyoNetworkPipe> { cont ->
        var bridgeFromFinder : Deferred<XyoNetworkPipe?>? = null
        var bridgeToFinder : Deferred<XyoNetworkPipe?>? = null
        var bridgeFromFinderHandler : Deferred<Unit>?= null
        var bridgeToFinderHandler : Deferred<Unit>? = null

        bridgeFromFinder = bridgeFromNetwork.find(procedureCatalogue)
        bridgeFromFinderHandler = GlobalScope.async {
            if (whoToTalkTo == XyoBridgeTalkTo.SEND) {
                delay((PRIORITY_HEAD_START * Math.random()).toInt())
            }

            val con = bridgeFromFinder.await()!!
            index++

            bridgeFromNetwork.stop()
            bridgeToNetwork.stop()

            cont.resume(con)
            bridgeToFinder?.cancel()
            bridgeFromFinder.cancel()
            bridgeToFinderHandler?.cancel()
            bridgeFromFinderHandler?.cancel()
            coroutineContext.cancel()
            return@async
        }

        bridgeToFinder = bridgeToNetwork.find(procedureCatalogue)
        bridgeToFinderHandler = GlobalScope.async {
            if (whoToTalkTo == XyoBridgeTalkTo.COLLECT) {
                delay((PRIORITY_HEAD_START * Math.random()).toInt())
            }

            val con = bridgeToFinder.await()!!
            index++

            bridgeFromNetwork.stop()
            bridgeToNetwork.stop()

            cont.resume(con)
            bridgeFromFinder.cancel()
            bridgeToFinder.cancel()
            bridgeFromFinderHandler.cancel()
            bridgeToFinderHandler?.cancel()
            coroutineContext.cancel()
            return@async
        }
    }

    private fun nextChoice () : XyoBridgeTalkTo {
        if (index % 3 == 0 && isBridging) {
            return XyoBridgeTalkTo.SEND
        }
        return XyoBridgeTalkTo.COLLECT
    }

    fun enableBridging (boolean: Boolean) {
        isBridging = boolean
        procedureCatalogue.enableBridging(boolean)
    }

    companion object {
        enum class XyoBridgeTalkTo {
            COLLECT,
            SEND
        }

        const val PRIORITY_HEAD_START = 60_000
    }
}