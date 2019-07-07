package ir.sina.mcptt

import javax.sip.message.Request

class McpttSession(val originatorRequest: Request) {
    var ffmpegProcess: Process? = null
    var inviteCount = 0
    var originatorSDP = ""
    var origRtpAddr = ""
    val peerRtps = mutableListOf<String>()
    fun addPeerRtp(rtpAddr: String) {
        peerRtps.add(rtpAddr)
    }
}