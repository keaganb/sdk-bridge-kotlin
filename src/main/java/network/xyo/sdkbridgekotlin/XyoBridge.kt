package network.xyo.sdkbridgekotlin

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import network.xyo.sdkcorekotlin.boundWitness.XyoBoundWitness
import network.xyo.sdkcorekotlin.data.XyoObject
import network.xyo.sdkcorekotlin.data.array.single.XyoBridgeBlockSet
import network.xyo.sdkcorekotlin.data.array.single.XyoSingleTypeArrayInt
import network.xyo.sdkcorekotlin.hashing.XyoHash
import network.xyo.sdkcorekotlin.network.XyoNetworkPipe
import network.xyo.sdkcorekotlin.network.XyoNetworkProcedureCatalogueInterface
import network.xyo.sdkcorekotlin.network.XyoNetworkProviderInterface
import network.xyo.sdkcorekotlin.network.XyoProcedureCatalogue
import network.xyo.sdkcorekotlin.node.*
import network.xyo.sdkcorekotlin.signing.algorithms.rsa.XyoRsaWithSha256
import network.xyo.sdkcorekotlin.storage.XyoStorageProviderInterface
import kotlin.coroutines.experimental.suspendCoroutine

open class XyoBridge (private val bridgeFromNetwork : XyoNetworkProviderInterface,
                      private val bridgeToNetwork :  XyoNetworkProviderInterface,
                      storageProvider : XyoStorageProviderInterface,
                      hashingProvider : XyoHash.XyoHashProvider) : XyoNodeBase(storageProvider, hashingProvider) {

    private val bridgeToArchivistOption = XyoBridgingOption(hashingProvider)
    private val originBlocksToBridge = XyoBridgeQueue()
    private val procedureCatalogue : XyoNetworkProcedureCatalogueInterface = XyoBridgeCollectorProcedureCatalogue()
    private var running = false

    private val mainBoundWitnessListener = object : XyoNodeListener {
        override fun onBoundWitnessEndFailure(error: Exception?) {}
        override fun onBoundWitnessStart() {}

        override fun onBoundWitnessDiscovered(boundWitness: XyoBoundWitness) {
            originBlocksToBridge.addBlock(boundWitness)
            bridgeToArchivistOption.updateOriginChain(getOriginBlocksToBridge())
        }

    }

    private val bridgeQueueListener = object : XyoBridgeQueue.Companion.XyoBridgeQueueListener {
        override fun onRemove(boundWitness: XyoBoundWitness) {
            async {
                val blockHash = boundWitness.getHash(hashingProvider).await()
                originBlocks.removeOriginBlock(blockHash.typed)
            }
        }
    }

    fun stop () {
        if (running) {
            running = false
        }
    }

    fun start () {
        if (!running) {
            running =  true
            loop()
        }
    }

    private fun getOriginBlocksToBridge() : Array<XyoObject> {
        return originBlocksToBridge.getBlocksToBridge()
    }

    override fun getChoice(catalog: Int): Int {
        if (catalog and XyoProcedureCatalogue.TAKE_ORIGIN_CHAIN == XyoProcedureCatalogue.TAKE_ORIGIN_CHAIN) {
            return  XyoProcedureCatalogue.GIVE_ORIGIN_CHAIN
        } else if (catalog and XyoProcedureCatalogue.GIVE_ORIGIN_CHAIN == XyoProcedureCatalogue.GIVE_ORIGIN_CHAIN) {
            return XyoProcedureCatalogue.TAKE_ORIGIN_CHAIN
        }

        return XyoProcedureCatalogue.BOUND_WITNESS
    }

    private suspend fun findSomeoneToTalkTo() = suspendCoroutine<XyoNetworkPipe> { cont ->
        async {
            cont.resume(bridgeFromNetwork.find(procedureCatalogue))
        }

        async {
            cont.resume(bridgeToNetwork.find(procedureCatalogue))
        }
    }

    private fun doConnection() = async {
        val connectionToOtherPartyFrom = findSomeoneToTalkTo()
        if (!running) return@async

        if (connectionToOtherPartyFrom.initiationData == null) {
            val whatTheOtherPartyWantsToDo = connectionToOtherPartyFrom.peer.getRole()
            if (procedureCatalogue.canDo(whatTheOtherPartyWantsToDo)) {
                doBoundWitness(null, connectionToOtherPartyFrom)
            } else {
                connectionToOtherPartyFrom.close()
            }
        } else {
            doBoundWitness(connectionToOtherPartyFrom.initiationData, connectionToOtherPartyFrom)
        }
    }

    private fun loop () {
        launch {
            while (running) {
                doConnection().await()
            }
        }
    }

    init {
        addListener(this.toString(), mainBoundWitnessListener)
        addBoundWitnessOption(bridgeToArchivistOption)
        originBlocksToBridge.addListener(this.toString(), bridgeQueueListener)
    }
}