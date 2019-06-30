package ir.sina.mcptt

import gov.nist.javax.sdp.parser.SDPAnnounceParser
import gov.nist.javax.sip.DialogTimeoutEvent
import gov.nist.javax.sip.SipListenerExt
import gov.nist.javax.sip.message.SIPRequest
import org.apache.logging.log4j.LogManager
import javax.sip.*
import javax.sip.address.SipURI
import javax.sip.header.CallIdHeader
import javax.sip.header.ContactHeader
import javax.sip.header.ContentTypeHeader
import javax.sip.message.Request.INVITE
import javax.sip.message.Request.REGISTER

class Mcptt(
    val userList: MutableCollection<SipURI>,
    sipFactory: SipFactory,
    val sipProvider: SipProvider

) :
    SipListenerExt {
    val messageFactory = sipFactory.createMessageFactory()
    val headerFactory = sipFactory.createHeaderFactory()
    val addressFactory = sipFactory.createAddressFactory()

    val logger = LogManager.getLogger("MCPTT")
    val callIdToMcpttSession = HashMap<CallIdHeader, McpttSession>()

    val fromUri = addressFactory.createSipURI("MCPTT-server", "127.0.0.1:5060")
    val fromAddr = addressFactory.createAddress(fromUri)
    val fromHeader = headerFactory.createFromHeader(fromAddr, "mcptt-tag")
    val contactHeader = headerFactory.createContactHeader(fromAddr)


    override fun processIOException(exceptionEvent: IOExceptionEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun processDialogTerminated(dialogTerminatedEvent: DialogTerminatedEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun processRequest(requestEvent: RequestEvent?) {
        if (requestEvent != null) {
            val request = requestEvent.request as SIPRequest
            if (request.method == REGISTER) {
                processRegisterRequest(request)
            } else if (request.method == INVITE) {
                initGroupCall(request)
            }
        }
    }

    private fun initGroupCall(request: SIPRequest) {
        logger.info("Handling INVITE request")
        val originatingContact = request.getHeader(ContactHeader.NAME) as ContactHeader
        val originatingUri = originatingContact.address.uri as SipURI
        val groupMembers = userList.filter { it != originatingUri }
        val mcpttSession = McpttSession(request)

        // extract sdp from request body
        request.multipartMimeContent.contents.forEach {
            logger.info(it.contentTypeHeader)
            val subType = it.contentTypeHeader.contentSubType.toLowerCase()
            if (subType == "sdp") {
                logger.info("SDP content is:\n${it.content}")
                mcpttSession.originatorSDP = it.content.toString()
            }
        }

        groupMembers.forEach {
            invitePeer(it, mcpttSession)
        }
    }

    private fun invitePeer(
        it: SipURI,
        mcpttSession: McpttSession
    ) {
        val toAddr = addressFactory.createAddress(it)
        val toHeader = headerFactory.createToHeader(toAddr, null)
        val callIdHeader = sipProvider.newCallId
        val cseqHeader = headerFactory.createCSeqHeader(1L, INVITE)
        val viaHeader = headerFactory.createViaHeader("127.0.0.1", 5060, "udp", null)
        val viaHeaders = listOf(viaHeader)
        val maxForward = headerFactory.createMaxForwardsHeader(70)
        val request = messageFactory.createRequest(
            it,
            INVITE,
            callIdHeader,
            cseqHeader,
            fromHeader,
            toHeader,
            viaHeaders,
            maxForward
        )
        request.addHeader(contactHeader)
        val trans = sipProvider.getNewClientTransaction(request)
        trans.sendRequest()
        callIdToMcpttSession[callIdHeader] = mcpttSession
        mcpttSession.inviteCount = mcpttSession.inviteCount + 1
    }

    private fun processRegisterRequest(request: SIPRequest) {
        val contact = request.getHeader(ContactHeader.NAME) as ContactHeader
        if (contact.address.uri.isSipURI) {
            userList.add(contact.address.uri as SipURI)
            logger.info("register request received form: " + contact.address.uri.toString())
            val contentType = request.getHeader(ContentTypeHeader.NAME) as ContentTypeHeader
            if (contentType.contentSubType == "vnd.3gpp.mcptt-info+xml") {
                val response = messageFactory.createResponse(200, request)
                sipProvider.sendResponse(response)
            }
        }
    }

    override fun processResponse(responseEvent: ResponseEvent?) {
        if (responseEvent == null) {
            logger.warn("responseEvent is null :-|")
        }
        if (responseEvent != null) {
            val dialog = responseEvent.clientTransaction.dialog
            val ack = dialog.createAck(dialog.localSeqNumber)
            dialog.sendAck(ack)
            val mcpttSession = callIdToMcpttSession[dialog.callId]
            if (mcpttSession != null) {
                accept(mcpttSession)
            } else {
                logger.warn("Couldn't find MCPTT session for call-id")
            }
        }
    }

    private fun accept(mcpttSession: McpttSession) {
        mcpttSession.inviteCount = mcpttSession.inviteCount - 1
        if (mcpttSession.inviteCount == 0) {
            logger.info("OK received from all invited clients")
            logger.info("Sending response to originator ...")
            // Accept MCPTT request to call
            val response = messageFactory.createResponse(200, mcpttSession.originatorRequest)
            response.addHeader(contactHeader)

            val parser = SDPAnnounceParser(mcpttSession.originatorSDP)
            val sdp = parser.parse()
            logger.info("SDP connectiona address is: ${sdp.connection.address}")
            sipProvider.getNewServerTransaction(mcpttSession.originatorRequest).sendResponse(response)
        }
    }

    override fun processDialogTimeout(timeoutEvent: DialogTimeoutEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun processTimeout(timeoutEvent: TimeoutEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun processTransactionTerminated(transactionTerminatedEvent: TransactionTerminatedEvent?) {
        if (transactionTerminatedEvent?.isServerTransaction!!) {
            logger.warn("server transaction terminated")
        } else {
            logger.warn("client transaction terminated")
        }
    }
}