package ir.sina.mcptt

import org.apache.logging.log4j.LogManager
import javax.sip.message.Request
import kotlin.concurrent.thread

class McpttSession(val originatorRequest: Request) {
    val logger = LogManager.getLogger("MCPTT-SESSION")

    val pb = ProcessBuilder()
    var inviteCount = 0
    var originatorSDP = ""
    var origRtpAddr = ""

    val peerRtps = mutableListOf<String>()
    fun addPeerRtp(rtpAddr: String) {
        peerRtps.add(rtpAddr)
    }

    fun startFFMPEG() {
        thread {
            val cmd = mutableListOf<String>()
            cmd.add("/usr/bin/ffmpeg")
            cmd.add("-f")
            cmd.add("rtp")
            cmd.add("-i")
            cmd.add("rtp://$origRtpAddr")
            cmd.add("-c:a")
            cmd.add("copy")
            cmd.add("-f")
            cmd.add("tee")
            cmd.add("-map")
            cmd.add("0:a")
            var peersArg = ""
            peerRtps.forEach { rtp ->
                peersArg += "[f=rtp]rtp://$rtp|"
            }
            peersArg = peersArg.dropLast(1)
            cmd.add(peersArg)
            pb.command(cmd)
            try {
                val ps = pb.start()
//                ps.inputStream.bufferedReader().lines().forEach {
//                    logger.info("ffmpeg output: $it)")
//                }
//                ps.errorStream.bufferedReader().lines().forEach {
//                    logger.info("ffmpeg error: $it)")
//                }
                val ret = ps.waitFor()
                logger.info("return value of ffmpeg is: $ret")
            } catch (e: Exception) {
                logger.warn("hey some exception occured.")
            }
        }
    }
}