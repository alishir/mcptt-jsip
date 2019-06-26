package ir.sina.mcptt

import javax.sip.message.Request

class McpttSession(val originatorRequest: Request) {
    var inviteCount = 0
}