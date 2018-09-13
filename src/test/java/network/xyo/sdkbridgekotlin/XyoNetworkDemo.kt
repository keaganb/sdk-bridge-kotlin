package network.xyo.sdkbridgekotlin

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import network.xyo.modtcpkotlin.XyoTcpNetwork
import network.xyo.sdkcorekotlin.boundWitness.XyoBoundWitness
import network.xyo.sdkcorekotlin.data.XyoPayload
import network.xyo.sdkcorekotlin.data.array.multi.XyoKeySet
import network.xyo.sdkcorekotlin.data.array.single.XyoSingleTypeArrayInt
import network.xyo.sdkcorekotlin.data.heuristics.number.signed.XyoRssi
import network.xyo.sdkcorekotlin.data.heuristics.number.unsigned.XyoIndex
import network.xyo.sdkcorekotlin.hashing.XyoPreviousHash
import network.xyo.sdkcorekotlin.hashing.basic.XyoSha256
import network.xyo.sdkcorekotlin.node.XyoNodeListener
import network.xyo.sdkcorekotlin.signing.XyoNextPublicKey
import network.xyo.sdkcorekotlin.signing.XyoSignatureSet
import network.xyo.sdkcorekotlin.signing.algorithms.ecc.secp256k.XyoSha256WithSecp256K
import network.xyo.sdkcorekotlin.signing.algorithms.ecc.secp256k.keys.XyoSecp256K1UnCompressedPublicKey
import network.xyo.sdkcorekotlin.signing.algorithms.ecc.secp256k.signatures.XyoSecp256kSha256WithEcdsaSignature
import network.xyo.sdkcorekotlin.signing.algorithms.rsa.XyoRsaPublicKey
import network.xyo.sdkcorekotlin.storage.XyoInMemoryStorageProvider
import network.xyo.sdksentinelkotlin.XyoSentinel

class XyoNetworkDemo {
    private val timeToRun = 10_000
    private val sentinelNetwork = XyoTcpNetwork(9112)

    private val bridgeFromNetwork = XyoTcpNetwork(9113)
    private val bridgeToNetwork = XyoTcpNetwork(9114)

    private val archivistFromNetwork = XyoTcpNetwork(9115)
    private val archivistToNetwork = XyoTcpNetwork(9116)

    private val sentinel = XyoSentinel(
            sentinelNetwork,
            XyoInMemoryStorageProvider(),
            XyoSha256
    )

    private val bridge =  XyoBridge(
            bridgeFromNetwork,
            bridgeToNetwork,
            XyoInMemoryStorageProvider(),
            XyoSha256
    )

    private val archivist =  XyoBridge(
            archivistFromNetwork,
            archivistToNetwork,
            XyoInMemoryStorageProvider(),
            XyoSha256
    )

    @kotlin.test.Test
    fun run () {
        runBlocking {
            XyoKeySet.enable()
            XyoPayload.enable()
            XyoSignatureSet.enable()
            XyoPreviousHash.enable()
            XyoSha256WithSecp256K.enable()
            XyoRssi.enable()
            XyoSecp256K1UnCompressedPublicKey.enable()
            XyoSecp256kSha256WithEcdsaSignature.enable()
            XyoRsaPublicKey.enable()
            XyoNextPublicKey.enable()
            XyoIndex.enable()
            XyoSha256.enable()
            XyoSingleTypeArrayInt.enable()
            XyoBoundWitness.enable()

            sentinelNetwork.addPeer("localhost", 9113)

            bridgeFromNetwork.addPeer("localhost", 9112)
            bridgeToNetwork.addPeer("localhost", 9115)

            archivistFromNetwork.addPeer("localhost", 9114)

            sentinel.addListiner("sentinel", object : XyoNodeListener {
                override fun onBoundWitnessEndFailure() {
                    println("S ERROR")
                }

                override fun onBoundWitnessDiscovered(boundWitness: XyoBoundWitness) {
                    println("S END")
                }

                override fun onBoundWitnessStart() {
                    println("S START")
                }
            })

            bridge.addListiner("sentine33l", object : XyoNodeListener {
                override fun onBoundWitnessEndFailure() {
                    println("B ERROR")
                }

                override fun onBoundWitnessDiscovered(boundWitness: XyoBoundWitness) {
                    println("B END")
                }

                override fun onBoundWitnessStart() {
                    println("B START")
                }
            })

            archivist.addListiner("arcc", object : XyoNodeListener {
                override fun onBoundWitnessEndFailure() {
                    println("A ERROR")
                }

                override fun onBoundWitnessDiscovered(boundWitness: XyoBoundWitness) {
                    println("A END")
                }

                override fun onBoundWitnessStart() {
                    println("A START")
                }
            })


            sentinel.start()
            bridge.start()
            archivist.start()

            delay(timeToRun)
        }
    }
}