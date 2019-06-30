import ir.sina.mcptt.Mcptt
import ir.sina.mcptt.MyStackLogger
import java.util.*
import javax.sip.SipFactory
import javax.sip.address.SipURI


fun main() {
    val properties = Properties()
    properties.setProperty("javax.sip.STACK_NAME", "MCPTT")
    properties.setProperty("javax.sip.IP_ADDRESS", "0.0.0.0")
    properties.setProperty("gov.nist.javax.sip.STACK_LOGGER", MyStackLogger::class.java.name)

    val sipFactory = SipFactory.getInstance()
    val sipStack = sipFactory.createSipStack(properties)

    val udp = sipStack.createListeningPoint("127.0.0.1", 5060, "udp")


    val sipProviderUdp = sipStack.createSipProvider(udp)

    val users = mutableListOf<SipURI>()
    val mcptt = Mcptt(users, sipFactory, sipProviderUdp)
    sipProviderUdp.addSipListener(mcptt)
}